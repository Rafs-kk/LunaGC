package emu.grasscutter.server.packet.send;

import com.google.protobuf.CodedOutputStream;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;

import java.io.ByteArrayOutputStream;

public class PacketWidgetSlotChangeNotify extends BasePacket {

    public PacketWidgetSlotChangeNotify(int materialId, int slotTag, int op, boolean active) {
        super(PacketOpcodes.WidgetSlotChangeNotify);

        try {
            ByteArrayOutputStream slotBaos = new ByteArrayOutputStream();
            CodedOutputStream slotOutput = CodedOutputStream.newInstance(slotBaos);

            // REL6.0 WidgetSlotData:
            // message MOFKDLOMBNB {
            //     uint32 cd_over_time = 1;
            //     uint32 material_id = 5;
            //     WidgetSlotTag tag = 10;
            //     bool is_active = 13;
            // }

            if (materialId > 0) {
                slotOutput.writeUInt32(5, materialId);
            }

            slotOutput.writeEnum(10, slotTag);
            slotOutput.writeBool(13, active);
            slotOutput.flush();

            byte[] slotData = slotBaos.toByteArray();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CodedOutputStream output = CodedOutputStream.newInstance(baos);

            // REL6.0 obfuscated proto:
            // CmdID: 1465
            // message GACKMBBPJPK {
            //     WidgetSlotData slot = 2;
            //     WidgetSlotOp op = 10;
            // }

            output.writeByteArray(2, slotData);
            output.writeEnum(10, op);

            output.flush();
            this.setData(baos.toByteArray());
        } catch (Exception e) {
            Grasscutter.getLogger().error("Failed to build WidgetSlotChangeNotify payload", e);
        }
    }
}