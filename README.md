# Minecraft Mod Template

Fabric starter for Minecraft Java Edition 1.21.11. Requires JDK 21.

## Quick start (Windows)

1. Open this folder in IntelliJ IDEA as a Gradle project.
2. Set the project SDK and Gradle JVM to JDK 21.
3. Run `.\gradlew.bat runClient` to launch a development game.
4. Run `.\gradlew.bat build` to build your mod. The distributable JAR is in `build/libs` (use the one without `-sources`).

The first run downloads Gradle, Minecraft, and dependencies and requires internet access.

## Where to edit

- `src/main/java/com/example/ExampleMod.java`: shared mod initialization.
- `src/client/java/com/example/client/ExampleModClient.java`: client-only initialization.
- `src/main/resources/fabric.mod.json`: mod ID, name, description, and authors.
- `src/main/resources/assets/modid`: textures and other assets.
- `gradle.properties`: Minecraft, Fabric, and mod versions.

Before releasing, replace `modid` consistently in Java, metadata, build.gradle, mixin configuration filenames and asset folders. Rename the `com.example` package and update entrypoints/mixin package declarations. Update the example contact links in fabric.mod.json.

Based on the official Fabric example mod: https://github.com/FabricMC/fabric-example-mod/tree/1.21.11
Template license: CC0-1.0 (see LICENSE).
