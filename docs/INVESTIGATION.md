# Layout investigation

Minecraft 1.21.9 introduced per-entry visibility. The enabled-entry list sorts identifiers alphabetically. DebugScreenOverlay then balances priority lines, divides ordinary lines in half, and divides named groups in half. Removing entries therefore changes both ordering and columns.

Locally inspected OptiFine 1.21.11 HD U J9: the patched DebugScreenEntries defines fixed side sets and ordering indices; DebugScreenEntryList sorts by those indices. The overlay tracks the current entry identifier and routes every output method to that side; FPS, performance impactors, and GPU utilization share a line. No OptiFine code or binaries are included in this project.

Nostalgic F3 captures each enabled entry through a small independent DebugScreenDisplayer, sorts its output through a shared pure-Java layout policy, and supplies the resulting columns to vanilla's existing text renderer. Vanilla continues to select entries, enforce reduced debug information, and render charts and keybinding hints. Entries are called exactly once. Unknown mod entries are retained after known entries on the left, sorted by full identifier. The layout is compact: disabling a row closes its gap without changing another entry's side or relative order.

Targets: 1.21.10, 1.21.11, 26.1.2, 26.2, 26.3. Separate binaries are necessary across the obfuscation boundary at 26.1. Source generation handles ResourceLocation -> Identifier and render -> extractRenderState. Builds use JDK 25; 1.21.x artifacts target Java 21.

Sources: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9 and https://fabricmc.net/2025/09/23/1219.html; official Mojang mappings/source generated locally by Loom; local OptiFine reference supplied by the user.
## Verification result

All five targets built successfully. The layout regression runner passed its 2,048 visibility combinations and additional multiline/namespace/performance/footer checks. A separate test-only prelaunch mod forced Fabric to transform DebugScreenOverlay and confirmed all four hooks were present for every target; all five launches exited successfully before window creation. This is not an in-world visual test.

26.3 extractLines adds an integer argument; its generated hook descriptor includes this argument. From 26.1 onward, target state and tag entries are independent and both are explicitly assigned to the same section. 26.3 adds player_speed and renames post_effect to post_effects; both variants have fixed placements.
