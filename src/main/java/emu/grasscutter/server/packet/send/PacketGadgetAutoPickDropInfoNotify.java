package emu.grasscutter.server.packet.send;

import com.google.protobuf.CodedOutputStream;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.*;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

public class PacketGadgetAutoPickDropInfoNotify extends BasePacket {

    public PacketGadgetAutoPickDropInfoNotify(Collection<GameItem> items) {
        super(PacketOpcodes.GadgetAutoPickDropInfoNotify);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CodedOutputStream output = CodedOutputStream.newInstance(baos);

            for (GameItem item : items) {
                // REL6.0 obfuscated proto:
				// CmdID: 22337
				// message BIJMANKLGIK {
				//     repeated Item item_list = 4;
				// }
                output.writeMessage(4, item.toProto());
            }

            output.flush();
            byte[] data = baos.toByteArray();
			/*
            Grasscutter.getLogger().info(
                    "[REWARD UI TEST] Sending GadgetAutoPickDropInfoNotify candidate: opcode={}, field=4, itemCount={}, payloadLen={}",
                    PacketOpcodes.GadgetAutoPickDropInfoNotify,
                    items.size(),
                    data.length
            );
			*/
            this.setData(data);
        } catch (Exception e) {
            Grasscutter.getLogger().error("Failed to build GadgetAutoPickDropInfoNotify payload", e);
        }
    }
}