# Changes:

* Restores the TrifleGadget item payload for dropped items.
* Fixes enemy drops being stuck in midair and unable to be picked up.
* Fixes chest loot appearing as scene props instead of proper pickup items.
* Fixes breakable ores not being destroyed when their HP reaches zero.
* Adds server-side drop handling for break-required gather objects, such as ores and mineral nodes.
* Prevents break-required gather objects from being collected directly without breaking them first.
* Adds simplified mining durability logic based on weapon type.
* Updated Resources, replacing `ConfigAvatar_Lauma.json` with a working version from 6.5.0 resources.
* Fixes Lauma's basic attacks not working correctly in-game.
* Fixes character level-up and ascension requests not being handled in 6.0.0.
* Updates avatar upgrade and promote request opcodes for the 6.0.0 client.
* Decodes 6.0 avatar upgrade/promote request fields from unknown proto fields.
* Fixes the domain reward "Obtained" screen not appearing after claiming Domain of Blessing, Forgery, or Mastery rewards.
* Adds REL6.0-compatible handling for GadgetAutoPickDropInfoNotify.
* Updates GadgetAutoPickDropInfoNotify opcode.
* Manually encodes the reward item list using the 6.0-compatible item_list field number.
* Fixes selectable item boxes from Gift Shop bundles returning "Internal server error".
* Updates 6.0 `UseItemReq` handling for selectable bundles.
* Reads the selected option from the 6.0.0 repeated selection field when present.
* Normalizes the selected option index before resolving the reward item.
* Fixes selector bounds checks for item IDs and item counts.
* Fixed quick-use widget/gadget equipping for 6.0.0 by restoring SetWidgetSlotReq handling and manually encoding REL6.0-compatible SetWidgetSlotRsp, WidgetSlotChangeNotify, GetWidgetSlotRsp, and GetWidgetQuickSlotListRsp packets.
* Corrected gadget classification so normal quick-use gadgets such as Kamera are no longer mistaken for companion/pet gadgets.
* Added temporary quarantine for companion-style gadgets that require a separate attach-avatar/companion entity path, preventing them from corrupting the quick-use gadget slot.
* Disabled incomplete widget bootstrap packets during login until their 6.0.0 payloads are fully verified.
* Adds a legacy/static spawn fallback for missing big-world script groups.
* Restores natural mob spawns in Fontaine, Chenyu Vale, and Natlan when enableScriptInBigWorld is enabled.
* Allows static/fallback overworld monsters to resolve drops after death.
* Falls back to legacy monsterId-based drops when modern drop tables produce no items.
* Restores breakable box/barrel behavior by only giving infinite HP to non-breakable gather objects.
* Adds missing drop entries for Fontaine, Chenyu Vale, Sumeru, and Natlan common/elite enemies.
* Restores material drops for Fontemer Aberrants, Clockwork Meka, Breacher Primus, Fatui Operatives, Praetorian Golems, Xuanwen Beasts, late Sumeru enemies, Sumeru baseline enemies, and Natlan common/elite enemies.
* Adjusts one Fluid Avatar of Lava spawn that was clipped inside terrain.
* Adds extra Fluid Avatar of Lava static spawns for easier testing/farming.
* Suppress false client monster kill-state invokes while monsters still have HP.
* Reset affected monsters by replacing the broken entity at its born position.
* Preserve normal server-side death/drop handling for real kills.
* Adds newer bosses materials as loot from the corresponding bosses.
* Boosted the amount of Mora and EXP from restored bosses.
* Adds elemental gemstone drops to restored overworld boss drop entries.
* Uses matching gem families for each boss element.
* Adds a fallback start path for Icewind Suite via Maillardet's talk gadget.
* Handles empty talk NPC validation for scripted gadget-based talks.
* Applies TALK_EXEC_SET_GADGET_STATE for Maillardet's boss selection.
* Spawns the selected Icewind Suite variant when the original Lua group has no start trigger.
* Hides/restores the decorative Icewind Suite prop.
* Applies and resets Fontaine boss weather around the fallback fight.
* Adds delayed arena teleport after Maillardet dialogue.
* Adds virtual HP handling to Icewind Suite to avoid the unsupported Climax phase.
* Preserves boss drops for fallback-spawned Icewind Suite monsters.
* Updates Drop.json with tested boss artifact/prayer/gem drops.
* Restores Setekh Wenut's missing on-defeat boss reward logic by adding the Trounce Blossom gadget and wiring the ANY_MONSTER_DIE trigger to create it after the boss is defeated.
* Fixes InvestigationMonster data used by the Adventurer Handbook and world map boss markers.
* Using actual Lua monster positions for boss markers when available.
* Echoing isForMark in GetInvestigationMonsterRsp.
* Building map-marker entries directly from InvestigationMonsterConfigData.
* Deriving valid scene IDs from boss group IDs when CityData scene IDs are missing.
* Restoring the Adventurer Handbook Enemies tab population and tracking behavior.
* Skipping the broken Araumi/PMA route barrier gadgets that block access to the Perpetual Mechanical Array underground route.
* Moves the Icewind Suite InvestigationMonster marker closer to the actual arena/fallback fight position.
* Golden Wolflord is now farmable through a virtual HP fallback that avoids the broken Rifthound Skull shield phase.
* Golden Wolflord arena weather now changes to the intended dark and gloomy ambience during the fight and resets after defeat or leaving the area.
* Golden Wolflord’s premature boss blossom spawn is blocked from static/fallback spawns so the reward blossom can appear after defeat.
* Changes Thunder Manifestation's boss group to start from its active suite so the boss consistently appears after relog/restart.
* The full arena/platform is controlled by scene tag 112, which can be enabled once with `/tag add 112`. Once enabled, the arena persists across relogs and the boss fight, reward blossom, and boss materials work normally.
* Adds a regional Seirai Island weather fallback in Scene.java.
* Applies Thunder Manifestation arena weather near the high-altitude Amakumo Peak platform.
* Applies lower Amakumo Peak, Seiraimaru/Koseki Village, initial Seirai Island, and Asase Shrine weather based on player position.
* Resets weather to default when leaving Seirai weather fallback zones or leaving scene 3.
* Applies Seirai fallback weather immediately after entering/spawning to reduce brief default-weather flicker after teleporting.
* Removes unsafe setTargetEntityIdList(index, value).
* Removes unsafe setTargetLockPointIndexList(index, value).
* Uses addTargetEntityIdList(...) only if targetEntityId > 0.
* Removes unsafe ownerEntity null fallback.
* Guard null/empty dropVecList.
* Fix worldLevel bounds check.
* Skip invalid blossom preview entries instead of crashing notifyIcon().
* Added a Dragonspine regional weather fallback in Scene.java.
* Applies Dragonspine's general snowy weather profile around the main Dragonspine region.
* Adds a dedicated Cryo Hypostasis weather zone using the Snow Mountain boss weather profile.
* Prevents Dragonspine weather from leaking into nearby Mondstadt and Liyue areas.
* Resets weather back to the default profile when the player leaves Dragonspine.
* Applies Dragonspine fallback weather immediately after entering/spawning in the scene to reduce brief default-weather flicker after teleporting.
* Keeps Dragonspine and Seirai regional weather fallbacks from overwriting each other when teleporting between both regions.
* Fixed Oceanid’s arena platforms starting submerged by preventing the platform route gadgets from auto-starting in the resource script.
* Disabled the broken Oceanid starter/worktop path so it no longer leaves the arena in a half-started state.
* Added a Java-side fallback Oceanid encounter for scene 3 group 133102769.
* Force-spawns the visible Oceanid body as a virtual-HP boss.
* Oceanid body no longer takes lethal direct damage; virtual HP is reduced by clearing mimic waves.
* Added randomized Oceanid mimic waves for the fallback encounter.
* Cleans up stale mimics, duplicate Oceanid bodies, and broken controller gadgets before/during the fallback fight.
* Resets/recreates Oceanid platforms during the fallback flow to prevent unexpected platform submersion.
* Spawns Oceanid’s Trounce Blossom after the fallback encounter is completed.
* Resets Oceanid weather back to default after defeat and prevents immediate boss respawn until the player leaves the arena reset radius.
* Added missing ScriptLib compatibility support used by Oceanid-related script paths.
* Restored the Configure Team `+` button by wiring the REL6.0 `AddBackupAvatarTeamReq` opcode.
* Restored the Disband button for non-default teams by wiring `DelBackupAvatarTeamReq` and decoding the REL6.0 backup team ID field.
* Added safety handling for configurable backup teams so extra teams remain visible, editable, deployable, and persistent after relog.
* New backup teams are created by cloning a valid existing team instead of creating an unsafe empty team, preventing hidden/empty party softlocks.
* Prevented normal saved teams from being sent as temporary/hidden teams in `AvatarTeamAllDataNotify`.
* Rebuilt active team state on login so extra teams can stay deployed normally after relog.
* Fixed team lookup for sparse team IDs, such as editing Team 7 after disbanding Team 6 (for example).


# Tested:

* Enemy drops can be picked up normally.
* Mora and Primogem drops are collected correctly.
* Chest rewards can be picked up after opening chests.
* Crystal Chunk ores can be broken and drop pickupable loot.
* Cor Lapis-style gather objects can no longer be collected directly without breaking.
* Ore drops can be picked up normally after breaking the node.
* Claymores break ores faster than swords and polearms.
* Bows and catalysts can damage ores more slowly, using reduced durability damage.
* Normal overworld materials, such as Starconch-style pickups, still collect normally.
* Lauma's normal attack string works correctly in-game after replacing the config.
* Character EXP books are consumed correctly when leveling up.
* Mora is consumed correctly during character level-up and ascension.
* Characters can level up through the normal in-game UI.
* Characters can ascend through the normal in-game UI.
* Level-up and ascension animations/effects play correctly.
* Character stats update after level-up and ascension.
* Domain rewards are still granted correctly after interacting with the reward tree/statue.
* The "Obtained" reward screen appears after claiming domain rewards.
* Share Bundles can redeem selected Talent Materials.
* Selecting first, middle, and last options gives the correct item.
* The consumed bundle count decreases correctly.
* The chosen reward item appears on the Obtained screen.
* Invalid end-of-list selection no longer causes "Internal server error".
* Confirmed quick-use gadgets can be equipped, persists after relog, and can be used through the Z quick-use button.
* Confirmed companion-style gadgets are prevented from overwriting/corrupting the normal quick-use slot for now.
* Confirmed enemies spawn naturally in Fontaine, Chenyu Vale, and Natlan with enableScriptInBigWorld: true.
* Confirmed regional collectables, chests, and overworld gadgets still spawn in Fontaine, Chenyu Vale, and Natlan.
* Confirmed basic mobs such as Hilichurls, Mitachurls, and Lawachurls drop loot in Fontaine, Chenyu Vale, and Natlan.
* Confirmed basic mobs still drop loot in older regions.
* Confirmed Fontaine common/elite enemies drop their expected materials.
* Confirmed Sumeru common/elite enemies drop their expected materials.
* Confirmed Natlan common/elite enemies drop their expected materials.
* Aggroed overworld monsters and escaped their aggro range.
* Monsters now reset back to their spawn position instead of dying.
* Normal kills still drop loot.
* Real deaths do not cause instant respawn/reset.
* Overworld bosses now drop their materials and gemstone rewards properly.
* Maillardet options now start Dirge of Coppelia and Nemesis of Coppelius.
* Boss prop disappears during the fight and returns after leaving the area.
* Weather changes during the fight and resets after leaving.
* Boss HP/drops work through the fallback fight.
* Setekh Wenut now spawns its Trounce Blossom after defeat and gives proper boss rewards through a resource script fix.
* Adventurer Handbook Enemies tab now populates and boss/enemy tracking works.
* World map boss markers are restored for normal overworld bosses.
* Boss marker positions now prefer actual Lua monster spawn positions when available.
* Perpetual Mechanical Array underground route barrier no longer blocks access; the broken Araumi route barrier gadgets are skipped server-side.
* Confirmed Golden Wolflord's fallback to prevent the broken Rifthound Skull shield phase from softlocking the fight.
* The boss now uses virtual HP while keeping its displayed HP above the unsupported shield threshold, allowing it to be defeated normally even though the skull phase is not implemented.
* The arena weather is also applied Java-side during the fight and reset after defeat or leaving.
* Golden Wolflord's Trounce Blossom now spawns normally after the boss is defeated.
* Thunder Manifestation boss group now starts in its active suite, making the boss consistently spawn after relog/restart.
* Thunder Manifestation arena/platform is restored by enabling scene tag 112.
* The boss wakes up, fights normally, drops a reward blossom after defeat, and gives the correct materials.
* Thunder Manifestation arena uses the intended high-altitude arena weather.
* Lower Amakumo Peak uses the intended storm weather.
* Seiraimaru/Koseki Village uses the intended local Seirai weather.
* Initial Seirai Island / Slumbering Court side uses the intended local weather.
* Asase Shrine uses its calmer local weather.
* Teleporting away from Seirai resets weather back to default.
* Dragonspine Statue of The Seven area applies the snowy Dragonspine weather correctly.
* Peak of Vindagnyr / Skyfrost Nail side applies the snowy Dragonspine weather correctly.
* Snow-Covered Path and Frostbearing Tree areas apply the snowy Dragonspine weather correctly.
* Dragonspine outskirts and Liyue-side Dragonspine waypoints apply the snowy Dragonspine weather correctly.
* Nearby non-Dragonspine areas in Mondstadt and Liyue reset back to default weather.
* Forsaken Rift, Ridge Watch, Sal Terrae-side areas, and Hidden Palace of Lianshan Formula remain outside the Dragonspine weather fallback.
* Cryo Hypostasis area uses the dedicated Snow Mountain boss weather profile.
* Cryo Hypostasis still spawns with the dedicated boss weather profile active.
* Teleporting from Dragonspine to Seirai still allows Seirai's regional weather fallback to apply correctly.
* Teleporting from Seirai to Dragonspine still allows Dragonspine's regional weather fallback to apply correctly.
* Teleporting out of Dragonspine resets weather back to default.
* Oceanid arena platforms no longer starts permanently submerged.
* Oceanid weather (ID 2021) applies near the arena and resets after defeat.
* Confirmed the visible Oceanid body spawns for the fallback encounter.
* direct attacks do not prematurely kill, damage or duplicate the Oceanid body.
* Confirmed randomized mimic waves spawn and progress the fallback fight.
* Clearing all fallback waves defeats Oceanid and spawns the Trounce Blossom.
* Confirmed the fight does not immediately restart while the reward blossom is active.
* Added Teams 5, 6, and 7 through the Configure Team `+` button.
* Edited each extra team with different characters.
* Deployed Team 7 and confirmed it stayed selected, visible, and usable after relog.
* Confirmed extra teams stayed populated and listed in Party Setup after relog.
* Disbanded Team 6 and confirmed Teams 5 and 7 remained intact.
* Confirmed deleted backup team IDs can be reused when adding a new team.
* Edited an active backup team after deleting a middle team slot without crashes or softlocks.
* Confirmed default Teams 1–4 cannot be disbanded.

# Notes:
* Ore durability is currently a simplified approximation. It's meant to make ores functional in LunaGC 6.0.0 to the best of my abilities, not perfectly match the official game’s mining behavior.
* The 6.0 client sends some avatar upgrade/promote request data through fields not mapped by the current generated proto classes, so the handlers include compatibility decoding for those fields.
* The current domain fix manually encodes the packet payload instead of regenerating all proto classes, because the existing generated 6.0.0 proto mapping does not match the client’s expected field layout for this packet.
* Icewind Suite InvestigationMonster marker fixed placement compensates for the incomplete scene3_group133402002.lua script, which prevents the marker system from resolving a real Lua monster position for Icewind Suite like it can for most other bosses.
* Players need to run `/tag add 112` once to permanently reveal the full Thunder Manifestation arena/platform. Preferably at the highest Teleport Waypoint in Amakumo Peak.
