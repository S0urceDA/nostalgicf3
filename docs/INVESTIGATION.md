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
