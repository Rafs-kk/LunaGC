package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AvatarUpgradeReqOuterClass.AvatarUpgradeReq;
import emu.grasscutter.net.proto.ItemParamOuterClass.ItemParam;
import emu.grasscutter.server.game.GameSession;
import java.util.ArrayList;
import java.util.List;

@Opcodes(PacketOpcodes.AvatarUpgradeReq)
public class HandlerAvatarUpgradeReq extends PacketHandler {

    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        AvatarUpgradeReq req = AvatarUpgradeReq.parseFrom(payload);

        long avatarGuid = req.getAvatarGuid();
        List<ItemParam> itemParamList = new ArrayList<>(req.getItemParamListList());

        // client appears to send avatarGuid as unknown field 4.
        var guidField = req.getUnknownFields().getField(4);
        if (avatarGuid == 0 && guidField != null && !guidField.getVarintList().isEmpty()) {
            avatarGuid = guidField.getVarintList().get(0);
        }

        // client appears to send EXP material params as repeated unknown field 6.
        var itemParamField = req.getUnknownFields().getField(6);
        if (itemParamList.isEmpty() && itemParamField != null) {
            for (var bytes : itemParamField.getLengthDelimitedList()) {
                itemParamList.add(ItemParam.parseFrom(bytes));
            }
        }
		/*
        Grasscutter.getLogger().info(
                "[AVATAR UPGRADE DECODED] guid={}, itemParamList={}, raw={}, unknown={}",
                avatarGuid,
                itemParamList,
                req,
                req.getUnknownFields()
        );
		*/
        session
                .getServer()
                .getInventorySystem()
                .upgradeAvatar(session.getPlayer(), avatarGuid, itemParamList);
    }
}