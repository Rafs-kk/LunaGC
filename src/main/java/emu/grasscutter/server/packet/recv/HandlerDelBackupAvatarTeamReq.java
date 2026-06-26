package emu.grasscutter.server.packet.recv;

import emu.grasscutter.net.packet.*;
import emu.grasscutter.net.proto.DelBackupAvatarTeamReqOuterClass.DelBackupAvatarTeamReq;
import emu.grasscutter.server.game.GameSession;

@Opcodes(PacketOpcodes.DelBackupAvatarTeamReq)
public class HandlerDelBackupAvatarTeamReq extends PacketHandler {
    @Override
    public void handle(GameSession session, byte[] header, byte[] payload) throws Exception {
        DelBackupAvatarTeamReq req = DelBackupAvatarTeamReq.parseFrom(payload);

        int teamId = req.getBackupAvatarTeamId();

        if (teamId <= 0) {
            teamId = decodeRel60BackupAvatarTeamId(payload);
        }

        if (teamId <= 0) {
            return;
        }

        session.getPlayer().getTeamManager().removeCustomTeam(teamId);
    }

    private int decodeRel60BackupAvatarTeamId(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return 0;
        }

        int index = 0;

        while (index < payload.length) {
            VarintResult tagResult = readVarint(payload, index);
            if (tagResult == null) {
                return 0;
            }

            index = tagResult.nextIndex;

            int tag = tagResult.value;
            int fieldNumber = tag >>> 3;
            int wireType = tag & 0x07;

            if ((fieldNumber == 3 || fieldNumber == 9) && wireType == 0) {
                VarintResult valueResult = readVarint(payload, index);
                if (valueResult == null) {
                    return 0;
                }

                return valueResult.value;
            }

            index = skipField(payload, index, wireType);
            if (index < 0) {
                return 0;
            }
        }

        return 0;
    }

    private VarintResult readVarint(byte[] data, int startIndex) {
        int result = 0;
        int shift = 0;
        int index = startIndex;

        while (index < data.length && shift < 32) {
            int b = data[index++] & 0xFF;
            result |= (b & 0x7F) << shift;

            if ((b & 0x80) == 0) {
                return new VarintResult(result, index);
            }

            shift += 7;
        }

        return null;
    }

    private int skipField(byte[] data, int index, int wireType) {
        switch (wireType) {
            case 0 -> {
                VarintResult skipped = readVarint(data, index);
                return skipped != null ? skipped.nextIndex : -1;
            }
            case 1 -> {
                return index + 8 <= data.length ? index + 8 : -1;
            }
            case 2 -> {
                VarintResult length = readVarint(data, index);
                if (length == null) {
                    return -1;
                }

                int next = length.nextIndex + length.value;
                return next <= data.length ? next : -1;
            }
            case 5 -> {
                return index + 4 <= data.length ? index + 4 : -1;
            }
            default -> {
                return -1;
            }
        }
    }

    private static class VarintResult {
        private final int value;
        private final int nextIndex;

        private VarintResult(int value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }
    }
}
