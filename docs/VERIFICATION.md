# Verification

17 October 2026. Windows, Oracle JDK/javac 23.0.2; compile target Java 8. Python 3.10.9 drives the smoke helper, and Pillow 12.0.0 is used only for the transcript rendering.

```powershell
python -B scripts/smoke.py
```

The helper resolves `java.home` then uses that JDK's actual `javac.exe` and `java.exe` directly. It compiles every `src/main/java/*.java` file with `--release 8 -encoding UTF-8` into a temporary directory, executes `SwiftCartMain` for a 45-second observation window, then terminates its own JVM and removes the temporary classes. Default sources/constants are unchanged. No application dependencies were installed.

Result: compilation passed; real synthetic intake, picking, packing, labelling and sorting activity appeared in stdout. The helper's assertions confirm key stage logs and reject unexpected stderr. Selected event excerpts and matching-line counts are in `docs/assets/smoke-transcript.txt`. Random outcomes and interleavings vary; counts are observations, not unique-order accounting or a performance benchmark.

Capture command:

```powershell
python -B scripts/smoke.py | Set-Content -Encoding utf8 docs/assets/smoke-transcript.txt
```

`docs/assets/smoke-run.png` is a Pillow/Consolas rendering of a compact excerpt from that actual transcript, explicitly labeled as a rendering. No native terminal/GUI screenshot is claimed.

The first observation used the Windows `javapath` launcher. Its child JVM survived termination of the launcher, so that run exceeded the intended window; the exact child was identified and stopped, the helper was corrected to launch the real JDK executable, and the bounded evidence was recaptured. No application behavior was changed.

The normal five-minute shutdown, completion of all 600 orders, lossless draining, failure-free resource release and accuracy of the final statistical report were not tested. Source inspection shows unbounded inter-stage queues, a stale Maven `exec.mainClass`, hard-coded received-order aggregation and calculated stage rejection counts. These limits are visible in the README; no full-throughput or production-correctness claim is made. There is no automated test suite in this snapshot.
