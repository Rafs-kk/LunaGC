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

Note: Ore durability is currently a simplified approximation. It's meant to make ores functional in LunaGC 6.0.0 to the best of my abilities, not perfectly match the official game’s mining behavior.

## Updated version of Grasscutters, with some new features implemented.
Old Discord for LunaGC https://discord.gg/7D5gkyJR5Y (don't ask for support there, instead create an issue in this repository)

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
- Get game version REL6.0.0 (pray you still have it)
- Make sure to install java and set the environment variables.
- Build the server (refer to "Compile the actual server" in this guide.)
- Download the [Resources]([https://github.com/Rafs-kk/LunaGC-6.0-res/tree/6.0.0), make a new folder called `resources` in the downloaded LunaGC folder and then extract the resources in that new folder.
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
  git clone https://github.com/pmagixc/LunaGC.git -b 6.0.0
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
