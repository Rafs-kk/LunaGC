package emu.grasscutter.server.packet.recv;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.WireFormat;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketGetWidgetQuickSlotListRsp;
import emu.grasscutter.server.packet.send.PacketGetWidgetSlotRsp;
import emu.grasscutter.server.packet.send.PacketSetWidgetSlotRsp;
import emu.grasscutter.server.packet.send.PacketWidgetSlotChangeNotify;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Opcodes(PacketOpcodes.SetWidgetSlotReq)
public class HandlerSetWidgetSlotReq extends PacketHandler {

    private static final int WIDGET_SLOT_OP_ATTACH = 0;
    private static final int WIDGET_SLOT_OP_DETACH = 1;

    private static final int WIDGET_SLOT_TAG_QUICK_USE = 0;

    // Companion/pet-like gadgets that should NOT be treated as normal quick-use gadgets.
    private static final Set<Integer> COMPANION_WIDGET_IDS = Set.of(
            220014, // Mini Seelie: Dayflower
            220015, // Mini Seelie: Rosé
            220016, // Mini Seelie: Curcuma
            220023, // Endora
            220038, // Mini Seelie: Viola
            220045, // Shiki Koshou
            220062, // Mini Seelie: Moss
            220072, // Jinni in the Magic Bottle — Liloupar
            220074, // Cloud Retainer's Damasked Device
            220084, // Itty Bitty Octobaby
            220096, // Mini Seelie: Brilliance
            220105  // Firstborn Firesprite
    );

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        int materialId = 0;
        int op = WIDGET_SLOT_OP_ATTACH;
        List<Integer> tagList = new ArrayList<>();

        CodedInputStream input = CodedInputStream.newInstance(payload);

        while (!input.isAtEnd()) {
            int tag = input.readTag();
            if (tag == 0) break;

            int fieldNumber = tag >>> 3;
            int wireType = WireFormat.getTagWireType(tag);

            switch (fieldNumber) {
                // REL6.0 SetWidgetSlotReq / OHKCBLEJBOE:
                // uint32 material_id = 3;
                case 3 -> materialId = input.readUInt32();

                // REL6.0 SetWidgetSlotReq / OHKCBLEJBOE:
                // repeated WidgetSlotTag tag_list = 8;
                case 8 -> {
                    if (wireType == WireFormat.WIRETYPE_VARINT) {
                        tagList.add(input.readEnum());
                    } else if (wireType == WireFormat.WIRETYPE_LENGTH_DELIMITED) {
                        int oldLimit = input.pushLimit(input.readRawVarint32());

                        while (input.getBytesUntilLimit() > 0) {
                            tagList.add(input.readEnum());
                        }

                        input.popLimit(oldLimit);
                    } else {
                        input.skipField(tag);
                    }
                }

                // REL6.0 SetWidgetSlotReq / OHKCBLEJBOE:
                // WidgetSlotOp op = 15;
                case 15 -> op = input.readEnum();

                default -> input.skipField(tag);
            }
        }

        Player player = session.getPlayer();

        if (materialId == 0 && op == WIDGET_SLOT_OP_DETACH) {
            materialId = player.getWidgetId();
        }

        Grasscutter.getLogger().info(
                "[WIDGET DEBUG] SetWidgetSlotReq materialId={}, op={}, tagList={}, currentQuickUse={}, payloadLen={}",
                materialId,
                op,
                tagList,
                player.getWidgetId(),
                payload == null ? 0 : payload.length
        );

        if (materialId == 0) {
            Grasscutter.getLogger().warn("[WIDGET DEBUG] Ignoring SetWidgetSlotReq with materialId=0");
            sendCurrentWidgetState(session, player);
            return;
        }

        // Companion gadgets are quarantined for now.
        // They probably need a real companion/attach-avatar entity path later.
        // For now, ignore them without sending a fake success response.
        if (isCompanionWidget(materialId)) {
            Grasscutter.getLogger().warn(
                    "[WIDGET DEBUG] Companion gadget materialId={} is unsupported for now; ignoring without changing quick-use state",
                    materialId
            );

            sendCurrentWidgetState(session, player);
            return;
        }

        handleQuickUseWidget(session, player, materialId, op);
    }

    private void handleQuickUseWidget(GameSession session, Player player, int materialId, int op) {
        int oldQuickUseId = player.getWidgetId();

        if (isCompanionWidget(oldQuickUseId)) {
            Grasscutter.getLogger().warn(
                    "[WIDGET DEBUG] Clearing companion materialId={} from quick-use slot",
                    oldQuickUseId
            );

            oldQuickUseId = 0;
            player.setWidgetId(0);
        }

        if (op == WIDGET_SLOT_OP_DETACH) {
            if (oldQuickUseId > 0) {
                session.send(new PacketWidgetSlotChangeNotify(
                        oldQuickUseId,
                        WIDGET_SLOT_TAG_QUICK_USE,
                        WIDGET_SLOT_OP_DETACH,
                        false
                ));
            }

            player.setWidgetId(0);

            session.send(new PacketSetWidgetSlotRsp(
                    materialId,
                    op,
                    List.of(WIDGET_SLOT_TAG_QUICK_USE)
            ));

            sendCurrentWidgetState(session, player);

            Grasscutter.getLogger().info(
                    "[WIDGET DEBUG] Detached quick-use widget materialId={}",
                    materialId
            );
            return;
        }

        if (oldQuickUseId > 0 && oldQuickUseId != materialId) {
            session.send(new PacketWidgetSlotChangeNotify(
                    oldQuickUseId,
                    WIDGET_SLOT_TAG_QUICK_USE,
                    WIDGET_SLOT_OP_DETACH,
                    false
            ));
        }

        player.setWidgetId(materialId);

        session.send(new PacketWidgetSlotChangeNotify(
                materialId,
                WIDGET_SLOT_TAG_QUICK_USE,
                WIDGET_SLOT_OP_ATTACH,
                true
        ));

        session.send(new PacketSetWidgetSlotRsp(
                materialId,
                op,
                List.of(WIDGET_SLOT_TAG_QUICK_USE)
        ));

        sendCurrentWidgetState(session, player);

        Grasscutter.getLogger().info(
                "[WIDGET DEBUG] Attached quick-use widget materialId={}",
                materialId
        );
    }

    private void sendCurrentWidgetState(GameSession session, Player player) {
        int quickUseId = player.getWidgetId();

        if (isCompanionWidget(quickUseId)) {
            Grasscutter.getLogger().warn(
                    "[WIDGET DEBUG] Clearing companion materialId={} from current quick-use state",
                    quickUseId
            );

            player.setWidgetId(0);
            quickUseId = 0;
        }

        session.send(new PacketGetWidgetSlotRsp(player));
        session.send(new PacketGetWidgetQuickSlotListRsp(player));

        Grasscutter.getLogger().info(
                "[WIDGET DEBUG] Re-sent widget state quickUseMaterialId={}",
                quickUseId
        );
    }

    private boolean isCompanionWidget(int materialId) {
        return COMPANION_WIDGET_IDS.contains(materialId);
    }
}