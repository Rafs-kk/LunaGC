package emu.grasscutter.server.packet.recv;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.AbilityInvokeEntryOuterClass.AbilityInvokeEntry;
import emu.grasscutter.net.proto.AbilityMetaSetKilledStateOuterClass.AbilityMetaSetKilledState;
import emu.grasscutter.net.proto.ClientAbilityInitFinishNotifyOuterClass.ClientAbilityInitFinishNotify;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.ClientAbilityInitFinishNotify)
public class HandlerClientAbilityInitFinishNotify extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        ClientAbilityInitFinishNotify notif = ClientAbilityInitFinishNotify.parseFrom(payload);
        Player player = session.getPlayer();

        player.getAbilityManager().onSkillEnd(player);

        boolean hasForwardedEntries = false;

        for (AbilityInvokeEntry entry : notif.getInvokesList()) {
            if (this.shouldSuppressAliveMonsterKillState(player, entry)) {
                continue;
            }

            player.getAbilityManager().onAbilityInvoke(entry);
            player.getClientAbilityInitFinishHandler().addEntry(entry.getForwardType(), entry);
            hasForwardedEntries = true;
        }

        if (hasForwardedEntries) {
            player.getClientAbilityInitFinishHandler().update(player);
        }
    }

    private boolean shouldSuppressAliveMonsterKillState(Player player, AbilityInvokeEntry entry)
            throws Exception {
        if (!"ABILITY_INVOKE_ARGUMENT_META_SET_KILLED_SETATE"
                .equals(entry.getArgumentType().name())) {
            return false;
        }

        GameEntity entity = player.getScene().getEntityById(entry.getEntityId());
        if (!(entity instanceof EntityMonster monster)) {
            return false;
        }

        AbilityMetaSetKilledState killState =
                AbilityMetaSetKilledState.parseFrom(entry.getAbilityData());

        if (!killState.getKilled()) {
            return false;
        }

        float curHp = monster.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);

		if (curHp <= 0f) {
			return false;
		}

		EntityMonster replacement = player.getScene().resetMonsterAtBornPosition(monster);

		Grasscutter.getLogger()
                .debug(
                        "Suppressed forwarded false monster kill-state and reset monster: oldEntityId={}, newEntityId={}, monsterId={}, hp={}, oldPos={}, bornPos={}",
						monster.getId(),
						replacement != null ? replacement.getId() : 0,
						monster.getMonsterData().getId(),
						curHp,
						monster.getPosition(),
						monster.getBornPos());

		return true;
    }
}