# Nostalgic F3

A small client-side Fabric mod that gives vanilla debug entries fixed columns and a classic order. Entry order, side assignments, per-group blank rows, empty target groups and help-footer spacing follow OptiFine 1.21.11 J9. Vanilla FPS measurements are retained. Vanilla Debug Options (F3 + F6) still control visibility, including Always On; reduced debug information and charts are left to vanilla.

## Install

Use Fabric Loader 0.19.5 or newer. Choose **one** JAR matching your Minecraft version from `build/libs`, and put it in that game's `mods` folder. Fabric API is not required. The server does not need this mod.

| JAR target | Declared compatible Minecraft versions | Game Java |
| --- | --- | --- |
| 1.21.10 | 1.21.9, 1.21.10 | 21+ |
| 1.21.11 | 1.21.11 | 21+ |
| 26.1.2 | 26.1, 26.1.1, 26.1.2 | 25+ |
| 26.2 | 26.2 | 25+ |
| 26.3 | 26.3 | 25+ |

The five target versions have passed builds and real Fabric/Mixin transformation probes. The adjacent patch versions are declared compatible but were not launched separately. An in-world visual check remains to be done.

## Layout

- Left: version, combined FPS/performance/GPU line, TPS, renderer counters; position, section and speed, light, heightmaps, biome, difficulty and day count; generation, spawn and sound information.
- Right: memory and system information, then targeted block, fluid and entity information. Newer versions' separate tag entries stay with their corresponding target.
- Unknown mod entries: placed in the right column before indexed entries. Equal-rank entries retain the original enabled-list order, matching J9. No text is discarded.

This mod changes placement, not which entries are enabled. To show additional information, use F3 + F6. Existing visibility choices are preserved.

## Develop (Windows)

Open this folder as a Gradle project. Building requires JDK 25, even for the Java 21 game targets.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.1'
.\gradlew.bat buildAll --no-daemon
.\gradlew.bat layoutTest --no-daemon
.\gradlew.bat probeAll --no-daemon
.\gradlew.bat :1.21.11:runClient
```

`buildAll` produces five installable jars in `build/libs`. `layoutTest` checks 2,048 complete row-for-row visibility fixtures, shuffled ordering, leading/trailing blanks, empty and repeated groups, merged FPS lines, unknown-entry ordering, exact footer handling and modern tags. `probeAll` loads the actual transformed game overlay and entry list under Fabric in all five versions, then exits before opening a window. The test-only probe is excluded from release jars.

Shared layout rules live in `src/shared/java`; the version-adapted mixin template is in `src/client-template`. Generated version sources live under each version's ignored build directory. `docs/INVESTIGATION.md` records the vanilla and locally supplied OptiFine investigation. OptiFine is a reference only and is excluded from Git and release archives.

Git history records the starter, multi-version setup, layout implementation, compatibility fixes and loader tests as separate commits.

Project location: `C:\Users\andra\AppData\Roaming\.minecraft\e\nostalgicf3`.

License: CC0-1.0, inherited from the Fabric starter. No Minecraft or OptiFine binaries/source are distributed.