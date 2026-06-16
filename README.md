# LunaGC-6.0.0

This is a personal fork of LunaGC for 6.0. I made this because I mostly play on this version, and I wanted to fix a few lingering bugs that affected normal gameplay.

Changes:

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

Tested:

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

Notes:
* Ore durability is currently a simplified approximation. It's meant to make ores functional in LunaGC 6.0.0 to the best of my abilities, not perfectly match the official game’s mining behavior.
* The 6.0 client sends some avatar upgrade/promote request data through fields not mapped by the current generated proto classes, so the handlers include compatibility decoding for those fields.
* The current domain fix manually encodes the packet payload instead of regenerating all proto classes, because the existing generated 6.0.0 proto mapping does not match the client’s expected field layout for this packet.

## Updated version of Grasscutters, with some new features implemented.

Features and functionality of the ps is not guaranteed, try it yourself to see what works and what doesnt.
This is possibly the only public PS with updated mob and gadget spawns! (Up to Version 5.4)

# Outstanding bug(s) hall of fame:
- Abyss (as expected) -  wrong floor and chamber numbers being displayed (for example floor 0 chamber 1000) and buffs don't apply/change
- Lunar Bloom

# Read the [handbook](handbook.md)!

# Setup Guide
- Read it below, its just enough to get the server up and running along with the client.

## Main Requirements

- Get [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- Get [MongoDB Community Server](https://www.mongodb.com/try/download/community)
- Get [NodeJS](https://nodejs.org/dist/v20.15.0/node-v20.15.0-x64.msi) (For handbook generation)
- Get game version [REL6.0.0](https://archive.heavens-era.com/Tenshi's%20Archive/Live%20Service/miHoYo/Genshin%20Impact/Game%20Files/OS/6.0.0)
- Make sure to install java and set the environment variables.
- Build the server (refer to "Compile the actual server" in this guide.)
- Download the [Resources](https://github.com/Rafs-kk/LunaGC-6.0-res/tree/6.0.0), make a new folder called `resources` in the downloaded LunaGC folder and then extract the resources in that new folder.
- Set useEncryption, Questing and useInRouting to false (it should be false by default, if not then change it)
- [Patch the game](#patching-the-game)
- Start the server and the game, make sure to also create an account in the LunaGC console!
- Have fun (or don't)

### Patching the game
- Put [Astrolabe.dll](https://github.com/pmagixc/LunaGC/raw/6.0.0/patch/Astrolabe.dll) in the game directory
- To "disable" the patch, just rename Astrolabe.dll to something else so it's not a DLL or don't name it Astrolabe (for example Astrolabe.deleleu / astrollable.dll)

### Getting started

- Clone the repository (install [Git](https://git-scm.com) first )

  ```
  git clone https://github.com/Rafs-kk/LunaGC.git -b 6.0.0
  ```

- Now you can continue with the steps below.


### Compile the actual Server

**Requirements**:

[Java Development Kit 17 | JDK](https://oracle.com/java/technologies/javase/jdk17-archive-downloads.html) or higher

- **Sidenote**: Handbook generation may fail on some systems. To disable handbook generation, append `-PskipHandbook=1` to the `gradlew jar` command.

- **For Windows**:

  ```shell
  .\gradlew.bat
  .\gradlew.bat jar
  ```

- **For Linux**:

  ```bash
  chmod +x gradlew
  ./gradlew
  ./gradlew jar
  ```

### You can find the output JAR in the project root folder.

### Manually compile the handbook

```shell
./gradlew generateHandbook
```

## Troubleshooting

- Make sure to set useEncryption and useInRouting both to false otherwise you might encounter errors.
- To use windy make sure that you put your luac files in C:\Windy (make the folder if it doesnt exist)
- If you get an error related to MongoDB connection timeout, check if the mongodb service is running. On windows: Press windows key and r then type `services.msc`, look for mongodb server and if it's not started then start it by right clicking on it and start. On linux, you can do `systemctl status mongod` to see if it's running, if it isn't then type `systemctl start mongod`. However, if you get error 14 on linux change the owner of the mongodb folder and the .sock file (`sudo chown -R mongodb:mongodb /var/lib/mongodb` and `sudo chown mongodb:mongodb /tmp/mongodb-27017.sock` then try to start the service again.)

## Credit

proto Repository [hk4e-protos](https://gitlab.com/kitkat-multiverse/genshin-protocol)

patch Repository [hk4e-patch-universal](https://github.com/oureveryday/hk4e-patch-universal) (and credit to Hartie95 for fixing it)

Original repository [pmagixc](https://github.com/pmagixc/LunaGC)
