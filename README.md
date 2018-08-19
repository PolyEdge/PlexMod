
  [![Discord](https://img.shields.io/discord/465349851920597029.svg?logo=discord&colorA=7289DA&logoWidth=15&colorB=gray)](https://discord.gg/neGvHCc)

# Plex Mod

Plex Mod is a mod for Minecraft that provides several useful QoL features for playing on the popular Mineplex server.

## Installing the Mod

### Prerequisites

Be sure to have run Minecraft 1.8.9 at least once before installing Forge. Download the latest 1.8.9 MinecraftForge installer (version 11.15.1.2318) [here](http://files.minecraftforge.net/maven/net/minecraftforge/forge/index_1.8.9.html). You can download either the Windows installer, which is a .exe file, or the plain installer, which is a .jar file, they're the same. Bear in mind, however, you must have Java installed to run the .jar installer (Minecraft installs its own version of Java, so you may not have it installed already). Chrome will also declare that the .jar installer may be harmful. You can safely ignore this warning and keep the file.

Once the installer has finished downloading, run it. Leave the default settings and press "OK" to install Forge. Wait until it is finished installing. You can then close out and delete the installer file. Minecraft Forge is now installed! There should be a new Forge launch profile in the Minecraft launcher. Run it at least once before continuing.

### Installing

#### Windows

Download the PlexMod zip file here(placeholder, no binary releases exist yet :P). Unzip the downloaded file.
Press the Windows Key and R at the same time, and a window titled "Run" should pop up in the bottom left corner of your screen. Paste the following into the "Open" bar and press OK.

```
%appdata%\.minecraft\mods
```

A file explorer window will pop up. Drop the jar file from inside the zip into the opened folder. Close the window. PlexMod is now installed. Enjoy!

#### Mac

No Mac install instructions cuz I don't own a Mac and I can't figure out how people install mods on Mac

#### Linux

No Linux install instructions yet, sorry! I'm sure you'll be able to figure it out though, since you're a smart Linux user

## Building From Source

Building from source is rather simple. Simply switch to the repository directory and run:

```
./gradlew build
```

A PlexMod jar will be generated in the build/lib folder.

## Setting Up Development Environment

Switch to the repository directory and type:

```
./gradlew setupDecompWorkspace
```

You should now be ready to make changes! To generate project files for either Eclipse or IntelliJ IDEA, run one of the following commands:

```
./gradlew eclipse
./gradlew idea
```

## Built With

* [MinecraftForge](https://files.minecraftforge.net/) - Minecraft mod compatibility framework
* [Gradle](https://gradle.org/) - Build and dependency management solution
* [ForgeGradle](https://github.com/MinecraftForge/ForgeGradle) - Gradle plugin to set up Forge build environment
* [ShadowJar](https://github.com/johnrengelman/shadow) - Gradle plugin to package dependencies into the mod jar along with relocating the org.slf4j package
* [DiscordIPC](https://github.com/jagrosh/DiscordIPC) - Library to connect to and update Discord Rich Presence

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on contributing to this repository.

## License

This project currently does not have a license. We're working on it!
