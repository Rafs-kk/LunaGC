package emu.grasscutter.server.packet.send;

import emu.grasscutter.GameConstants;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.packet.PacketOpcodes;
import emu.grasscutter.net.proto.AvatarTeamAllDataNotifyOuterClass.AvatarTeamAllDataNotify;

public class PacketAvatarTeamAllDataNotify extends BasePacket {
    public PacketAvatarTeamAllDataNotify(Player player) {
        super(PacketOpcodes.AvatarTeamAllDataNotify);

        player.getTeamManager().sanitizeAvatarTeams();

        var teamManager = player.getTeamManager();

        AvatarTeamAllDataNotify.Builder proto =
                AvatarTeamAllDataNotify.newBuilder()
                        .setCurAvatarTeamId(teamManager.getCurrentTeamId());

        teamManager
                .getTeams()
                .forEach(
                        (id, teamInfo) -> {
                            proto.putAvatarTeamMap(id, teamInfo.toProto(player));

                            if (id > GameConstants.DEFAULT_TEAMS) {
                                proto.addBackupAvatarTeamOrderList(id);
                            }
                        });

        // Important REL6.0 safety:
        // Do not send tempAvatarGuidList for normal saved teams.
        // When this list is sent during normal login, the 6.0 client can treat the active party as a temporary/hidden party instead of matching it to avatarTeamMap/curAvatarTeamId.
        if (teamManager.isUsingTrialTeam() || teamManager.isUsingTemporaryTeam()) {
            teamManager
                    .getActiveTeam()
                    .forEach(entity -> proto.addTempAvatarGuidList(entity.getAvatar().getGuid()));
        }

        this.setData(proto);
    }
}
