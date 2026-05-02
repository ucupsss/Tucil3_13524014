package cli;

import model.Puzzle;
import solver.SearchNode;
import solver.SearchResult;
import util.BoardRenderer;

import java.util.List;
import java.util.Scanner;

public final class PlaybackController {
    private final Puzzle puzzle;
    private final SearchResult result;
    private final Scanner scanner;

    public PlaybackController(Puzzle puzzle, SearchResult result, Scanner scanner) {
        this.puzzle = puzzle;
        this.result = result;
        this.scanner = scanner;
    }

    public void run() {
        if (!result.isFound()) {
            System.out.println("Playback tidak tersedia karena solusi tidak ditemukan.");
            return;
        }

        List<SearchNode> path = result.getSolutionPath();
        int currentStep = 0;
        printHelp(path.size() - 1);
        printStep(path, currentStep);

        while (true) {
            System.out.print("playback> ");
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("q")) {
                System.out.println("Playback selesai.");
                return;
            }
            if (command.equalsIgnoreCase("n")) {
                if (currentStep < path.size() - 1) {
                    currentStep++;
                } else {
                    System.out.println("Sudah di step terakhir.");
                }
                printStep(path, currentStep);
                continue;
            }
            if (command.equalsIgnoreCase("p")) {
                if (currentStep > 0) {
                    currentStep--;
                } else {
                    System.out.println("Sudah di initial step.");
                }
                printStep(path, currentStep);
                continue;
            }
            if (command.toLowerCase().startsWith("j")) {
                Integer targetStep = parseJumpTarget(command);
                if (targetStep == null) {
                    System.out.println("Format jump tidak valid. Gunakan: j <nomor_step>");
                } else if (targetStep < 0 || targetStep >= path.size()) {
                    System.out.println("Step harus berada di antara 0 dan " + (path.size() - 1) + ".");
                } else {
                    currentStep = targetStep;
                    printStep(path, currentStep);
                }
                continue;
            }

            System.out.println("Command tidak dikenal. Gunakan n, p, j <nomor_step>, atau q.");
        }
    }

    private void printHelp(int maxStep) {
        System.out.println();
        System.out.println("Playback command: n = next, p = previous, j <step> = jump, q = quit.");
        System.out.println("Step tersedia: 0 sampai " + maxStep + ". Step 0 adalah Initial.");
    }

    private void printStep(List<SearchNode> path, int step) {
        SearchNode node = path.get(step);
        System.out.println();
        if (step == 0) {
            System.out.println("Initial");
        } else {
            System.out.println("Step " + step + " : " + node.getMoveFromParent().getCode());
        }
        System.out.println(BoardRenderer.renderState(puzzle, node.getState()));
    }

    private Integer parseJumpTarget(String command) {
        String[] parts = command.trim().split("\\s+");
        if (parts.length != 2 || !parts[0].equalsIgnoreCase("j")) {
            return null;
        }
        try {
            return Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
