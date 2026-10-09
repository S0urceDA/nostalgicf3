# Layout refactor measurements — 0.1.7

The refactor preserves 0.1.6's output while reducing the work performed by Nostalgic F3's layout assembly. These measurements cover local Java assembly, not Minecraft frame rate or complete F3 rendering. They do not compare against Better Vanilla F3.

## Changes

- Known entries use fixed indexed buffers and precomputed spacing/merge flags; no per-frame sorting, stream pipelines, event records or per-entry collector allocation.
- One collector, entry buffers, output columns and FPS StringBuilder are reused per overlay. Identifier-to-index lookup is cached; unknown group identifiers are used directly rather than converted to strings each frame.
- List copying avoids temporary toArray allocations. FPS text is built once instead of repeatedly concatenated, with the same Unicode whitespace behavior.
- Unknown groups retain insertion order and merge by group identifier. Their text buffers are pooled; the frame's identifier map is cleared each reset, avoiding accumulation of dynamic group IDs.
- Frame allocation is lazy. After old output is cleared, hidden frames return immediately from reset. References to vanilla's current displayer are released in finally, including entry failures.

Reusable columns are consumed synchronously by vanilla's line renderer; they must not be retained across reset. This was checked against the game renderer's line-reading loops. Buffer capacity is retained at its high-water mark to avoid repeated allocation, while clear releases references to old text and group keys.

## Local assembly benchmark

Windows, JDK 25.0.1; fixed 256 MiB initial / 512 MiB maximum heap. Fixture: all 35 policy IDs emit three rows each, plus two unknown groups and vanilla remainder text. This is a stress fixture, including aliases that ordinarily do not coexist in one game version. Input text is prebuilt, so formatting, measurements and font/GPU work are excluded. The old path includes its event/list capture; the new path uses warmed reusable buffers and precomputed indices. Collector dispatch and identifier-cache lookup are not timed.

20,000 paired warm-up frames, then 25,000 frames per measurement; median of seven alternating before/after rounds. Allocation is measured with ThreadMXBean on the benchmark thread. Time includes ordinary JVM noise and garbage collection.

| Measurement | 0.1.6 | 0.1.7 | Reduction |
| --- | ---: | ---: | ---: |
| Layout assembly | 7,421 ns/frame | 1,243 ns/frame | 83.3% |
| Allocation | 31,328 bytes/frame | 408 bytes/frame | 98.7% |

The earlier run was 7,465 / 1,291 ns and the same allocation counts. Treat timings as approximate; this is a lightweight local benchmark rather than JMH or an in-game profile. Remaining allocations include completed merged text and unknown-group map nodes. Vanilla debug entry formatting and rendering still allocate/work normally.

Run `gradlew layoutBenchmark --no-daemon` to repeat. The benchmark is opt-in and is not run by ordinary builds or shipped in release jars.

## Correctness and compatibility

- The existing 2,048 visibility fixtures and targeted spacing/disabled-entry/coordinate tests pass.
- 12,000 randomized complete-row comparisons match the preserved 0.1.6 implementation. They include changing visibility across reused frames, repeated entry output, empty/merged mod groups, footer variants and Unicode whitespace.
- All five release targets build with existing Loader and Java requirements.
- Real Fabric probes transform every hook and exercise the reused collector on every target: known capture, unknown priority/ordinary forwarding, named group merging, reset, collector identity, cleared displayer references and the lazy empty-overlay path.
- Test references, benchmark code and constructor-free probe helpers are excluded from release jars. The layout has been visually tested by the user in game. No in-game FPS improvement is claimed; the benchmark measures layout assembly only.
