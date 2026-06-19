package emu.grasscutter.game.world;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.GameDepot;
import emu.grasscutter.data.binout.SceneNpcBornEntry;
import emu.grasscutter.data.binout.routes.Route;
import emu.grasscutter.data.excels.ItemData;
import emu.grasscutter.data.excels.codex.CodexAnimalData;
import emu.grasscutter.data.excels.monster.MonsterData;
import emu.grasscutter.data.excels.scene.SceneData;
import emu.grasscutter.data.excels.world.WorldLevelData;
import emu.grasscutter.data.server.Grid;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.dungeons.DungeonManager;
import emu.grasscutter.game.dungeons.DungeonSettleListener;
import emu.grasscutter.game.dungeons.challenge.WorldChallenge;
import emu.grasscutter.game.dungeons.enums.DungeonPassConditionType;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.entity.gadget.GadgetWorktop;
import emu.grasscutter.game.entity.gadget.GadgetGatherObject;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.managers.blossom.BlossomManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.player.TeamInfo;
import emu.grasscutter.game.props.*;
import emu.grasscutter.game.quest.QuestGroupSuite;
import emu.grasscutter.game.world.data.TeleportProperties;
import emu.grasscutter.net.packet.BasePacket;
import emu.grasscutter.net.proto.*;
import emu.grasscutter.net.proto.AttackResultOuterClass.AttackResult;
import emu.grasscutter.net.proto.VisionTypeOuterClass.VisionType;
import emu.grasscutter.scripts.SceneIndexManager;
import emu.grasscutter.scripts.SceneScriptManager;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneBlock;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.event.entity.EntityCreationEvent;
import emu.grasscutter.server.event.player.PlayerTeleportEvent;
import emu.grasscutter.server.packet.send.*;
import emu.grasscutter.server.scheduler.ServerTaskScheduler;
import emu.grasscutter.utils.algorithms.KahnsSort;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.*;
import emu.grasscutter.game.props.ClimateType;
import emu.grasscutter.game.props.EnterReason;
import emu.grasscutter.net.proto.EnterTypeOuterClass;
import emu.grasscutter.server.packet.send.PacketScenePlayerLocationNotify;

public class Scene {
    @Getter private final World world;
    @Getter private final SceneData sceneData;
    @Getter private final List<Player> players;
    @Getter private final Map<Integer, GameEntity> entities;
    @Getter private final Map<Integer, GameEntity> weaponEntities;
    @Getter private final Set<SpawnDataEntry> spawnedEntities;
    @Getter private final Set<SpawnDataEntry> deadSpawnedEntities;
    @Getter private final Set<SceneBlock> loadedBlocks;
    @Getter private final Set<SceneGroup> loadedGroups;
    @Getter private final BlossomManager blossomManager;
    private final HashSet<Integer> unlockedForces;
    private final long startWorldTime;
    @Getter @Setter DungeonManager dungeonManager;
    @Getter Int2ObjectMap<Route> sceneRoutes;
    private Set<SpawnDataEntry.GridBlockId> loadedGridBlocks;
	private Set<SpawnDataEntry.GridBlockId> loadedMissingScriptGridBlocks;
    @Getter @Setter private boolean dontDestroyWhenEmpty;
    @Getter private final SceneScriptManager scriptManager;
    @Getter @Setter private WorldChallenge challenge;
    @Getter private List<DungeonSettleListener> dungeonSettleListeners;
    @Getter @Setter private int prevScene; // Id of the previous scene
    @Getter @Setter private int prevScenePoint;
    @Getter @Setter private int killedMonsterCount;
    private Set<SceneNpcBornEntry> npcBornEntrySet;
    @Getter private boolean finishedLoading = false;
    @Getter protected int tickCount = 0;
    @Getter private boolean isPaused = false;
	private static final int ICEWIND_SCENE_ID = 3;
	private static final int ICEWIND_GROUP_ID = 133402002;
	private static final int ICEWIND_TALK_CONFIG_ID = 2004;
	private static final int ICEWIND_PROP_CONFIG_ID = 2006;
	private static final int ICEWIND_PROP_GADGET_ID = 70330531;
	private static final int ICEWIND_BLOCK_ID = 334;
	private static final Position ICEWIND_PROP_POS = new Position(3603.317f, 438.115f, 3814.537f);
	private static final Position ICEWIND_PROP_ROT = new Position(0f, 221.7f, 0f);
	private static final Set<Integer> ICEWIND_FALLBACK_MONSTER_IDS = Set.of(24070101, 24070102, 24070201, 24070202, 24070301);
	
	private static final int ICEWIND_FALLBACK_REFRESH_SECONDS = 24;
	private static final int ICEWIND_FALLBACK_LOW_HP_REFRESH_SECONDS = 8;
	private static final float ICEWIND_FALLBACK_CLIMAX_HP_RATIO = 0.715f;
	
	private static final int ICEWIND_WEATHER_ID = 5009;
	private static final int ICEWIND_DEFAULT_WEATHER_ID = 0;

	private static final Position ICEWIND_PLAYER_START_POS =
			new Position(3588.0f, 438.2f, 3804.0f);

	private static final Position ICEWIND_PLAYER_START_ROT =
			new Position(0f, 45f, 0f);
			
	private static final int PMA_ROUTE_BARRIER_SCENE_ID = 3;
	private static final int PMA_ROUTE_BARRIER_GROUP_ID = 133220374;
	private static final int PMA_ROUTE_BARRIER_CONFIG_A = 374001;
	private static final int PMA_ROUTE_BARRIER_CONFIG_B = 374002;
	private static final int PMA_ROUTE_BARRIER_GADGET_A = 70290155;
	private static final int PMA_ROUTE_BARRIER_GADGET_B = 70290156;

	private boolean icewindSuiteFallbackWeatherActive = false;
	
	private static final float ICEWIND_FALLBACK_SAFE_HP_RATIO = 0.80f;
	private static final float ICEWIND_FALLBACK_MIN_DISPLAY_HP_RATIO = 0.78f;

	private final Map<Integer, Float> icewindFallbackVirtualHp = new ConcurrentHashMap<>();
	private final Map<Integer, Float> icewindFallbackVirtualMaxHp = new ConcurrentHashMap<>();
	
	private final Map<Integer, Long> pendingIcewindSuiteArenaTeleports = new ConcurrentHashMap<>();
	private final Map<Integer, Integer> icewindFallbackSpawnTimes = new ConcurrentHashMap<>();
	private final Map<Integer, Float> icewindFallbackLastHpRatios = new ConcurrentHashMap<>();

    private final List<Runnable> afterLoadedCallbacks = new ArrayList<>();
    private final List<Runnable> afterHostInitCallbacks = new ArrayList<>();

    @Getter private GameEntity sceneEntity;
    @Getter private final ServerTaskScheduler scheduler;

    public Scene(World world, SceneData sceneData) {
        this.world = world;
        this.sceneData = sceneData;
        this.players = new CopyOnWriteArrayList<>();
        this.entities = new ConcurrentHashMap<>();
        this.weaponEntities = new ConcurrentHashMap<>();

        this.prevScene = 3;
        this.sceneRoutes = GameData.getSceneRoutes(getId());

        this.startWorldTime = world.getWorldTime();

        this.spawnedEntities = ConcurrentHashMap.newKeySet();
        this.deadSpawnedEntities = ConcurrentHashMap.newKeySet();
        this.loadedBlocks = ConcurrentHashMap.newKeySet();
        this.loadedGroups = ConcurrentHashMap.newKeySet();
        this.loadedGridBlocks = new HashSet<>();
		this.loadedMissingScriptGridBlocks = new HashSet<>();
        this.npcBornEntrySet = ConcurrentHashMap.newKeySet();
        this.scriptManager = new SceneScriptManager(this);
        this.blossomManager = new BlossomManager(this);
        this.unlockedForces = new HashSet<>();
        this.sceneEntity = new EntityScene(this);
        this.scheduler = new ServerTaskScheduler();
    }

    public int getId() {
        return sceneData.getId();
    }

    public SceneType getSceneType() {
        return getSceneData().getSceneType();
    }

    public int getPlayerCount() {
        return this.getPlayers().size();
    }

    /**
     * @return The scene's world's host.
     */
    public Player getHost() {
        return this.getWorld().getHost();
    }

    public GameEntity getEntityById(int id) {
        // Check if the scene's entity ID is referenced.
        if (id == 0x13800001) return this.sceneEntity;
        else if (id == this.getWorld().getLevelEntityId()) return this.getWorld().getEntity();

        var teamEntityPlayer =
                players.stream().filter(p -> p.getTeamManager().getEntity().getId() == id).findAny();
        if (teamEntityPlayer.isPresent()) return teamEntityPlayer.get().getTeamManager().getEntity();

        // Check for an avatar.
        var entity = this.entities.get(id);
        if (entity == null) entity = this.weaponEntities.get(id);
        if (entity == null && (id >> 24) == EntityIdType.AVATAR.getId()) {
            for (var player : getPlayers()) {
                for (var avatar : player.getTeamManager().getActiveTeam()) {
                    if (avatar.getId() == id) return avatar;
                }
            }
        }

        // Check for a weapon.
        if (entity == null && (id >> 24) == EntityIdType.WEAPON.getId()) {
            for (var player : this.getPlayers()) {
                for (var avatar : player.getTeamManager().getActiveTeam()) {
                    if (avatar.getWeaponEntityId() == id) return avatar;
                }
            }
        }

        return entity;
    }

    public GameEntity getFirstEntityByConfigId(int configId) {
        return this.entities.values().stream()
                .filter(x -> x.getConfigId() == configId)
                .findFirst()
                .orElse(null);
    }

    public GameEntity getEntityByConfigId(int configId, int groupId) {
        return this.entities.values().stream()
                .filter(x -> x.getConfigId() == configId && x.getGroupId() == groupId)
                .findFirst()
                .orElse(null);
    }

    @Nullable public Route getSceneRouteById(int routeId) {
        return sceneRoutes.get(routeId);
    }

    /**
     * Sets the scene's pause state. Sends the current scene's time to all players.
     *
     * @param paused The new pause state.
     */
    public void setPaused(boolean paused) {
        if (this.isPaused != paused) {
            this.isPaused = paused;
            this.broadcastPacket(new PacketSceneTimeNotify(this));
        }
    }

    /**
     * Gets the time in seconds since the scene started.
     *
     * @return The time in seconds since the scene started.
     */
    public int getSceneTime() {
        return (int) (this.getWorld().getWorldTime() - this.startWorldTime);
    }

    /**
     * Gets {@link Scene#getSceneTime()} in seconds.
     *
     * @return The time in seconds since the scene started.
     */
    public int getSceneTimeSeconds() {
        return this.getSceneTime() / 1000;
    }

    public void addDungeonSettleObserver(DungeonSettleListener dungeonSettleListener) {
        if (dungeonSettleListeners == null) {
            dungeonSettleListeners = new ArrayList<>();
        }

        dungeonSettleListeners.add(dungeonSettleListener);
    }

    /**
     * Triggers an event in the dungeon manager.
     *
     * @param conditionType The condition type to trigger.
     * @param params The parameters to pass to the event.
     */
    public void triggerDungeonEvent(DungeonPassConditionType conditionType, int... params) {
        if (this.dungeonManager == null) return;
        this.dungeonManager.triggerEvent(conditionType, params);
    }

    public boolean isInScene(GameEntity entity) {
        return this.entities.containsKey(entity.getId());
    }

    public synchronized void addPlayer(Player player) {
        // Check if player already in
        if (getPlayers().contains(player)) {
            return;
        }

        // Remove player from prev scene
        if (player.getScene() != null) {
            player.getScene().removePlayer(player);
        }

        // Add
        getPlayers().add(player);
        player.setSceneId(this.getId());
        player.setScene(this);

        this.setupPlayerAvatars(player);
    }

    public synchronized void removePlayer(Player player) {
		if (this.getId() == ICEWIND_SCENE_ID && this.icewindSuiteFallbackWeatherActive) {
			this.resetIcewindSuiteFallbackWeather(player);
		}
        // Remove from challenge if leaving
        if (this.getChallenge() != null && this.getChallenge().inProgress()) {
            player.sendPacket(new PacketDungeonChallengeFinishNotify(this.getChallenge()));
        }

        // Remove player from scene
        getPlayers().remove(player);
        player.setScene(null);

        // Remove player avatars
        this.removePlayerAvatars(player);

        // Remove player gadgets
        for (EntityBaseGadget gadget : player.getTeamManager().getGadgets()) {
            this.removeEntity(gadget);
        }

        // Remove player widget gadgets
        this.getEntities().values().stream()
                .filter(gameEntity -> gameEntity instanceof EntityVehicle)
                .map(gameEntity -> (EntityVehicle) gameEntity)
                .filter(entityVehicle -> entityVehicle.getOwner().equals(player))
                .forEach(entityVehicle -> this.removeEntity(entityVehicle, VisionType.VISION_TYPE_REMOVE));

        // Deregister scene if not in use
        if (this.getPlayerCount() <= 0 && !this.dontDestroyWhenEmpty) {
            this.getScriptManager().onDestroy();
            this.getWorld().deregisterScene(this);
        }

        this.saveGroups();
    }

    private void setupPlayerAvatars(Player player) {
        // Clear entities from old team
        player.getTeamManager().getActiveTeam().clear();

        // Add new entities for player
        TeamInfo teamInfo = player.getTeamManager().getCurrentTeamInfo();
        for (int avatarId : teamInfo.getAvatars()) {
            Avatar avatar = player.getAvatars().getAvatarById(avatarId);
            if (avatar == null) {
                if (player.getTeamManager().isUsingTrialTeam()) {
                    avatar = player.getTeamManager().getTrialAvatars().get(avatarId);
                }
                if (avatar == null) continue;
            }
            player
                    .getTeamManager()
                    .getActiveTeam()
                    .add(
                            EntityCreationEvent.call(
                                    EntityAvatar.class,
                                    new Class<?>[] {Scene.class, Avatar.class},
                                    new Object[] {player.getScene(), avatar}));
        }

        // Limit character index in case its out of bounds
        if (player.getTeamManager().getCurrentCharacterIndex()
                        >= player.getTeamManager().getActiveTeam().size()
                || player.getTeamManager().getCurrentCharacterIndex() < 0) {
            player
                    .getTeamManager()
                    .setCurrentCharacterIndex(player.getTeamManager().getCurrentCharacterIndex() - 1);
        }
    }

    private synchronized void removePlayerAvatars(Player player) {
        var team = player.getTeamManager().getActiveTeam();
        // removeEntities(team, VisionType.VISION_TYPE_REMOVE);  // List<SubType> isn't cool apparently
        // :(
        team.forEach(e -> removeEntity(e, VisionType.VISION_TYPE_REMOVE));
        team.clear();
    }

    public void spawnPlayer(Player player) {
        var teamManager = player.getTeamManager();
        if (this.isInScene(teamManager.getCurrentAvatarEntity())) {
            return;
        }

        if (teamManager.getCurrentAvatarEntity().getFightProperty(FightProperty.FIGHT_PROP_CUR_HP)
                <= 0f) {
            teamManager.getCurrentAvatarEntity().setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 1f);
        }

        this.addEntity(teamManager.getCurrentAvatarEntity());

        // Notify the client of any extra skill charges
        teamManager.getActiveTeam().stream()
                .map(EntityAvatar::getAvatar)
                .forEach(Avatar::sendSkillExtraChargeMap);
    }

    private void addEntityDirectly(GameEntity entity) {
        getEntities().put(entity.getId(), entity);
        entity.onCreate(); // Call entity create event
    }
	
	public synchronized EntityMonster resetMonsterAtBornPosition(EntityMonster monster) {
        if (monster == null || !this.getEntities().containsKey(monster.getId())) {
            return null;
        }

        Position resetPos =
                monster.getSpawnEntry() != null ? monster.getSpawnEntry().getPos() : monster.getBornPos();
        Position resetRot =
                monster.getSpawnEntry() != null ? monster.getSpawnEntry().getRot() : monster.getRotation();

        EntityMonster replacement =
                new EntityMonster(
                        this,
                        monster.getMonsterData(),
                        resetPos,
                        resetRot,
                        monster.getLevel());

        replacement.setGroupId(monster.getGroupId());
        replacement.setConfigId(monster.getConfigId());
        replacement.setBlockId(monster.getBlockId());
        replacement.setCampId(monster.getCampId());
        replacement.setCampType(monster.getCampType());
        replacement.setPoseId(monster.getPoseId());
        replacement.setAiId(monster.getAiId());
        replacement.setOwnerEntityId(monster.getOwnerEntityId());
        replacement.setSummonedTag(monster.getSummonedTag());
        replacement.setSpawnEntry(monster.getSpawnEntry());
        replacement.setMetaMonster(monster.getMetaMonster());
        replacement.setEntityController(monster.getEntityController());

        if (monster.getWeaponEntity() != null) {
            this.getWeaponEntities().remove(monster.getWeaponEntity().getId());
        }

        // Remove the client-broken entity without treating it as a death, then spawn a fresh
        // monster at its original/home position.
        this.removeEntity(monster, VisionType.VISION_TYPE_REMOVE);
        this.addEntities(List.of(replacement), VisionType.VISION_TYPE_BORN);

        Grasscutter.getLogger()
                .debug(
                        "Reset false-dead/leashing monster at born position: oldEntityId={}, newEntityId={}, monsterId={}, oldPos={}, resetPos={}, groupId={}, configId={}",
                        monster.getId(),
                        replacement.getId(),
                        monster.getMonsterData().getId(),
                        monster.getPosition(),
                        replacement.getPosition(),
                        replacement.getGroupId(),
                        replacement.getConfigId());

        return replacement;
    }

    public synchronized void addEntity(GameEntity entity) {
		if (this.isBlockedPmaRouteBarrierEntity(entity)) {
			return;
		}
        this.addEntityDirectly(entity);
        this.broadcastPacket(new PacketSceneEntityAppearNotify(entity));
    }

    public synchronized void addEntityToSingleClient(Player player, GameEntity entity) {
        this.addEntityDirectly(entity);
        player.sendPacket(new PacketSceneEntityAppearNotify(entity));
    }

    public void addDropEntity(GameItem item, GameEntity bornForm, Player player, boolean share) {
        // TODO:optimize EntityItem.java. Maybe we should make other players can't see
        // the ItemEntity.
        ItemData itemData = GameData.getItemDataMap().get(item.getItemId());
        if (itemData == null) return;
        if (itemData.isEquip()) {
            float range = (1.5f + (.05f * item.getCount()));
            for (int j = 0; j < item.getCount(); j++) {
                Position pos = bornForm.getPosition().nearby2d(range).addY(0.5f);
                EntityItem entity = new EntityItem(this, player, itemData, pos, item.getCount(), share);
                addEntity(entity);
            }
        } else {
            EntityItem entity =
                    new EntityItem(
                            this,
                            player,
                            itemData,
                            bornForm.getPosition().clone().addY(0.5f),
                            item.getCount(),
                            share);
            addEntity(entity);
        }
    }

    public void addEntities(Collection<? extends GameEntity> entities) {
        addEntities(entities, VisionType.VISION_TYPE_BORN);
    }

    public void updateEntity(GameEntity entity) {
        this.broadcastPacket(new PacketSceneEntityUpdateNotify(entity));
    }

    public void updateEntity(GameEntity entity, VisionType type) {
        this.broadcastPacket(new PacketSceneEntityUpdateNotify(Arrays.asList(entity), type));
    }

    private static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + L))));
        }
        return parts;
    }

    public synchronized void addEntities(
			Collection<? extends GameEntity> entities, VisionType visionType) {
		if (entities == null || entities.isEmpty()) {
			return;
		}

		var filteredEntities =
				entities.stream()
						.filter(entity -> !this.isBlockedPmaRouteBarrierEntity(entity))
						.toList();

		if (filteredEntities.isEmpty()) {
			return;
		}

		for (var entity : filteredEntities) {
			this.addEntityDirectly(entity);
		}

		for (var l : chopped(new ArrayList<>(filteredEntities), 100)) {
			this.broadcastPacket(new PacketSceneEntityAppearNotify(l, visionType));
		}
	}

    private GameEntity removeEntityDirectly(GameEntity entity) {
        var removed = getEntities().remove(entity.getId());
        if (removed != null) {
            removed.onRemoved(); // Call entity remove event
        }
        return removed;
    }

    public void removeEntity(GameEntity entity) {
        this.removeEntity(entity, VisionType.VISION_TYPE_DIE);
    }

    public synchronized void removeEntity(GameEntity entity, VisionType visionType) {
        GameEntity removed = this.removeEntityDirectly(entity);
        if (removed != null) {
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(removed, visionType));
        }
    }

    public void removeEntities(List<GameEntity> entity, VisionType visionType) {
        var toRemove =
                entity.stream()
                        .filter(Objects::nonNull)
                        .map(this::removeEntityDirectly)
                        .filter(Objects::nonNull)
                        .toList();
        if (!toRemove.isEmpty()) {
            this.broadcastPacket(new PacketSceneEntityDisappearNotify(toRemove, visionType));
        }
    }

    public synchronized void replaceEntity(EntityAvatar oldEntity, EntityAvatar newEntity) {
        this.removeEntityDirectly(oldEntity);
        this.addEntityDirectly(newEntity);
        this.broadcastPacket(
                new PacketSceneEntityDisappearNotify(oldEntity, VisionType.VISION_TYPE_REPLACE));
        this.broadcastPacket(
                new PacketSceneEntityAppearNotify(
                        newEntity, VisionType.VISION_TYPE_REPLACE, oldEntity.getId()));
    }

    public void showOtherEntities(Player player) {
        GameEntity currentEntity = player.getTeamManager().getCurrentAvatarEntity();
        List<GameEntity> entities =
                this.getEntities().values().stream()
                        .filter(entity -> entity != currentEntity)
                        .filter(
                                gameEntity ->
                                        !(gameEntity instanceof Rebornable rebornable) || !rebornable.isInCD())
                        .toList();

        player.sendPacket(new PacketSceneEntityAppearNotify(entities, VisionType.VISION_TYPE_MEET));
    }

    public void handleAttack(AttackResult result) {
        // GameEntity attacker = getEntityById(result.getAttackerId());
        GameEntity target = getEntityById(result.getDefenseId());
        ElementType attackType = ElementType.getTypeByValue(result.getElementType());

        if (target == null) {
            return;
        }

        // Godmode check
        if (target instanceof EntityAvatar) {
            if (((EntityAvatar) target).getPlayer().isInGodMode()) {
                return;
            }
        }
		
		if (target instanceof EntityMonster monster && this.isIcewindFallbackMonster(monster)) {
			if (this.handleIcewindSuiteVirtualDamage(monster, result.getDamage(), result.getAttackerId())) {
				return;
			}
		}
		
        // Sanity check
        target.damage(result.getDamage(), result.getAttackerId(), attackType);
    }

    public void killEntity(GameEntity target) {
        killEntity(target, 0);
    }

    public void killEntity(GameEntity target, int attackerId) {
		
		
		
        GameEntity attacker = null;

        if (attackerId > 0) {
            attacker = getEntityById(attackerId);
        }

        if (attacker != null) {
            // Check codex
            if (attacker instanceof EntityClientGadget gadgetAttacker) {
                var clientGadgetOwner = getEntityById(gadgetAttacker.getOwnerEntityId());
                if (clientGadgetOwner instanceof EntityAvatar) {
                    ((EntityClientGadget) attacker)
                            .getOwner()
                            .getCodex()
                            .checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
                }
            } else if (attacker instanceof EntityAvatar avatarAttacker) {
                avatarAttacker
                        .getPlayer()
                        .getCodex()
                        .checkAnimal(target, CodexAnimalData.CountType.CODEX_COUNT_TYPE_KILL);
            }
        }

        // Packet
        this.broadcastPacket(new PacketLifeStateChangeNotify(attackerId, target, LifeState.LIFE_DEAD));

        // Reward drop
		var world = this.getWorld();
		if (target instanceof EntityMonster monster && this.getSceneType() != SceneType.SCENE_DUNGEON) {
			boolean handled = false;

			var legacyDrops = world.getServer().getDropSystemLegacy().getDropData();

			if (monster.getMetaMonster() == null
					&& (monster.getSpawnEntry() != null || this.isIcewindFallbackMonster(monster))
					&& legacyDrops.containsKey(monster.getMonsterData().getId())) {
				world.getServer().getDropSystemLegacy().callDrop(monster);
				handled = true;
			}

			if (!handled && !world.getServer().getDropSystem().handleMonsterDrop(monster)) {
				if (monster.getMetaMonster() != null) {
					Grasscutter.getLogger()
							.debug(
									"Can not solve monster drop: drop_id = {}, drop_tag = {}. Falling back to legacy drop system.",
									monster.getMetaMonster().drop_id,
									monster.getMetaMonster().drop_tag);
				} else {
					Grasscutter.getLogger()
							.debug(
									"Can not solve static monster drop: monster_id = {}, kill_drop_id = {}. Falling back to legacy drop system.",
									monster.getMonsterData().getId(),
									monster.getMonsterData().getKillDropId());
				}

				world.getServer().getDropSystemLegacy().callDrop(monster);
			}
		}
		
		if (target instanceof EntityGadget gadget
				&& gadget.getContent() instanceof GadgetGatherObject gatherObject
				&& attackerId > 0
				&& gatherObject.requiresBreaking()) {

			Player dropOwner = this.getWorld().getHost();

			if (attacker instanceof EntityAvatar avatarAttacker) {
				dropOwner = avatarAttacker.getPlayer();
			} else if (attacker instanceof EntityClientGadget clientGadgetAttacker) {
				dropOwner = clientGadgetAttacker.getOwner();
			}

			gatherObject.dropItems(dropOwner, false);

		} else if (target instanceof EntityGadget gadget) {
			if (gadget.getMetaGadget() != null) {
				world
						.getServer()
						.getDropSystem()
						.handleChestDrop(
								gadget.getMetaGadget().drop_id,
								gadget.getMetaGadget().drop_count,
								gadget);
			}
		}

        // Remove entity from world
        this.removeEntity(target);

        // Death event
        target.onDeath(attackerId);
        this.triggerDungeonEvent(
                DungeonPassConditionType.DUNGEON_COND_KILL_MONSTER_COUNT, ++killedMonsterCount);
    }

    public void onTick() {
        // Disable ticking for the player's home world.
        if (this.getSceneType() == SceneType.SCENE_HOME_WORLD
                || this.getSceneType() == SceneType.SCENE_HOME_ROOM) {
            this.finishLoading();
            return;
        }

        if (!isPaused) {
            this.getScheduler().runTasks();
        }

        if (this.getScriptManager().isInit()) {
            // this.checkBlocks();
            this.checkGroups();
            this.checkLegacySpawnsForMissingScriptGroups();
        } else {
            // TEMPORARY
            this.checkSpawns();
        }

        // Triggers
        this.scriptManager.checkRegions();

        if (challenge != null) {
            challenge.onCheckTimeOut();
        }

        var sceneTime = getSceneTimeSeconds();

        var entities = Map.copyOf(this.getEntities());
        entities.forEach(
                (eid, e) -> {
                    if (!e.isAlive()) {
                        this.getEntities().remove(eid);
                    } else {
                        e.onTick(sceneTime);
                    }
                });

        blossomManager.onTick();

        // Should be OK to check only player 0,
        // as no other players could enter Tower
        var towerManager = getPlayers().get(0).getTowerManager();
        if (towerManager != null && towerManager.isInProgress()) {
            towerManager.onTick();
        }

        this.checkNpcGroup();
		this.processPendingIcewindSuiteArenaTeleports();
		
		if (this.tickCount % 20 == 0) {
			this.checkIcewindSuiteFallbackAntiStall(sceneTime);
			this.checkIcewindSuiteFallbackReset();
		}

        this.finishLoading();
        this.checkPlayerRespawn();
        if (this.tickCount++ % 10 == 0) this.broadcastPacket(new PacketSceneTimeNotify(this));
    }

    /** Validates a player's current position. Teleports the player if the player is out of bounds. */
    protected void checkPlayerRespawn() {
        if (this.getScriptManager().getConfig() == null) return;
        var diePos = this.getScriptManager().getConfig().die_y;

        // Check players in the scene.
        this.players.forEach(
                player -> {
                    if (this.getScriptManager().getConfig() == null) return;

                    // Check if we need a respawn
                    if (diePos >= player.getPosition().getY()) {
                        // Respawn the player.
                        this.respawnPlayer(player);
                    }
                });

        // Check entities in the scene.
        this.getEntities()
                .forEach(
                        (id, entity) -> {
                            if (diePos >= entity.getPosition().getY()) {
                                this.killEntity(entity);
                            }
                        });
    }

    /**
     * @return The script's default location, or the player's location.
     */
    public Position getDefaultLocation(Player player) {
        val defaultPosition = getScriptManager().getConfig().born_pos;
        return defaultPosition != null ? defaultPosition : player.getPosition();
    }

    /**
     * @return The script's default rotation, or the player's rotation.
     */
    public Position getDefaultRotation(Player player) {
        var defaultRotation = this.getScriptManager().getConfig().born_rot;
        return defaultRotation != null ? defaultRotation : player.getRotation();
    }

    /**
     * Gets the respawn position for the player.
     *
     * @param player The player to get the respawn position for.
     * @return The respawn position for the player.
     */
    private Position getRespawnLocation(Player player) {
        // TODO: Get the last valid location the player stood on.
        var lastCheckpointPos = dungeonManager != null ? dungeonManager.getRespawnLocation() : null;
        return lastCheckpointPos != null ? lastCheckpointPos : getDefaultLocation(player);
    }

    /**
     * Gets the respawn rotation for the player.
     *
     * @param player The player to get the respawn rotation for.
     * @return The respawn rotation for the player.
     */
    private Position getRespawnRotation(Player player) {
        var lastCheckpointRot =
                this.dungeonManager != null ? this.dungeonManager.getRespawnRotation() : null;
        return lastCheckpointRot != null ? lastCheckpointRot : this.getDefaultRotation(player);
    }

    /**
     * Teleports the player to the respawn location.
     *
     * @param player The player to respawn.
     * @return true if the player was successfully respawned, false otherwise.
     */
    public boolean respawnPlayer(Player player) {
        // Apply void damage as a penalty.
        player.getTeamManager().applyVoidDamage();

        // TODO: Respawn the player at the last valid location.
        var targetPos = getRespawnLocation(player);
        var targetRot = getRespawnRotation(player);
        var teleportProps =
                TeleportProperties.builder()
                        .sceneId(getId())
                        .teleportTo(targetPos)
                        .teleportRot(targetRot)
                        .teleportType(PlayerTeleportEvent.TeleportType.INTERNAL)
                        .enterType(EnterTypeOuterClass.EnterType.ENTER_TYPE_GOTO)
                        .enterReason(
                                dungeonManager != null ? EnterReason.DungeonReviveOnWaypoint : EnterReason.Revival);

        return this.getWorld().transferPlayerToScene(player, teleportProps.build());
    }

    /**
     * Invoked when the scene finishes loading. Runs all callbacks that were added with {@link
     * #runWhenFinished(Runnable)}.
     */
    public void finishLoading() {
        if (this.finishedLoading) return;

        this.finishedLoading = true;
        this.afterLoadedCallbacks.forEach(Runnable::run);
        this.afterLoadedCallbacks.clear();
    }

    /**
     * Adds a callback to be executed when the scene is finished loading. If the scene is already
     * finished loading, the callback will be executed immediately.
     *
     * @param runnable The callback to be executed.
     */
    public void runWhenFinished(Runnable runnable) {
        if (this.isFinishedLoading()) {
            runnable.run();
            return;
        }

        this.afterLoadedCallbacks.add(runnable);
    }

    /**
     * Invoked when a player initializes loading the scene.
     *
     * @param player The player that initialized loading the scene.
     */
    public void playerSceneInitialized(Player player) {
        // Check if the player is the host.
        if (!player.equals(this.getHost())) return;

        // Run all callbacks.
        this.afterHostInitCallbacks.forEach(Runnable::run);
        this.afterHostInitCallbacks.clear();
    }

    /**
     * Run a callback when the host initializes loading the scene.
     *
     * @param runnable The callback to be executed.
     */
    public void runWhenHostInitialized(Runnable runnable) {
        if (this.isFinishedLoading()) {
            runnable.run();
            return;
        }

        this.afterHostInitCallbacks.add(runnable);
    }

    public int getEntityLevel(int baseLevel, int worldLevelOverride) {
        int level = worldLevelOverride > 0 ? worldLevelOverride + baseLevel - 22 : baseLevel;
        level = Math.min(level, 100);
        level = level <= 0 ? 1 : level;

        return level;
    }

    public int getLevelForMonster(int configId, int defaultLevel) {
        if (getDungeonManager() != null) {
            return getDungeonManager().getLevelForMonster(configId);
        } else if (getWorld().getWorldLevel() > 0) {
            var worldLevelData = GameData.getWorldLevelDataMap().get(getWorld().getWorldLevel());

            if (worldLevelData != null) {
                return worldLevelData.getMonsterLevel();
            }
        }
        return defaultLevel;
    }

    public void checkNpcGroup() {
        Set<SceneNpcBornEntry> npcBornEntries = ConcurrentHashMap.newKeySet();
        for (Player player : this.getPlayers()) {
            npcBornEntries.addAll(loadNpcForPlayer(player));
        }

        this.npcBornEntrySet = npcBornEntries;
    }

	public void checkSpawns() {
		this.checkSpawns(false);
	}

	private void checkLegacySpawnsForMissingScriptGroups() {
		this.checkSpawns(true);
	}

	private void checkSpawns(boolean missingScriptOnly) {
		Set<SpawnDataEntry.GridBlockId> loadedGridBlocks = new HashSet<>();
		for (Player player : this.getPlayers()) {
			Collections.addAll(
					loadedGridBlocks,
					SpawnDataEntry.GridBlockId.getAdjacentGridBlockIds(
							player.getSceneId(), player.getPosition()));
		}

		Set<SpawnDataEntry.GridBlockId> previousLoadedGridBlocks =
				missingScriptOnly ? this.loadedMissingScriptGridBlocks : this.loadedGridBlocks;

		if (previousLoadedGridBlocks.containsAll(
				loadedGridBlocks)) { // Don't recalculate static spawns if nothing has changed
			return;
		}

		if (missingScriptOnly) {
			this.loadedMissingScriptGridBlocks = loadedGridBlocks;
		} else {
			this.loadedGridBlocks = loadedGridBlocks;
		}

		var spawnLists = GameDepot.getSpawnLists();
		Set<SpawnDataEntry> visible = new HashSet<>();
		for (var block : loadedGridBlocks) {
			var spawns = spawnLists.get(block);
			if (spawns != null) {
				visible.addAll(spawns);
			}
		}

		if (missingScriptOnly) {
			visible.removeIf(entry -> !this.shouldUseLegacyFallbackSpawn(entry));
		}
		visible.removeIf(this::isBlockedPmaRouteBarrierSpawn);

		// World level
		WorldLevelData worldLevelData = GameData.getWorldLevelDataMap().get(getWorld().getWorldLevel());
		int worldLevelOverride = 0;

		if (worldLevelData != null) {
			worldLevelOverride = worldLevelData.getMonsterLevel();
		}

		// Todo
		List<GameEntity> toAdd = new ArrayList<>();
		List<GameEntity> toRemove = new ArrayList<>();
		var spawnedEntities = this.getSpawnedEntities();
		for (SpawnDataEntry entry : visible) {
			// If spawn entry is in our view and hasnt been spawned/killed yet, we should spawn it
			if (!spawnedEntities.contains(entry) && !this.getDeadSpawnedEntities().contains(entry)) {
				// Entity object holder
				GameEntity entity = null;

				// Check if spawn entry is monster or gadget
				if (entry.getMonsterId() > 0) {
					MonsterData data = GameData.getMonsterDataMap().get(entry.getMonsterId());
					if (data == null) continue;

					int level = this.getEntityLevel(entry.getLevel(), worldLevelOverride);

					EntityMonster monster =
							new EntityMonster(this, data, entry.getPos(), entry.getRot(), level);
					monster.setGroupId(entry.getGroup().getGroupId());
					monster.setPoseId(entry.getPoseId());
					monster.setConfigId(entry.getConfigId());
					monster.setSpawnEntry(entry);

					entity = monster;
				} else if (entry.getGadgetId() > 0) {
					EntityGadget gadget =
							new EntityGadget(this, entry.getGadgetId(), entry.getPos(), entry.getRot());
					gadget.setGroupId(entry.getGroup().getGroupId());
					gadget.setConfigId(entry.getConfigId());
					gadget.setSpawnEntry(entry);
					int state = entry.getGadgetState();
					if (state > 0) {
						gadget.setState(state);
					}
					gadget.buildContent();

					if (gadget.getContent() instanceof GadgetGatherObject gatherObject
							&& !gatherObject.requiresBreaking()) {
						gadget.setFightProperty(FightProperty.FIGHT_PROP_BASE_HP, Float.POSITIVE_INFINITY);
						gadget.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, Float.POSITIVE_INFINITY);
						gadget.setFightProperty(FightProperty.FIGHT_PROP_MAX_HP, Float.POSITIVE_INFINITY);
					}

					entity = gadget;
					blossomManager.initBlossom(gadget);
				}

				if (entity == null) continue;

				// Add to scene and spawned list
				toAdd.add(entity);
				spawnedEntities.add(entry);
			}
		}

		for (GameEntity entity : this.getEntities().values()) {
			var spawnEntry = entity.getSpawnEntry();
			if (spawnEntry != null
					&& !(entity instanceof EntityWeapon)
					&& (!missingScriptOnly || this.isMissingScriptSpawn(spawnEntry))
					&& !visible.contains(spawnEntry)) {
				toRemove.add(entity);
				spawnedEntities.remove(spawnEntry);
			}
		}

		if (toAdd.size() > 0) {
			var filteredToAdd =
					toAdd.stream()
							.filter(entity -> !this.isBlockedPmaRouteBarrierEntity(entity))
							.toList();

			if (!filteredToAdd.isEmpty()) {
				filteredToAdd.forEach(this::addEntityDirectly);
				this.broadcastPacket(
						new PacketSceneEntityAppearNotify(filteredToAdd, VisionType.VISION_TYPE_BORN));
			}
		}

		if (toRemove.size() > 0) {
			toRemove.forEach(this::removeEntityDirectly);
			this.broadcastPacket(
					new PacketSceneEntityDisappearNotify(toRemove, VisionType.VISION_TYPE_REMOVE));
			blossomManager.recycleGadgetEntity(toRemove);
		}
	}
	
	private boolean shouldUseLegacyFallbackSpawn(SpawnDataEntry entry) {
		// Monsters still use the original missing-script-group rule.
		if (entry.getMonsterId() > 0) {
			return this.isMissingScriptSpawn(entry);
		}

		// Gadgets from GadgetSpawns.json do not always have reliable script block/group metadata, especially in newer/partial regions.
		// So allow the static fallback, but only if the script path has not already spawned an equivalent gadget nearby.
		if (entry.getGadgetId() > 0) {
			return this.isMissingScriptSpawn(entry) && !this.hasEquivalentScriptGadget(entry);
		}

		return false;
	}
	
	private boolean isBlockedPmaRouteBarrierSpawn(SpawnDataEntry entry) {
		if (entry == null || entry.getGroup() == null) {
			return false;
		}

		if (this.getId() != PMA_ROUTE_BARRIER_SCENE_ID) {
			return false;
		}

		if (entry.getGroup().getGroupId() != PMA_ROUTE_BARRIER_GROUP_ID) {
			return false;
		}

		return (entry.getConfigId() == PMA_ROUTE_BARRIER_CONFIG_A
						&& entry.getGadgetId() == PMA_ROUTE_BARRIER_GADGET_A)
				|| (entry.getConfigId() == PMA_ROUTE_BARRIER_CONFIG_B
						&& entry.getGadgetId() == PMA_ROUTE_BARRIER_GADGET_B);
	}
	
	private boolean isBlockedPmaRouteBarrierEntity(GameEntity entity) {
		if (this.getId() != PMA_ROUTE_BARRIER_SCENE_ID) {
			return false;
		}

		if (!(entity instanceof EntityGadget gadget)) {
			return false;
		}

		if (gadget.getGroupId() != PMA_ROUTE_BARRIER_GROUP_ID) {
			return false;
		}

		return (gadget.getConfigId() == PMA_ROUTE_BARRIER_CONFIG_A
						&& gadget.getGadgetId() == PMA_ROUTE_BARRIER_GADGET_A)
				|| (gadget.getConfigId() == PMA_ROUTE_BARRIER_CONFIG_B
						&& gadget.getGadgetId() == PMA_ROUTE_BARRIER_GADGET_B);
	}

	private boolean hasEquivalentScriptGadget(SpawnDataEntry entry) {
		if (entry.getGadgetId() <= 0 || entry.getPos() == null) {
			return false;
		}

		for (GameEntity entity : this.getEntities().values()) {
			if (!(entity instanceof EntityGadget gadget)) {
				continue;
			}

			// Only compare against script/runtime-created gadgets.
			// Legacy fallback gadgets have a SpawnDataEntry, so ignore those to avoid the fallback blocking itself.
			if (gadget.getSpawnEntry() != null) {
				continue;
			}

			if (gadget.getGadgetId() != entry.getGadgetId()) {
				continue;
			}

			if (isNearSameSpawnPoint(gadget.getPosition(), entry.getPos())) {
				return true;
			}
		}

		return false;
	}

	private boolean isNearSameSpawnPoint(Position a, Position b) {
		float dx = a.getX() - b.getX();
		float dy = a.getY() - b.getY();
		float dz = a.getZ() - b.getZ();

		// Strict enough to catch true duplicate objects, but loose enough for tiny coordinate differences between script and static data.
		return dx * dx + dz * dz <= 4.0f && Math.abs(dy) <= 5.0f;
	}

	private boolean isMissingScriptSpawn(SpawnDataEntry entry) {
		if (!this.getScriptManager().isInit()) {
			return true;
		}

		var spawnGroup = entry.getGroup();
		if (spawnGroup == null) {
			return false;
		}

		var scriptBlocks = this.getScriptManager().getBlocks();
		if (scriptBlocks == null) {
			return false;
		}

		var scriptBlock = scriptBlocks.get(spawnGroup.getBlockId());
		if (scriptBlock == null) {
			return true;
		}

		if (scriptBlock.groups == null) {
			this.getScriptManager().loadBlockFromScript(scriptBlock);
		}

		return scriptBlock.groups == null || !scriptBlock.groups.containsKey(spawnGroup.getGroupId());
	}

    public List<SceneBlock> getPlayerActiveBlocks(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        return SceneIndexManager.queryNeighbors(
                getScriptManager().getBlocksIndex(),
                player.getPosition().toXZDoubleArray(),
                Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange);
    }

    public Set<Integer> getPlayerActiveGroups(Player player) {
        // consider the borders' entities of blocks, so we check if contains by index
        Position playerPosition = player.getPosition();
        Set<Integer> activeGroups = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            Grid grid = getScriptManager().getGroupGrids().get(i);

            activeGroups.addAll(grid.getNearbyGroups(i, playerPosition));
        }

        return activeGroups;
    }

    public boolean loadBlock(SceneBlock block) {
        if (this.loadedBlocks.contains(block)) return false;

        this.onLoadBlock(block, this.players);
        this.loadedBlocks.add(block);
        return true;
    }

    public void checkGroups() {
        Set<Integer> visible =
                this.players.stream()
                        .map(this::getPlayerActiveGroups)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet());

        for (var group : this.loadedGroups) {
            if (!visible.contains(group.id) && !group.dynamic_load && !group.dontUnload)
                unloadGroup(scriptManager.getBlocks().get(group.block_id), group.id);
        }

        var toLoad =
                visible.stream()
                        .filter(g -> this.loadedGroups.stream().noneMatch(gr -> gr.id == g))
                        .map(
                                g -> {
                                    for (var b : scriptManager.getBlocks().values()) {
                                        loadBlock(b);
                                        SceneGroup group = b.groups.getOrDefault(g, null);
                                        if (group != null && !group.dynamic_load) return group;
                                    }

                                    return null;
                                })
                        .filter(Objects::nonNull)
                        .toList();

        this.onLoadGroup(toLoad);
        if (!toLoad.isEmpty()) this.onRegisterGroups();
    }

    public void onLoadBlock(SceneBlock block, List<Player> players) {
        this.getScriptManager().loadBlockFromScript(block);
        scriptManager.getLoadedGroupSetPerBlock().put(block.id, new HashSet<>());

        Grasscutter.getLogger().trace("Scene {} block {} loaded.", this.getId(), block.id);
    }

    public int loadDynamicGroup(int group_id) {
        SceneGroup group = getScriptManager().getGroupById(group_id);
        if (group == null) return -1; // Group not found

        this.onLoadGroup(new ArrayList<>(List.of(group)));

        if (GameData.getGroupReplacements().containsKey(group_id)) onRegisterGroups();

        if (group.init_config == null) return -1;
        return group.init_config.suite;
    }

    public boolean unregisterDynamicGroup(int groupId) {
        var group = getScriptManager().getGroupById(groupId);
        if (group == null) return false;

        var block = getScriptManager().getBlocks().get(group.block_id);
        this.unloadGroup(block, groupId);
        return true;
    }

    public void onRegisterGroups() {
        var sceneGroups = this.loadedGroups;
        var sceneGroupMap =
                sceneGroups.stream().collect(Collectors.toMap(item -> item.id, item -> item));
        var sceneGroupsIds = sceneGroups.stream().map(group -> group.id).toList();
        var dynamicGroups =
                sceneGroups.stream().filter(group -> group.dynamic_load).map(group -> group.id).toList();

        // Create the graph
        var nodes = new ArrayList<KahnsSort.Node>();
        var groupList = new ArrayList<Integer>();
        GameData.getGroupReplacements().values().stream()
                .filter(replacement -> dynamicGroups.contains(replacement.id))
                .forEach(
                        replacement -> {
                            Grasscutter.getLogger().debug("Graph ordering replacement {}", replacement);
                            replacement.replace_groups.forEach(
                                    group -> {
                                        nodes.add(new KahnsSort.Node(replacement.id, group));
                                        if (!groupList.contains(group)) groupList.add(group);
                                    });

                            if (!groupList.contains(replacement.id)) groupList.add(replacement.id);
                        });

        KahnsSort.Graph graph = new KahnsSort.Graph(nodes, groupList);
        List<Integer> dynamicGroupsOrdered = KahnsSort.doSort(graph);

        // Now we can start unloading and loading groups :D
        dynamicGroupsOrdered.forEach(
                group -> {
                    if (GameData.getGroupReplacements().containsKey((int) group)) { // isGroupJoinReplacement
                        var data = GameData.getGroupReplacements().get((int) group);
                        var sceneGroupReplacement =
                                this.loadedGroups.stream().filter(g -> g.id == group).findFirst().orElseThrow();
                        if (sceneGroupReplacement.is_replaceable != null) {
                            var it = data.replace_groups.iterator();
                            while (it.hasNext()) {
                                var replace_group = it.next();
                                if (!sceneGroupsIds.contains(replace_group)) continue;

                                // Check if we can replace this group
                                SceneGroup sceneGroup = sceneGroupMap.get(replace_group);
                                if (sceneGroup != null
                                        && sceneGroup.is_replaceable != null
                                        && ((sceneGroup.is_replaceable.value
                                                        && sceneGroup.is_replaceable.version
                                                                <= sceneGroupReplacement.is_replaceable.version)
                                                || sceneGroup.is_replaceable.new_bin_only)) {
                                    this.unloadGroup(
                                            scriptManager.getBlocks().get(sceneGroup.block_id), replace_group);
                                    it.remove();
                                    Grasscutter.getLogger().debug("Graph ordering: unloaded {}", replace_group);
                                }
                            }
                        }
                    }
                });
    }

    public void loadTriggerFromGroup(SceneGroup group, String triggerName) {
        // Load triggers and regions
        this.getScriptManager()
                .registerTrigger(
                        group.triggers.values().stream()
                                .filter(p -> p.getName().contains(triggerName))
                                .toList());
        group.regions.values().stream()
                .filter(q -> q.config_id == Integer.parseInt(triggerName.substring(13)))
                .map(region -> new EntityRegion(this, region))
                .forEach(getScriptManager()::registerRegion);
    }

    public void onLoadGroup(List<SceneGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            return;
        }

        for (var group : groups) {
            if (this.loadedGroups.contains(group)) continue;

            // We load the script files for the groups here
            this.getScriptManager().loadGroupFromScript(group);
            if (!this.scriptManager.getLoadedGroupSetPerBlock().containsKey(group.block_id))
                this.onLoadBlock(scriptManager.getBlocks().get(group.block_id), players);
            this.scriptManager.getLoadedGroupSetPerBlock().get(group.block_id).add(group);
        }

        // Spawn gadgets AFTER triggers are added
        // TODO
        var entities = new ArrayList<GameEntity>();
        for (var group : groups) {
            if (this.loadedGroups.contains(group)) continue;

            if (group.init_config == null) {
                continue;
            }

            var groupInstance = this.getScriptManager().getGroupInstanceById(group.id);
            var cachedInstance = this.getScriptManager().getCachedGroupInstanceById(group.id);
            if (cachedInstance != null) {
                cachedInstance.setLuaGroup(group);
                groupInstance = cachedInstance;
            }

            // Load suites
            // int suite = group.findInitSuiteIndex(0);
            this.getScriptManager()
                    .refreshGroup(groupInstance, 0, false); // This is what the official server does

            this.loadedGroups.add(group);
        }

        this.scriptManager.meetEntities(entities);
        groups.forEach(
                g -> scriptManager.callEvent(new ScriptArgs(g.id, EventType.EVENT_GROUP_LOAD, g.id)));

        Grasscutter.getLogger().trace("Scene {} loaded {} group(s)", this.getId(), groups.size());
    }

    public void unloadGroup(SceneBlock block, int group_id) {
        List<GameEntity> toRemove =
                this.getEntities().values().stream()
                        .filter(e -> e != null && (e.getBlockId() == block.id && e.getGroupId() == group_id))
                        .toList();

        if (toRemove.size() > 0) {
            toRemove.forEach(this::removeEntityDirectly);
            this.broadcastPacket(
                    new PacketSceneEntityDisappearNotify(toRemove, VisionType.VISION_TYPE_REMOVE));
        }

        var group = block.groups.get(group_id);
        if (group.triggers != null) {
            group.triggers.values().forEach(getScriptManager()::deregisterTrigger);
        }
        if (group.regions != null) {
            group.regions.values().forEach(getScriptManager()::deregisterRegion);
        }
        if (challenge != null && group.id == challenge.getGroup().id) {
            challenge.fail();
        }

        scriptManager.getLoadedGroupSetPerBlock().get(block.id).remove(group);
        this.loadedGroups.remove(group);

        if (this.scriptManager.getLoadedGroupSetPerBlock().get(block.id).isEmpty()) {
            this.scriptManager.getLoadedGroupSetPerBlock().remove(block.id);
            Grasscutter.getLogger().trace("Scene {} block {} is unloaded.", this.getId(), block.id);
        }

        this.broadcastPacket(new PacketGroupUnloadNotify(List.of(group_id)));
        this.scriptManager.unregisterGroup(group);
    }

    // Gadgets

    public void onPlayerCreateGadget(EntityClientGadget gadget) {
        // Directly add
        this.addEntityDirectly(gadget);

        // Add to owner's gadget list
        gadget.getOwner().getTeamManager().getGadgets().add(gadget);

        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == gadget.getOwner()) {
            return;
        }

        this.broadcastPacketToOthers(gadget.getOwner(), new PacketSceneEntityAppearNotify(gadget));
    }

    public void onPlayerDestroyGadget(int entityId) {
        GameEntity entity = getEntities().get(entityId);

        if (!(entity instanceof EntityClientGadget gadget)) {
            return;
        }

        // Get and remove entity
        this.removeEntityDirectly(gadget);

        // Remove from owner's gadget list
        gadget.getOwner().getTeamManager().getGadgets().remove(gadget);

        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == gadget.getOwner()) {
            return;
        }

        this.broadcastPacketToOthers(
                gadget.getOwner(),
                new PacketSceneEntityDisappearNotify(gadget, VisionType.VISION_TYPE_DIE));
    }

    // Broadcasting

    public void broadcastPacket(BasePacket packet) {
        // Send to all players - might have to check if player has been sent data packets
        for (Player player : this.getPlayers()) {
            player.getSession().send(packet);
        }
    }

    public void broadcastPacketToOthers(Player excludedPlayer, BasePacket packet) {
        // Optimization
        if (this.getPlayerCount() == 1 && this.getPlayers().get(0) == excludedPlayer) {
            return;
        }
        // Send to all players - might have to check if player has been sent data packets
        for (Player player : this.getPlayers()) {
            if (player == excludedPlayer) {
                continue;
            }
            // Send
            player.getSession().send(packet);
        }
    }

    public void addItemEntity(int itemId, int amount, GameEntity bornForm) {
        ItemData itemData = GameData.getItemDataMap().get(itemId);
        if (itemData == null) {
            return;
        }
        if (itemData.isEquip()) {
            float range = (1.5f + (.05f * amount));
            for (int i = 0; i < amount; i++) {
                Position pos = bornForm.getPosition().nearby2d(range).addZ(.9f); // Why Z?
                EntityItem entity = new EntityItem(this, null, itemData, pos, 1);
                addEntity(entity);
            }
        } else {
            EntityItem entity =
                    new EntityItem(
                            this, null, itemData, bornForm.getPosition().clone().addZ(.9f), amount); // Why Z?
            addEntity(entity);
        }
    }

    public void loadNpcForPlayerEnter(Player player) {
        this.npcBornEntrySet.addAll(loadNpcForPlayer(player));
    }

    private List<SceneNpcBornEntry> loadNpcForPlayer(Player player) {
        var pos = player.getPosition();
        var data = GameData.getSceneNpcBornData().get(getId());
        if (data == null) {
            return List.of();
        }

        var npcList =
                SceneIndexManager.queryNeighbors(
                        data.getIndex(),
                        pos.toDoubleArray(),
                        Grasscutter.getConfig().server.game.loadEntitiesForPlayerRange);

        var sceneNpcBornCanidates =
                npcList.stream().filter(i -> !this.npcBornEntrySet.contains(i)).toList();

        List<SceneNpcBornEntry> sceneNpcBornEntries = new ArrayList<>();
        sceneNpcBornCanidates.forEach(
                i -> {
                    var groupInstance = scriptManager.getGroupInstanceById(i.getGroupId());
                    if (groupInstance == null) return;
                    if (i.getSuiteIdList() != null
                            && !i.getSuiteIdList().contains(groupInstance.getActiveSuiteId())) return;
                    sceneNpcBornEntries.add(i);
                });

        if (sceneNpcBornEntries.size() > 0) {
            this.broadcastPacket(new PacketGroupSuiteNotify(sceneNpcBornEntries));
            Grasscutter.getLogger().trace("Loaded Npc Group Suite {}", sceneNpcBornEntries);
        }

        return npcList.stream()
                .filter(i -> this.npcBornEntrySet.contains(i) || sceneNpcBornEntries.contains(i))
                .toList();
    }

    public void loadGroupForQuest(List<QuestGroupSuite> sceneGroupSuite) {
        if (!scriptManager.isInit()) {
            return;
        }

        sceneGroupSuite.forEach(
                i -> {
                    var group = scriptManager.getGroupById(i.getGroup());
                    if (group == null) return;

                    var groupInstance = scriptManager.getGroupInstanceById(i.getGroup());
                    var suite = group.getSuiteByIndex(i.getSuite());
                    if (suite == null || groupInstance == null) {
                        return;
                    }

                    scriptManager.refreshGroup(groupInstance, i.getSuite(), false);
                });
    }

    /**
     * Adds an unlocked force to the scene.
     *
     * @param force The ID of the force to unlock.
     */
    public void unlockForce(int force) {
        this.unlockedForces.add(force);
        this.broadcastPacket(new PacketSceneForceUnlockNotify(force, true));
    }

    /**
     * Removes an unlocked force from the scene.
     *
     * @param force The ID of the force to lock.
     */
    public void lockForce(int force) {
        this.unlockedForces.remove(force);
        this.broadcastPacket(new PacketSceneForceLockNotify(force));
    }

    public void selectWorktopOptionWith(SelectWorktopOptionReqOuterClass.SelectWorktopOptionReq req) {
        GameEntity entity = getEntityById(req.getGadgetEntityId());
        if (entity == null) {
            return;
        }
        // Handle
        if (entity instanceof EntityGadget gadget) {
            if (gadget.getContent() instanceof GadgetWorktop worktop) {
                boolean shouldDelete = worktop.onSelectWorktopOption(req);
                if (shouldDelete) {
                    entity.getScene().removeEntity(entity, VisionType.VISION_TYPE_REMOVE);
                }
            }
        }
    }

    public void saveGroups() {
        this.getScriptManager().getCachedGroupInstances().values().forEach(SceneGroupInstance::save);
    }

	public void hideIcewindSuitePresenceProp() {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		var prop = this.getEntityByConfigId(ICEWIND_PROP_CONFIG_ID, ICEWIND_GROUP_ID);

		if (prop != null) {
			this.removeEntity(prop, VisionType.VISION_TYPE_REMOVE);

			Grasscutter.getLogger()
					.debug(
							"[IcewindSuiteFallback] Hid Icewind Suite presence prop: entityId={}, configId={}, groupId={}",
							prop.getId(),
							prop.getConfigId(),
							prop.getGroupId());
		}
	}

	private void restoreIcewindSuitePresencePropIfMissing() {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		if (this.getEntityByConfigId(ICEWIND_PROP_CONFIG_ID, ICEWIND_GROUP_ID) != null) {
			return;
		}

		EntityGadget prop =
				new EntityGadget(
						this,
						ICEWIND_PROP_GADGET_ID,
						ICEWIND_PROP_POS.clone(),
						ICEWIND_PROP_ROT.clone());

		prop.setGroupId(ICEWIND_GROUP_ID);
		prop.setBlockId(ICEWIND_BLOCK_ID);
		prop.setConfigId(ICEWIND_PROP_CONFIG_ID);
		prop.setState(0);

		this.addEntity(prop);

		Grasscutter.getLogger()
				.debug(
						"[IcewindSuiteFallback] Restored Icewind Suite presence prop: entityId={}, configId={}, groupId={}",
						prop.getId(),
						prop.getConfigId(),
						prop.getGroupId());
	}

	private void checkIcewindSuiteFallbackReset() {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		boolean hasIcewindBoss =
				this.getEntities().values().stream()
						.filter(e -> e instanceof EntityMonster)
						.map(e -> (EntityMonster) e)
						.anyMatch(m -> ICEWIND_FALLBACK_MONSTER_IDS.contains(m.getMonsterData().getId()));

		boolean propMissing =
				this.getEntityByConfigId(ICEWIND_PROP_CONFIG_ID, ICEWIND_GROUP_ID) == null;

		if (!hasIcewindBoss && !propMissing) {
			return;
		}

		boolean playerNearArena =
				this.getPlayers().stream()
						.anyMatch(p -> p.getPosition().computeDistance(ICEWIND_PROP_POS) <= 120.0);

		// Do not reset while the player is still near the arena.
		if (playerNearArena) {
			return;
		}

		if (hasIcewindBoss) {
			List<GameEntity> icewindBosses =
					this.getEntities().values().stream()
							.filter(e -> e instanceof EntityMonster)
							.filter(
									e ->
											ICEWIND_FALLBACK_MONSTER_IDS.contains(
													((EntityMonster) e).getMonsterData().getId()))
							.toList();

			this.removeEntities(icewindBosses, VisionType.VISION_TYPE_REMOVE);

			Grasscutter.getLogger()
					.debug(
							"[IcewindSuiteFallback] Removed active Icewind fallback boss after player left arena.");
		}

		var talkEntity = this.getEntityByConfigId(ICEWIND_TALK_CONFIG_ID, ICEWIND_GROUP_ID);

		if (talkEntity instanceof EntityGadget talkGadget && talkGadget.getState() != 0) {
			talkGadget.updateState(0);
		}
		
		this.icewindFallbackVirtualHp.clear();
		this.icewindFallbackVirtualMaxHp.clear();
		this.icewindFallbackSpawnTimes.clear();
		this.icewindFallbackLastHpRatios.clear();
		
		this.restoreIcewindSuitePresencePropIfMissing();
		this.resetIcewindSuiteFallbackWeather();
		this.restoreIcewindSuitePresencePropIfMissing();
	}
	
	private boolean isIcewindFallbackMonster(EntityMonster monster) {
		return this.getId() == ICEWIND_SCENE_ID
				&& monster.getGroupId() == ICEWIND_GROUP_ID
				&& ICEWIND_FALLBACK_MONSTER_IDS.contains(monster.getMonsterData().getId());
	}

	private float getIcewindHpRatio(EntityMonster monster) {
		float curHp = monster.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
		float maxHp = monster.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);

		if (maxHp <= 0f) {
			return 1f;
		}

		return curHp / maxHp;
	}
	
	public void registerIcewindSuiteFallbackBoss(EntityMonster monster) {
		if (monster == null || !this.isIcewindFallbackMonster(monster)) {
			return;
		}

		float maxHp = monster.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);

		this.icewindFallbackSpawnTimes.put(monster.getId(), this.getSceneTimeSeconds());
		this.icewindFallbackLastHpRatios.put(monster.getId(), this.getIcewindHpRatio(monster));

		this.icewindFallbackVirtualMaxHp.putIfAbsent(monster.getId(), maxHp);
		this.icewindFallbackVirtualHp.putIfAbsent(monster.getId(), maxHp);

		this.setIcewindFallbackDisplayedHp(monster);
	}

	private void checkIcewindSuiteFallbackAntiStall(int sceneTime) {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		var icewindBosses =
				this.getEntities().values().stream()
						.filter(e -> e instanceof EntityMonster)
						.map(e -> (EntityMonster) e)
						.filter(this::isIcewindFallbackMonster)
						.toList();

		if (icewindBosses.isEmpty()) {
			this.icewindFallbackVirtualHp.clear();
			this.icewindFallbackVirtualMaxHp.clear();
			this.icewindFallbackSpawnTimes.clear();
			this.icewindFallbackLastHpRatios.clear();
			return;
		}

		for (EntityMonster monster : icewindBosses) {
			if (!monster.isAlive()) {
				continue;
			}

			int bornTime =
					this.icewindFallbackSpawnTimes.computeIfAbsent(
							monster.getId(), id -> sceneTime);

			float currentRatio = this.getIcewindHpRatio(monster);
			float previousRatio =
					this.icewindFallbackLastHpRatios.getOrDefault(monster.getId(), currentRatio);

			boolean inClimaxHpZone = currentRatio <= ICEWIND_FALLBACK_CLIMAX_HP_RATIO;

			int refreshSeconds =
					inClimaxHpZone
							? ICEWIND_FALLBACK_LOW_HP_REFRESH_SECONDS
							: ICEWIND_FALLBACK_REFRESH_SECONDS;

			boolean timedRefresh = sceneTime - bornTime >= refreshSeconds;

			boolean crossedClimaxHpThreshold =
					previousRatio > ICEWIND_FALLBACK_CLIMAX_HP_RATIO
							&& currentRatio <= ICEWIND_FALLBACK_CLIMAX_HP_RATIO;

			this.icewindFallbackLastHpRatios.put(monster.getId(), currentRatio);

			if (timedRefresh || crossedClimaxHpThreshold) {
				this.refreshIcewindSuiteFallbackBoss(
						monster,
						sceneTime,
						timedRefresh
								? (inClimaxHpZone ? "low-hp-timer" : "timer")
								: "hp-threshold");
				return;
			}
		}

		var liveIds = icewindBosses.stream().map(EntityMonster::getId).collect(Collectors.toSet());
		this.icewindFallbackSpawnTimes.keySet().removeIf(id -> !liveIds.contains(id));
		this.icewindFallbackLastHpRatios.keySet().removeIf(id -> !liveIds.contains(id));
		this.icewindFallbackVirtualHp.keySet().removeIf(id -> !liveIds.contains(id));
		this.icewindFallbackVirtualMaxHp.keySet().removeIf(id -> !liveIds.contains(id));
	}
	
	private void refreshIcewindSuiteFallbackBoss(EntityMonster oldMonster, int sceneTime, String reason) {
		if (oldMonster == null || !oldMonster.isAlive()) {
			return;
		}

		int monsterId = oldMonster.getMonsterData().getId();

		var monsterData = GameData.getMonsterDataMap().get(monsterId);
		if (monsterData == null) {
			Grasscutter.getLogger()
					.warn("[IcewindSuiteFallback] Cannot refresh boss; missing monsterData for monsterId={}", monsterId);
			return;
		}

		float oldCurHp = oldMonster.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);

		if (oldCurHp <= 0f) {
			return;
		}

		float oldMaxHp = oldMonster.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
		float virtualHp = this.icewindFallbackVirtualHp.getOrDefault(oldMonster.getId(), oldMaxHp);
		float virtualMaxHp = this.icewindFallbackVirtualMaxHp.getOrDefault(oldMonster.getId(), oldMaxHp);

		Position pos = oldMonster.getBornPos().clone();
		Position rot = oldMonster.getRotation().clone();

		EntityMonster replacement =
				new EntityMonster(
						this,
						monsterData,
						pos,
						rot,
						oldMonster.getLevel());

		replacement.setGroupId(oldMonster.getGroupId());
		replacement.setBlockId(oldMonster.getBlockId());
		replacement.setConfigId(oldMonster.getConfigId());
		replacement.setCampId(oldMonster.getCampId());
		replacement.setCampType(oldMonster.getCampType());
		replacement.setPoseId(oldMonster.getPoseId());
		replacement.setAiId(oldMonster.getAiId());
		replacement.setOwnerEntityId(oldMonster.getOwnerEntityId());
		replacement.setSummonedTag(oldMonster.getSummonedTag());

		float replacementMaxHp = replacement.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);

		this.icewindFallbackVirtualHp.remove(oldMonster.getId());
		this.icewindFallbackVirtualMaxHp.remove(oldMonster.getId());
		this.icewindFallbackSpawnTimes.remove(oldMonster.getId());
		this.icewindFallbackLastHpRatios.remove(oldMonster.getId());

		this.icewindFallbackVirtualHp.put(replacement.getId(), Math.min(virtualHp, virtualMaxHp));
		this.icewindFallbackVirtualMaxHp.put(replacement.getId(), virtualMaxHp);

		replacement.setFightProperty(
				FightProperty.FIGHT_PROP_CUR_HP,
				replacementMaxHp * ICEWIND_FALLBACK_SAFE_HP_RATIO);
				
		this.removeEntity(oldMonster, VisionType.VISION_TYPE_REMOVE);
		this.addEntities(List.of(replacement), VisionType.VISION_TYPE_BORN);

		this.registerIcewindSuiteFallbackBoss(replacement);
		this.setIcewindFallbackDisplayedHp(replacement);

		Grasscutter.getLogger()
				.debug(
						"[IcewindSuiteFallback] Refreshed boss to avoid broken Climax: reason={}, oldEntityId={}, newEntityId={}, monsterId={}, virtualHp={}/{}, actualHp={}, pos={}",
						reason,
						oldMonster.getId(),
						replacement.getId(),
						monsterId,
						virtualHp,
						virtualMaxHp,
						replacement.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP),
						pos);
	}
	
	public void activateIcewindSuiteFallbackWeather() {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		for (Player player : this.getPlayers()) {
			player.setWeather(ICEWIND_WEATHER_ID, ClimateType.CLIMATE_SUNNY);
		}

		this.icewindSuiteFallbackWeatherActive = true;

		Grasscutter.getLogger()
				.debug("[IcewindSuiteFallback] Set arena weather to {}", ICEWIND_WEATHER_ID);
	}

	public void resetIcewindSuiteFallbackWeather() {
		if (this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		if (!this.icewindSuiteFallbackWeatherActive) {
			return;
		}

		for (Player player : this.getPlayers()) {
			this.resetIcewindSuiteFallbackWeather(player);
		}
		
		this.icewindSuiteFallbackWeatherActive = false;
	}

	private void resetIcewindSuiteFallbackWeather(Player player) {
		if (player == null) {
			return;
		}

		player.setWeather(ICEWIND_DEFAULT_WEATHER_ID, ClimateType.CLIMATE_SUNNY);
	}

	public void teleportPlayerToIcewindSuiteArena(Player player) {
		if (player == null || player.getWorld() == null || player.getScene() != this) {
			return;
		}

		var teleportProps =
				TeleportProperties.builder()
						.sceneId(this.getId())
						.teleportType(PlayerTeleportEvent.TeleportType.COMMAND)
						.enterReason(EnterReason.Gm)
						.enterType(EnterTypeOuterClass.EnterType.ENTER_TYPE_GOTO)
						.teleportTo(ICEWIND_PLAYER_START_POS.clone())
						.teleportRot(ICEWIND_PLAYER_START_ROT.clone())
						.build();

		if (player.getWorld().transferPlayerToScene(player, teleportProps)) {
			player.sendPacket(new PacketScenePlayerLocationNotify(this));

			Grasscutter.getLogger()
					.debug(
							"[IcewindSuiteFallback] Teleported player {} near Icewind Suite arena: pos={}",
							player.getUid(),
							ICEWIND_PLAYER_START_POS);
		}
	}
	
	public void queueIcewindSuiteArenaTeleport(Player player, long delayMs) {
		if (player == null || player.getScene() != this || this.getId() != ICEWIND_SCENE_ID) {
			return;
		}

		this.pendingIcewindSuiteArenaTeleports.put(
				player.getUid(),
				System.currentTimeMillis() + delayMs);

		Grasscutter.getLogger()
				.debug(
						"[IcewindSuiteFallback] Queued delayed arena teleport for player {} in {}ms",
						player.getUid(),
						delayMs);
	}

	private void processPendingIcewindSuiteArenaTeleports() {
		if (this.getId() != ICEWIND_SCENE_ID || this.pendingIcewindSuiteArenaTeleports.isEmpty()) {
			return;
		}

		long now = System.currentTimeMillis();

		var iterator = this.pendingIcewindSuiteArenaTeleports.entrySet().iterator();

		while (iterator.hasNext()) {
			var entry = iterator.next();

			if (entry.getValue() > now) {
				continue;
			}

			iterator.remove();

			Player targetPlayer =
					this.getPlayers().stream()
							.filter(p -> p.getUid() == entry.getKey())
							.findFirst()
							.orElse(null);

			if (targetPlayer == null || targetPlayer.getScene() != this) {
				continue;
			}

			this.teleportPlayerToIcewindSuiteArena(targetPlayer);
		}
	}
	
	private void setIcewindFallbackDisplayedHp(EntityMonster monster) {
		if (monster == null || !this.isIcewindFallbackMonster(monster)) {
			return;
		}

		float maxHp = monster.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
		float virtualMaxHp = this.icewindFallbackVirtualMaxHp.getOrDefault(monster.getId(), maxHp);
		float virtualHp = this.icewindFallbackVirtualHp.getOrDefault(monster.getId(), virtualMaxHp);

		if (maxHp <= 0f || virtualMaxHp <= 0f) {
			return;
		}

		float virtualRatio = Math.max(0f, Math.min(1f, virtualHp / virtualMaxHp));

		// Keep the actual monster HP above the broken Climax threshold.
		// The visible HP bar will move between 100% and ~78%, then the boss dies when virtual HP reaches 0.
		float displayRatio =
				ICEWIND_FALLBACK_MIN_DISPLAY_HP_RATIO
						+ ((1f - ICEWIND_FALLBACK_MIN_DISPLAY_HP_RATIO) * virtualRatio);

		displayRatio = Math.max(ICEWIND_FALLBACK_MIN_DISPLAY_HP_RATIO, displayRatio);

		monster.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, maxHp * displayRatio);
		this.broadcastPacket(new PacketEntityFightPropUpdateNotify(monster, FightProperty.FIGHT_PROP_CUR_HP));
	}

	private boolean handleIcewindSuiteVirtualDamage(
			EntityMonster monster, float amount, int attackerId) {
		if (monster == null || !this.isIcewindFallbackMonster(monster)) {
			return false;
		}

		if (amount <= 0f || !monster.isAlive()) {
			return true;
		}

		float maxHp = monster.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
		float virtualMaxHp = this.icewindFallbackVirtualMaxHp.getOrDefault(monster.getId(), maxHp);
		float virtualHp = this.icewindFallbackVirtualHp.getOrDefault(monster.getId(), virtualMaxHp);

		virtualHp = Math.max(0f, virtualHp - amount);

		this.icewindFallbackVirtualMaxHp.put(monster.getId(), virtualMaxHp);
		this.icewindFallbackVirtualHp.put(monster.getId(), virtualHp);

		if (virtualHp <= 0f) {
			monster.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP, 0f);
			this.broadcastPacket(new PacketEntityFightPropUpdateNotify(monster, FightProperty.FIGHT_PROP_CUR_HP));

			this.icewindFallbackVirtualHp.remove(monster.getId());
			this.icewindFallbackVirtualMaxHp.remove(monster.getId());
			this.icewindFallbackSpawnTimes.remove(monster.getId());
			this.icewindFallbackLastHpRatios.remove(monster.getId());

			this.killEntity(monster, attackerId);
			return true;
		}

		this.setIcewindFallbackDisplayedHp(monster);

		Grasscutter.getLogger()
				.debug(
						"[IcewindSuiteFallback] Virtual damage: entityId={}, monsterId={}, damage={}, virtualHp={}/{}, actualHp={}",
						monster.getId(),
						monster.getMonsterData().getId(),
						amount,
						virtualHp,
						virtualMaxHp,
						monster.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP));

		return true;
	}
}
