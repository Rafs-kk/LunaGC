package emu.grasscutter.game.entity.gadget;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.*;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.GadgetInteractReqOuterClass.GadgetInteractReq;
import emu.grasscutter.net.proto.GatherGadgetInfoOuterClass.GatherGadgetInfo;
import emu.grasscutter.net.proto.InteractTypeOuterClass.InteractType;
import emu.grasscutter.net.proto.SceneGadgetInfoOuterClass.SceneGadgetInfo;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketGadgetInteractRsp;
import emu.grasscutter.utils.Utils;

public final class GadgetGatherObject extends GadgetContent {
    private int itemId;
	private int breakDurability;
    private boolean isForbidGuest;
	private boolean initDisableInteract;

	public GadgetGatherObject(EntityGadget gadget) {
		super(gadget);

		GatherData gatherData = resolveGatherData(gadget);

		if (gatherData != null) {
			this.itemId = gatherData.getItemId();
			this.isForbidGuest = gatherData.isForbidGuest();
			this.initDisableInteract = gatherData.initDisableInteract();
			this.breakDurability = chooseBreakDurability(gatherData);

			// Stabilize pointType for later logic.
			gadget.setPointType(gatherData.getId());
		}

		// Spawn entries may override the gathered item ID,
		// but they should not skip initDisableInteract/isForbidGuest.
		if (gadget.getSpawnEntry() != null && gadget.getSpawnEntry().getGatherItemId() > 0) {
			this.itemId = gadget.getSpawnEntry().getGatherItemId();
		}
		
		/*
		Grasscutter.getLogger().info(
				"[GATHER DEBUG] gadgetId={}, pointType={}, spawnItem={}, resolvedItem={}, requiresBreaking={}",
				gadget.getGadgetId(),
				gadget.getPointType(),
				gadget.getSpawnEntry() != null ? gadget.getSpawnEntry().getGatherItemId() : 0,
				this.itemId,
				this.initDisableInteract
		);
		*/
		
		if (this.itemId <= 0) {
			Grasscutter.getLogger().trace("invalid gather object: {}", gadget.getConfigId());
		}
	}

	public int getBreakDurability() {
		if (!this.requiresBreaking()) {
			return 0;
		}

		return this.breakDurability > 0 ? this.breakDurability : 3;
	}

	private int chooseBreakDurability(GatherData gatherData) {
		if (!gatherData.initDisableInteract()) {
			return 0;
		}

		int itemId = gatherData.getItemId();

		// Common ore/mineral item range.
		// Covers normal ores and most later regional ore-like gatherables.
		if (itemId >= 101001 && itemId < 102000) {
			return 3;
		}

		// Local-specialty mineral nodes, such as Cor Lapis / Noctilucous Jade-style nodes.
		return 2;
	}

	private GatherData resolveGatherData(EntityGadget gadget) {
		GatherData gatherData = GameData.getGatherDataMap().get(gadget.getPointType());
		if (gatherData != null) {
			return gatherData;
		}

		int wantedItemId = 0;
		if (gadget.getSpawnEntry() != null) {
			wantedItemId = gadget.getSpawnEntry().getGatherItemId();
		}

		GatherData fallbackByGadgetId = null;

		for (GatherData data : GameData.getGatherDataMap().values()) {
			if (data.getGadgetId() != gadget.getGadgetId()) {
				continue;
			}

			if (wantedItemId <= 0 || data.getItemId() == wantedItemId) {
				return data;
			}

			fallbackByGadgetId = data;
		}

		if (fallbackByGadgetId != null) {
			return fallbackByGadgetId;
		}

		if (wantedItemId > 0) {
			for (GatherData data : GameData.getGatherDataMap().values()) {
				if (data.getItemId() == wantedItemId && data.initDisableInteract()) {
					return data;
				}
			}
		}

		return null;
	}

    public int getItemId() {
        return this.itemId;
    }

    public boolean isForbidGuest() {
        return isForbidGuest;
    }
	
	public boolean requiresBreaking() {
		return this.initDisableInteract;
	}

    public boolean onInteract(Player player, GadgetInteractReq req) {
		if (this.requiresBreaking()) {
			return false;
		}
        // Sanity check
        ItemData itemData = GameData.getItemDataMap().get(getItemId());
        if (itemData == null) {
            return false;
        }

        GameItem item = new GameItem(itemData, 1);
        player.getInventory().addItem(item, ActionReason.Gather);

        var ScriptArgs =
                new ScriptArgs(getGadget().getGroupId(), EventType.EVENT_GATHER, getGadget().getConfigId());
        if (getGadget().getMetaGadget() != null) {
            ScriptArgs.setEventSource(getGadget().getMetaGadget().config_id);
        }
        getGadget().getScene().getScriptManager().callEvent(ScriptArgs);

        getGadget()
                .getScene()
                .broadcastPacket(
                        new PacketGadgetInteractRsp(getGadget(), InteractType.INTERACT_TYPE_GATHER));

        return true;
    }

    public void onBuildProto(SceneGadgetInfo.Builder gadgetInfo) {
		GatherGadgetInfo gatherGadgetInfo =
				GatherGadgetInfo.newBuilder()
						.setItemId(this.getItemId())
						.setIsForbidGuest(this.isForbidGuest())
						.build();

		gadgetInfo.setGatherGadget(gatherGadgetInfo);

		if (this.requiresBreaking()) {
			gadgetInfo.setIsEnableInteract(false);
		}
	}

    public void dropItems(Player player) {
		dropItems(player, true);
	}

	public void dropItems(Player player, boolean killGadget) {
		Scene scene = getGadget().getScene();
		int times = Utils.randomRange(1, 2);

		ItemData itemData = GameData.getItemDataMap().get(itemId);
		if (itemData == null) {
			return;
		}

		for (int i = 0; i < times; i++) {
			EntityItem item =
					new EntityItem(
							scene,
							player,
							itemData,
							getGadget().getPosition().nearby2d(1f).addY(2f),
							1,
							true);

			scene.addEntity(item);
		}

		if (killGadget) {
			scene.killEntity(this.getGadget(), player.getTeamManager().getCurrentAvatarEntity().getId());
		}
	}
}
