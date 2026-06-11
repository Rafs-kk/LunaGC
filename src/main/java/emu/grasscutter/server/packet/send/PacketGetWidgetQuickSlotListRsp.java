package emu.grasscutter.server.packet.send;

import com.google.protobuf.CodedOutputStream;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.*;

import java.io.ByteArrayOutputStream;
import java.util.Set;

public class PacketGetWidgetQuickSlotListRsp extends BasePacket {

    private static final int WIDGET_SLOT_TAG_QUICK_USE = 0;

    // Companion/pet-like gadgets that should not be reported as normal quick-use gadgets.
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

    public PacketGetWidgetQuickSlotListRsp(Player player) {
        super(PacketOpcodes.GetWidgetQuickSlotListRsp);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CodedOutputStream output = CodedOutputStream.newInstance(baos);

            int quickUseMaterialId = player.getWidgetId();

            if (isCompanionWidget(quickUseMaterialId)) {
                Grasscutter.getLogger().warn(
                        "[WIDGET QUICKLIST DEBUG] Clearing companion materialId={} from quick-use slot",
                        quickUseMaterialId
                );

                player.setWidgetId(0);
                quickUseMaterialId = 0;
            }

            if (quickUseMaterialId > 0) {
                byte[] slotData = buildWidgetSlotData(
                        quickUseMaterialId,
                        WIDGET_SLOT_TAG_QUICK_USE,
                        true
                );

                // REL6.0:
                // CmdID: 22601
                // message FCBJONFDPFM {
                //     repeated WidgetSlotData slot_list = 12;
                // }
                output.writeByteArray(12, slotData);
            }

            output.flush();
            this.setData(baos.toByteArray());

            Grasscutter.getLogger().info(
                    "[WIDGET QUICKLIST DEBUG] quickUseMaterialId={}, payloadLen={}",
                    quickUseMaterialId,
                    baos.size()
            );
        } catch (Exception e) {
            Grasscutter.getLogger().error("Failed to build GetWidgetQuickSlotListRsp payload", e);
        }
    }

    private byte[] buildWidgetSlotData(int materialId, int slotTag, boolean active) throws Exception {
        ByteArrayOutputStream slotBaos = new ByteArrayOutputStream();
        CodedOutputStream slotOutput = CodedOutputStream.newInstance(slotBaos);

        // REL6.0 WidgetSlotData:
        // message MOFKDLOMBNB {
        //     uint32 cd_over_time = 1;
        //     uint32 material_id = 5;
        //     WidgetSlotTag tag = 10;
        //     bool is_active = 13;
        // }

        slotOutput.writeUInt32(5, materialId);
        slotOutput.writeEnum(10, slotTag);
        slotOutput.writeBool(13, active);

        slotOutput.flush();
        return slotBaos.toByteArray();
    }

    private boolean isCompanionWidget(int materialId) {
        return COMPANION_WIDGET_IDS.contains(materialId);
    }
}