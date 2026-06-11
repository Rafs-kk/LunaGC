package emu.grasscutter.server.packet.send;

import com.google.protobuf.CodedOutputStream;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class PacketSetWidgetSlotRsp extends BasePacket {

    public PacketSetWidgetSlotRsp(int materialId, int op, List<Integer> tagList) {
        super(PacketOpcodes.SetWidgetSlotRsp);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            CodedOutputStream output = CodedOutputStream.newInstance(baos);

            // REL6.0 obfuscated proto:
            // CmdID: 28587
            // message PILCCHJHNGC {
            //     WidgetSlotOp op = 1;
            //     int32 retcode = 2;
            //     uint32 material_id = 11;
            //     repeated WidgetSlotTag tag_list = 15;
            // }

            output.writeEnum(1, op);
            output.writeInt32(2, 0);

            if (materialId > 0) {
                output.writeUInt32(11, materialId);
            }

            if (tagList != null) {
                for (int tag : tagList) {
                    output.writeEnum(15, tag);
                }
            }

            output.flush();
            this.setData(baos.toByteArray());
        } catch (Exception e) {
            Grasscutter.getLogger().error("Failed to build SetWidgetSlotRsp payload", e);
        }
    }
}