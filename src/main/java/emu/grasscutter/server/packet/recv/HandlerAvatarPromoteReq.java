package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AvatarPromoteReqOuterClass.AvatarPromoteReq;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.AvatarPromoteReq)
public class HandlerAvatarPromoteReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        AvatarPromoteReq req = AvatarPromoteReq.parseFrom(payload);

        long guid = req.getGuid();

        // client appears to send avatar guid as unknown field 4.
        var guidField = req.getUnknownFields().getField(4);
        if (guid == 0 && guidField != null && !guidField.getVarintList().isEmpty()) {
            guid = guidField.getVarintList().get(0);
        }
		/*
        Grasscutter.getLogger().info(
                "[AVATAR PROMOTE DECODED] guid={}, raw={}, unknown={}",
                guid,
                req,
                req.getUnknownFields()
        );
		*/
        session.getServer().getInventorySystem().promoteAvatar(session.getPlayer(), guid);
    }
}