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

# Notes:
* Ore durability is currently a simplified approximation. It's meant to make ores functional in LunaGC 6.0.0 to the best of my abilities, not perfectly match the official game’s mining behavior.
* The 6.0 client sends some avatar upgrade/promote request data through fields not mapped by the current generated proto classes, so the handlers include compatibility decoding for those fields.
* The current domain fix manually encodes the packet payload instead of regenerating all proto classes, because the existing generated 6.0.0 proto mapping does not match the client’s expected field layout for this packet.
* Icewind Suite InvestigationMonster marker fixed placement compensates for the incomplete scene3_group133402002.lua script, which prevents the marker system from resolving a real Lua monster position for Icewind Suite like it can for most other bosses.
