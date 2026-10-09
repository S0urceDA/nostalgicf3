# Layout investigation

Minecraft 1.21.9 introduced per-entry visibility. The enabled-entry list sorts identifiers alphabetically. DebugScreenOverlay then balances priority lines, divides ordinary lines in half, and divides named groups in half. Removing entries therefore changes both ordering and columns.

Locally inspected OptiFine 1.21.11 HD U J9: the patched DebugScreenEntries defines fixed side sets and ordering indices; DebugScreenEntryList sorts by those indices. The overlay tracks the current entry identifier and routes every output method to that side; FPS, performance impactors, and GPU utilization share a line. No OptiFine code or binaries are included in this project.

Nostalgic F3 captures each enabled entry through a small independent DebugScreenDisplayer, sorts its output through a shared pure-Java layout policy, and supplies the resulting columns to vanilla's existing text renderer. Vanilla continues to select entries, enforce reduced debug information, and render charts and keybinding hints. Entries are called exactly once. Since 0.1.1, each entry retains its individual line/group output events. The layout follows J9 exactly: separators are inserted on each marked group call, including empty groups and leading/trailing blanks. Unknown entries route right with index -1; ties preserve enabled-list order. The enabled-entry list uses the same rank comparator. Vanilla footer rows are appended unchanged.

Targets: 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3. Separate binaries are necessary across the obfuscation boundary at 26.1. Source generation handles ResourceLocation -> Identifier and render -> extractRenderState. Builds use JDK 25; 1.21.x artifacts target Java 21.

Sources: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9 and https://fabricmc.net/2025/09/23/1219.html; official Mojang mappings/source generated locally by Loom; local OptiFine reference supplied by the user.
## Verification result

All five targets built successfully. The layout regression runner passed its 2,048 visibility combinations and additional multiline/namespace/performance/footer checks. A separate test-only prelaunch mod forced Fabric to transform DebugScreenOverlay and confirmed all four hooks were present for every target; all five launches exited successfully before window creation. This is not an in-world visual test.

26.3 extractLines adds an integer argument; its generated hook descriptor includes this argument. From 26.1 onward, target state and tag entries are independent and both are explicitly assigned to the same section. 26.3 adds player_speed and renames post_effect to post_effects; both variants have fixed placements.

## 0.1.1: exact J9 layout parity

Replaced broad section separators with the exact group-call rule. The separator-marked J9 entries are player_position, system_specs, looking_at_block, looking_at_fluid and looking_at_entity. addPriorityLine delegates to normal line behavior; FPS, simple_performance_impactors and gpu_utilization merge each emitted ordinary line with a single space. Group events do not participate in FPS merging. Repeated and empty group calls remain observable. No leading/trailing blank normalization is performed. The footer is preserved verbatim.

Shared vanilla IDs have J9's relative order and side assignment. Unknown IDs get -1 and right-column placement; the entry-list mixin replaces the alphabetical comparator with the layout comparator and preserves equal-rank input ordering. Later Minecraft versions' split tag entries remain adjacent to their target state without extra separators. These modern additions are adaptations because the supplied J9 reference targets 1.21.11.

Per the user's explicit choice, this remains a layout-only mod: vanilla measurements are retained rather than reproducing OptiFine-specific minimum-FPS and renderer chunk-update counters. The 1.21.11 text-rendering geometry and colors already match J9.

Verification: complete-row expected fixtures across 2,048 enabled-entry combinations, plus specific FPS/group/empty-group/tie-order/footer/tag fixtures; five builds and test-only Fabric transformation probes for both overlay and entry-list hooks. No claim of a side-by-side visual test is made.
## 0.1.2: 26.2 K2 pre1 reference and lowered requirements

Inspected preview_OptiFine_26.2_HD_U_K2_pre1.jar supplied by the user. Its explicit right-side list contains memory, system_specs, block state/tags, fluid state/tags and entity. detailed_memory, looking_at_entity_tags, day_count and sound_cache remain in the registration-order tail and route right via the fallback rule. The mod now reproduces that sequence rather than assigning those entries custom positions. Block/fluid tags are explicitly marked for group-call separators in this preview. Player speed is absent from this 26.2 reference and retains its prior 26.3 placement. The J9 legacy-ID sequence is unaffected.

Lowered minimum Fabric Loader from 0.19.5 to 0.17.3 for 1.21.x and 0.18.4 for 26.x. Both legacy targets passed transformation probes on Java 21 with Loader 0.17.3; all three modern targets passed on Java 25 with Loader 0.18.4. All mod classes and the mixin compatibility declaration target Java 21. Minecraft 26.x's own Java 25 runtime requirement remains. Fabric API and external configuration libraries are not required.

Tests include the exact K2 pre1 right-column row sequence and tag separators, alongside the 2,048 visibility fixtures and five version probes. Earlier sections describe historical decisions superseded by this update.

## 0.1.3: config-free Better Vanilla F3 decisions

The user designated the local bettervanillaf3 project as the definitive behavioral reference and requested its fixes without configuration. This supersedes the earlier exact OptiFine spacing and fallback placement. Implementation is independent; no reference implementation is copied or shipped.

Detailed memory follows memory; sound cache follows sound mood on the left; entity tags stay beside entity information; block/fluid tags have no separate gaps. Day count joins the actual local-difficulty output, falling back to its own line if difficulty is disabled. Performance/GPU information remains visible with FPS disabled. Empty target groups add no separators, and repeated group calls receive one separator per entry. A visible memory block is separated from the next visible section regardless of which memory/system entries are disabled. Unknown IDs delegate to vanilla's displayer instead of being forced to a column; equal-rank identifiers retain the original comparator.

The Block-line formatting hook restores section-relative coordinates using bit masking, including negative world coordinates. The separate section entry remains available. The pre-26.1 entity counter captures the previous frame's render-state size immediately before renderLevel resets it. Later versions retain their vanilla counter.

Menu rendering moves before screen/overlay rendering. 1.21.x hooks GameRenderer.render, 26.1.2 hooks GameRenderer.extractGui, and 26.2/26.3 hook Gui.extractRenderState before its first overlay-field read. The old later call is suppressed to avoid duplicate output; DebugOptionsScreen remains excluded. F1 cancels overlay rendering regardless of menus: Options.hideGui through 26.1.2, Hud.isHidden() on 26.2/26.3. Optional pie scale/background controls retain vanilla defaults; no config or new dependency is added. Loader baselines remain 0.17.3/0.18.4, with Java 21 mod bytecode.

Verification: 2,048 visibility combinations and focused modern-placement, empty/repeated-target, disabled-FPS/difficulty, memory-separator, unknown-ID/remainder and negative-coordinate tests passed. All five builds passed. Expanded real loader probes transformed every added mixin target under the existing minimum Loader versions. These probes confirm injection compatibility, not a visual or in-world behavior test.


## 0.1.4: generic mod groups, spacing and default F1 behavior

Unknown entries now forward priority/ordinary lines to vanilla but collect named groups independently by group identifier. Those groups appear on the right after known entries and vanilla priority/ordinary output, with one trailing separator per nonempty group. No mod IDs are hardcoded. This matches the reference's group-versus-priority distinction, including Fabric renderer information using priority output.

FPS additions strip boundary whitespace and join with exactly one space. The two vanilla separators around left priority/regular text are suppressed, leaving the existing chart-hint separator. The forced F1 cancellation from 0.1.3 is removed at the user's request: Better Vanilla F3 defaults that option off. Optional forced day-count and profiler customization remain absent.

All five builds and expanded real loader probes passed on the existing minimum Loader versions. Regression fixtures cover unknown groups sharing identifiers, empty groups, priority remainder, FPS whitespace and chart-hint spacing alongside the existing 2,048 visibility checks. Visual confirmation in a running world remains a separate check.

## 0.1.5: separator before unknown groups

Insert a blank row before each nonempty unknown group only when the previous right-column row contains text. Existing separators are reused, empty groups add nothing, and a mod-only column has no leading blank. This fixes groups such as Voxy touching system specifications without any mod-specific rule. Regression cases for present/absent separators, repeated groups, empty groups and mod-only columns passed, as did all five builds. Mixin hooks are unchanged from the 0.1.4 loader probes.

## 0.1.6: ordinary mod output boundary

The user confirmed 0.1.5 still lacked a gap. The live game log confirmed that version was loaded. Inspecting Better Vanilla F3 showed known right-side lines are placed directly into vanilla rightLines before vanilla retains its pre-regular-output separator (only the left separators are suppressed). Our separate assembly left vanilla unaware of those known rows. Voxy's version entry uses addLine, while its detailed debug entry uses named groups; the 0.1.5 group-only fix therefore missed the ordinary version line.

Restore a separator at the boundary between assembled known right-side rows and vanilla ordinary/priority remainder when both boundary rows contain text. Existing blank rows are reused; empty remainder or absent known rows add no separator. This is independent of mod IDs. Regression fixtures cover ordinary mod text after system specs, a preexisting gap, empty remainder and a mod-only column. All five builds and layout checks passed. The actual modded-world visual result remains to be checked by the user.

## 0.1.7: reusable layout assembly

Replaced per-entry event capture, per-frame maps/sorting/stream flattening and per-entry anonymous collectors with fixed indexed buffers, precomputed flags, cached ID indices and a single reusable collector. Group and column buffers are reused; list copying avoids temporary arrays, and FPS joining uses one reusable StringBuilder with equivalent Unicode boundary handling. Frame creation is lazy, hidden resets have a no-work fast path after clearing prior text, and the vanilla displayer reference is cleared in finally. All spacing and visibility choices from 0.1.6 are preserved.

The preserved old implementation is test-only. Existing 2,048 fixtures and 12,000 randomized full-output comparisons passed. Real Fabric probes exercised the transformed collector and all hooks on all five targets, in addition to successful builds. Local assembly benchmark median: 7,421 -> 1,243 ns/frame, 31,328 -> 408 bytes/frame (83.3% time and 98.7% allocation reduction). This is a stress fixture, not an FPS measurement or a Better Vanilla F3 performance comparison. Detailed method and limits are in PERFORMANCE.md. No dependency/version minimums were raised; 0.1.7 remains a pre-1.0 test release.

## Earlier patch-version compatibility

The packaged 0.1.7+mc1.21.10 jar passed real Fabric/Mixin and collector probes on Minecraft 1.21.9 with Loader 0.17.3 and Java 21. The same packaged 0.1.7+mc26.1.2 jar passed on both 26.1 and 26.1.1 with Loader 0.18.4 and Java 25. All hooks were transformed and the collector forwarding, grouping, reset and lazy-empty paths passed. The tested mod jars were not rebuilt against the earlier games. Loom remaps the legacy packaged jar into the earlier game's named development namespace; the unobfuscated 26.x jar is loaded directly. These are automated compatibility checks, not additional in-world visual tests.

The isolated `compatibility-tests` build consumes jars from `build/libs`, derives their version and Loader baselines from the main gradle.properties, and shares the existing probe sources. Run the root buildAll first, then `gradlew -p compatibility-tests verifyAll`. CI now repeats these three earlier-version checks after the five target builds and probes. No additional release jar targets are introduced.
