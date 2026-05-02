# Tucil3_13524014
Tugas Kecil 3 Strategi Algoritma Ice Sliding Puzzle Solver

## Current Stage

Implemented:
- Project folders: `src`, `bin`, `test`, `doc`
- Core model classes for puzzle, board, position, direction, and tile type
- Text input parser with validation and clear error messages
- Uniform Cost Search solver

Compile:

```powershell
javac -d bin src\Main.java src\model\*.java src\parser\*.java src\solver\*.java src\util\*.java
```

Run UCS smoke test:

```powershell
java -cp bin Main test\sample_valid.txt
```

Useful Stage 4 comparison cases:

```powershell
java -cp bin Main test\stage4_simple_no_checkpoint.txt
java -cp bin Main test\stage4_checkpoint_vs_goal.txt
java -cp bin Main test\stage4_weighted_greedy_trap.txt
```

Run validation smoke test:

```powershell
java -cp bin Main test\sample_invalid_checkpoint_gap.txt
```
