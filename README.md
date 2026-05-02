# Tucil3_13524014

Ice Sliding Puzzle Solver untuk IF2211 Strategi Algoritma.

Program menyediakan:
- CLI solver untuk UCS, GBFS, dan A*
- GUI JavaFX untuk memilih file, memilih algoritma, menjalankan solver, melihat hasil, playback solusi, dan menyimpan hasil
- Parser input `.txt`
- Heuristic H1, H2, H3 untuk GBFS dan A*

## Requirements

- Java 21
- Maven

Check installation:

```powershell
java -version
mvn -version
```

JavaFX dependency dikelola oleh Maven melalui `pom.xml`.

## Project Structure

Source code memakai layout Maven standar:

```text
src/main/java/
  Main.java
  cli/
  gui/
  model/
  parser/
  solver/
  util/
bin/
test/
doc/
README.md
pom.xml
```

Folder `src`, `bin`, `test`, dan `doc` tetap tersedia sesuai struktur tugas.

## Compile

```powershell
mvn clean compile
```

## Run CLI

Interactive:

```powershell
mvn exec:java
```

With input file argument:

```powershell
mvn exec:java "-Dexec.args=test\sample_valid.txt"
```

Then choose:
- Algorithm: `UCS`, `GBFS`, or `A*`
- Heuristic for GBFS/A*: `H1`, `H2`, or `H3`

## Run GUI

```powershell
mvn javafx:run
```

GUI usage:
1. Click `Choose File`.
2. Select a `.txt` puzzle input.
3. Choose algorithm: `UCS`, `GBFS`, or `A*`.
4. Choose heuristic for `GBFS` or `A*`.
5. Click `Solve`.
6. Use playback controls:
   - `Previous`
   - `Play/Pause`
   - `Next`
   - speed slider
   - jump-to-step input
7. Click `Save Results` to save solution and expanded iteration snapshots.

The heuristic dropdown is disabled for UCS because UCS uses `f(n) = g(n)`.

## CLI Playback

Raw arrow-key handling is not portable in standard Java console input, so CLI playback uses simple commands:

- `n` = next step
- `p` = previous step
- `j <step>` = jump to step number, for example `j 3`
- `q` = quit playback

When saving a solution in CLI, press Enter on the output path prompt to use the default path under `test/`.

## Algorithms

- UCS uses `f(n) = g(n)`.
- GBFS uses `f(n) = h(n)`.
- A* uses `f(n) = g(n) + h(n)`.

`g(n)` is accumulated movement cost. `h(n)` is estimated remaining cost.

Available heuristics:
- H1: Manhattan distance from current position to goal multiplied by minimum passable tile cost.
- H2: Manhattan distance from current position to the next mandatory target multiplied by minimum passable tile cost.
- H3: Manhattan chain through remaining checkpoints and then goal multiplied by minimum passable tile cost.

H1, H2, and H3 are not claimed universally admissible for every ice sliding case. Compare A* results against UCS as the optimal-cost baseline.

## Test Cases

```powershell
mvn exec:java "-Dexec.args=test\sample_valid.txt"
mvn exec:java "-Dexec.args=test\stage4_simple_no_checkpoint.txt"
mvn exec:java "-Dexec.args=test\stage4_checkpoint_vs_goal.txt"
mvn exec:java "-Dexec.args=test\stage4_weighted_greedy_trap.txt"
```
