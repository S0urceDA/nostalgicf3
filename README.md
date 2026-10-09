# Nostalgic F3

A small client-side Fabric mod that gives vanilla debug entries fixed columns and a familiar order. Version 0.1.7 preserves the config-free layout and fixes requested after comparing Better Vanilla F3. Vanilla Debug Options (F3 + F6) still control visibility, including Always On. There is no configuration file, Fabric API requirement, or configuration library.

## Install

Use Fabric Loader 0.17.3 or newer on 1.21.x, or 0.18.4 or newer on 26.x. Choose **one** matching JAR from `build/libs` and put it in the game's `mods` folder. The server does not need this mod.

| JAR target | Declared compatible Minecraft versions | Game Java |
| --- | --- | --- |
| 1.21.10 | 1.21.9, 1.21.10 | 21+ |
| 1.21.11 | 1.21.11 | 21+ |
| 26.1.2 | 26.1, 26.1.1, 26.1.2 | 25+ |
| 26.2 | 26.2 | 25+ |
| 26.3 | 26.3 | 25+ |

All five targets passed builds and real Fabric/Mixin transformation probes. Adjacent patch versions are declared compatible but were not launched separately. The probes also exercise the actual transformed reusable collector, its forwarding and reset behavior. The probes do not replace an in-world visual check.

## Behavior

- Left: version; FPS, performance impactors and GPU on one line; TPS and renderer counters; position, section, speed, lighting, heightmaps, biome and difficulty; generation, spawn and sound information.
- Right: memory and detailed memory together, then system information; targeted block, fluid and entity information, with tags directly alongside each target.
- Day count joins local difficulty when both entries are visible. With difficulty disabled, day count remains a separate left-column line. Performance information likewise remains visible with FPS disabled.
- The Block line includes the old section-relative `[x y z]` coordinates. The separate section-position entry is retained.
- Empty targets produce no blank gaps; repeated group outputs share one separator. Memory has one separator before the next visible section, even with system specs disabled.
- F3 draws behind menus, except on the Debug Options screen. F1 visibility retains vanilla behavior, matching Better Vanilla F3's default.
- On 1.21.x, the rendered entity counter uses the previous completed frame's count before vanilla clears it.
- Unknown third-party groups go to the right without mod-specific IDs. Priority lines and ordinary lines retain vanilla routing; entries retain vanilla ordering among themselves.

Existing entry visibility choices and reduced debug information remain controlled by vanilla. Vanilla measurements are retained; OptiFine-specific minimum-FPS and chunk-update counters are not added. Profiler pie scale and background keep vanilla defaults.

## Develop (Windows)

Building uses JDK 25; 1.21.x development launches use JDK 21. All shipped mod classes target Java 21 bytecode. Minecraft 26.x itself requires Java 25. If Gradle does not find your JDKs, set JAVA_HOME_21_X64/JAVA_HOME_25_X64 or supply `-Porg.gradle.java.installations.paths` with comma-separated installation folders.

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-25.0.1'
.\gradlew.bat buildAll probeAll --no-daemon
.\gradlew.bat :26.3:runClient
```

`layoutParityTest` is part of buildAll/check and compares 12,000 randomized frames against the test-only 0.1.6 reference. `buildAll` produces five installable jars in `build/libs`. `layoutTest` checks 2,048 visibility combinations plus modern placement, absent FPS/difficulty, empty and repeated targets, memory spacing, vanilla remainder preservation and negative relative coordinates. `probeAll` forces the actual game classes through Fabric/Mixin and checks overlay, ordering, position, menu rendering and applicable entity-counter hooks, then exits without a game window. Its test mod is excluded from release jars.

Shared layout rules live in `src/shared/java`; version-adapted mixin templates are in `src/client-template`. Generated sources live under each version's ignored build directory. `docs/INVESTIGATION.md` records decisions and verification. The historical OptiFine layout was superseded by the user's requested Better Vanilla F3 behavior in 0.1.3.

Version 0.1.7 refactors assembly into reusable indexed buffers and one collector. Output matches 0.1.6 in 12,000 randomized comparisons. The local stress benchmark measured about 83% less assembly time and 99% less allocation; this is not an in-game FPS result. See `docs/PERFORMANCE.md` for measurements and limits. Run `gradlew layoutBenchmark` to repeat. Git history records the behavior reference, optimization, tests and release documentation separately.

Project location: `C:\Users\andra\AppData\Roaming\.minecraft\e\nostalgicf3`.

License: CC0-1.0, inherited from the Fabric starter. Reference behavior was independently implemented; no Minecraft, OptiFine or Better Vanilla F3 source/binaries are distributed.
