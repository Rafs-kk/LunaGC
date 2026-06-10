package emu.grasscutter.server.packet.recv;

import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.UseItemReqOuterClass.UseItemReq;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.server.packet.send.PacketUseItemRsp;
import emu.grasscutter.Grasscutter;

@Opcodes(PacketOpcodes.UseItemReq)
public class HandlerUseItemReq extends PacketHandler {

    @Override
	public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
		UseItemReq req = UseItemReq.parseFrom(payload);

		int count = req.getCount() > 0 ? req.getCount() : 1;

		// Fallback: older/normal option_idx field.
		// In current 6.0 selector-box tests this stays 0.
		int selectedOptionIdx = req.getOptionIdx();

		// REL6.0 selectable boxes appear to send the real selected option here,
		// using a 1-based flattened option index.
		if (req.getAKCOAFJNBMICount() > 0) {
			selectedOptionIdx = req.getAKCOAFJNBMI(0) - 1;
		}
		/*
		Grasscutter.getLogger().info(
				"[USE ITEM DEBUG] guid={}, targetGuid={}, count={}, optionIdx={}, rawSelectList={}, normalizedOptionIdx={}, enterMpDungeonTeam={}, payloadLen={}",
				req.getGuid(),
				req.getTargetGuid(),
				count,
				req.getOptionIdx(),
				req.getAKCOAFJNBMIList(),
				selectedOptionIdx,
				req.getIsEnterMpDungeonTeam(),
				payload == null ? 0 : payload.length
		);
		*/
		GameItem useItem =
				session
						.getServer()
						.getInventorySystem()
						.useItem(
								session.getPlayer(),
								req.getTargetGuid(),
								req.getGuid(),
								count,
								selectedOptionIdx,
								req.getIsEnterMpDungeonTeam());
		/*
		Grasscutter.getLogger().info(
				"[USE ITEM DEBUG] result={}",
				useItem != null ? "SUCCESS itemId=" + useItem.getItemId() : "FAILED"
		);
		*/
		if (useItem != null) {
			session.send(new PacketUseItemRsp(req.getTargetGuid(), useItem));
		} else {
			session.send(new PacketUseItemRsp());
		}
	}
}
