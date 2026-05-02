package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Puzzle;
import parser.PuzzleParser;
import solver.AStarSolver;
import solver.GreedyBestFirstSolver;
import solver.Heuristic;
import solver.HeuristicType;
import solver.Heuristics;
import solver.SearchAlgorithm;
import solver.SearchResult;
import solver.UniformCostSolver;
import util.SolutionWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public final class IceSlidingApp extends Application {
    private static final String SURFACE = "#f6fafe";
    private static final String SURFACE_LOW = "#f0f4f8";
    private static final String SURFACE_CONTAINER = "#eaeef2";
    private static final String OUTLINE = "#c1c6d7";
    private static final String PRIMARY = "#0058be";
    private static final String SECONDARY = "#00677e";
    private static final String TEXT = "#171c1f";
    private static final String DISPLAY_FONT = "Bahnschrift";
    private static final String BODY_FONT = "Segoe UI";
    private static final String MONO_FONT = "Cascadia Code";

    private final BoardView boardView = new BoardView();
    private final Label selectedFileLabel = new Label("No file selected");
    private final Label statusLabel = new Label("Status: Ready");
    private final ComboBox<String> algorithmBox = new ComboBox<String>();
    private final ComboBox<HeuristicType> heuristicBox = new ComboBox<HeuristicType>();
    private final Button chooseFileButton = new Button("Choose File");
    private final Button solveButton = new Button("Solve");
    private final Button previousButton = new Button("Previous");
    private final Button playPauseButton = new Button("Play");
    private final Button nextButton = new Button("Next");
    private final Button jumpButton = new Button("Jump");
    private final Button saveButton = new Button("Save Results");
    private final TextField jumpField = new TextField();
    private final Slider speedSlider = new Slider(0.5, 3.0, 1.0);
    private final Label speedLabel = new Label("1.0x");
    private final Label solutionLabel = new Label("-");
    private final Label costLabel = new Label("-");
    private final Label iterationsLabel = new Label("-");
    private final Label timeLabel = new Label("-");
    private final Label algorithmResultLabel = new Label("-");
    private final Label heuristicResultLabel = new Label("-");
    private final Label stepLabel = new Label("Step 0 / 0");

    private Stage stage;
    private File selectedFile;
    private Puzzle currentPuzzle;
    private SearchResult currentResult;
    private SearchAlgorithm currentAlgorithm;
    private HeuristicType currentHeuristic;
    private Path currentInputPath;
    private StepPlaybackModel playbackModel = new StepPlaybackModel(null);
    private Timeline timeline;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        BorderPane root = new BorderPane();
        root.setTop(createTopArea());
        root.setCenter(createCenterArea());
        root.setRight(createResultPanel());
        root.setBottom(createPlaybackBar());
        root.setStyle("-fx-background-color: " + SURFACE_LOW + "; " + bodyFont());

        configureControls();
        setPlaybackEnabled(false);
        saveButton.setDisable(true);

        Scene scene = new Scene(root, 1200, 780);
        primaryStage.setTitle("Ice Sliding Puzzle Solver");
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createTopArea() {
        Label title = new Label("ICE SLIDING PUZZLE SOLVER");
        title.setStyle(displayFont() + "-fx-font-size: 22px; -fx-font-weight: 800; -fx-letter-spacing: 1px; -fx-text-fill: " + TEXT + ";");
        Label mark = new Label("*");
        mark.setStyle(displayFont() + "-fx-font-size: 30px; -fx-font-weight: 900; -fx-text-fill: " + PRIMARY + ";");
        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);

        HBox titleBar = new HBox(12, mark, title, titleSpacer, statusLabel);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(12, 22, 12, 22));
        titleBar.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: transparent transparent #d1dbe5 transparent;");

        Label algorithmLabel = smallCaps("Algorithm:");
        Label heuristicLabel = smallCaps("Heuristic:");
        selectedFileLabel.setStyle(bodyFont() + "-fx-font-style: italic; -fx-text-fill: #414755;");
        Region setupSpacer = new Region();
        HBox.setHgrow(setupSpacer, Priority.ALWAYS);

        HBox setupBar = new HBox(14,
                chooseFileButton,
                selectedFileLabel,
                separator(),
                algorithmLabel,
                algorithmBox,
                heuristicLabel,
                heuristicBox,
                solveButton,
                setupSpacer);
        setupBar.setAlignment(Pos.CENTER_LEFT);
        setupBar.setPadding(new Insets(10, 22, 10, 22));
        setupBar.setStyle("-fx-background-color: " + SURFACE_CONTAINER + "; -fx-border-color: transparent transparent " + OUTLINE + " transparent;");
        algorithmBox.setStyle(displayFont() + "-fx-font-size: 12px;");
        heuristicBox.setStyle(displayFont() + "-fx-font-size: 12px;");

        VBox top = new VBox(titleBar, setupBar);
        stylePrimaryButton(chooseFileButton);
        styleActionButton(solveButton);
        return top;
    }

    private BorderPane createCenterArea() {
        BorderPane pane = new BorderPane(boardView);
        pane.setPadding(new Insets(18));
        pane.setStyle("-fx-background-color: " + SURFACE_LOW + ";");
        return pane;
    }

    private VBox createResultPanel() {
        Label title = new Label("Result Panel");
        title.setStyle(displayFont() + "-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: " + TEXT + ";");

        VBox solutionCard = card();
        solutionCard.getChildren().addAll(smallCaps("Solution Path"), solutionLabel);
        solutionLabel.setWrapText(true);
        solutionLabel.setStyle(monoFont() + "-fx-font-size: 15px; -fx-padding: 8; -fx-background-color: " + SURFACE + "; -fx-border-color: " + OUTLINE + ";");

        GridPane metricGrid = new GridPane();
        metricGrid.setHgap(10);
        metricGrid.setVgap(10);
        metricGrid.add(metricCard("Total Cost", costLabel), 0, 0);
        metricGrid.add(metricCard("Step", stepLabel), 1, 0);

        VBox details = card();
        details.getChildren().addAll(
                statRow("Algorithm", algorithmResultLabel),
                statRow("Heuristic", heuristicResultLabel),
                statRow("Iterations", iterationsLabel),
                statRow("Exec Time", timeLabel));

        VBox panel = new VBox(18, title, solutionCard, metricGrid, details);
        panel.setPrefWidth(310);
        panel.setPadding(new Insets(22));
        panel.setStyle("-fx-background-color: " + SURFACE + "; -fx-border-color: transparent transparent transparent " + OUTLINE + ";");
        return panel;
    }

    private HBox createPlaybackBar() {
        styleSecondaryButton(previousButton);
        stylePrimaryRoundButton(playPauseButton);
        styleSecondaryButton(nextButton);
        styleSecondaryButton(jumpButton);
        styleSecondaryButton(saveButton);

        speedSlider.setShowTickMarks(false);
        speedSlider.setShowTickLabels(false);
        speedSlider.setPrefWidth(160);
        jumpField.setPrefWidth(70);
        jumpField.setAlignment(Pos.CENTER);
        jumpField.setPromptText("target");
        jumpField.setStyle(monoFont() + "-fx-font-size: 13px;");

        HBox buttons = new HBox(8, previousButton, playPauseButton, nextButton);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox speedBox = new VBox(4, new HBox(8, smallCaps("Speed"), speedLabel), speedSlider);
        speedBox.setAlignment(Pos.CENTER_LEFT);
        speedLabel.setStyle(monoFont() + "-fx-text-fill: #414755;");

        Region spacerLeft = new Region();
        Region spacerRight = new Region();
        HBox.setHgrow(spacerLeft, Priority.ALWAYS);
        HBox.setHgrow(spacerRight, Priority.ALWAYS);

        HBox jumpBox = new HBox(8, smallCaps("Jump to"), jumpField, jumpButton);
        jumpBox.setAlignment(Pos.CENTER);
        jumpBox.setPadding(new Insets(0, 0, 0, 8));

        HBox bottom = new HBox(18, buttons, speedBox, spacerLeft, jumpBox, spacerRight, saveButton);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setPadding(new Insets(12, 22, 12, 22));
        bottom.setStyle("-fx-background-color: " + SURFACE_CONTAINER + "; -fx-border-color: " + OUTLINE + " transparent transparent transparent;");
        return bottom;
    }

    private void configureControls() {
        algorithmBox.getItems().addAll("UCS", "GBFS", "A*");
        algorithmBox.setValue("A*");
        heuristicBox.getItems().addAll(HeuristicType.values());
        heuristicBox.setValue(HeuristicType.H2);
        algorithmBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!solveButton.isDisable()) {
                heuristicBox.setDisable("UCS".equals(newValue));
            }
        });
        heuristicBox.setDisable(false);

        chooseFileButton.setOnAction(event -> chooseFile());
        solveButton.setOnAction(event -> solve());
        previousButton.setOnAction(event -> moveStep(-1));
        nextButton.setOnAction(event -> moveStep(1));
        jumpButton.setOnAction(event -> jumpToStep());
        playPauseButton.setOnAction(event -> togglePlayback());
        saveButton.setOnAction(event -> saveResults());
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            speedLabel.setText(String.format("%.1fx", newValue.doubleValue()));
            if (timeline != null) {
                timeline.setRate(newValue.doubleValue());
            }
        });

        timeline = new Timeline(new KeyFrame(Duration.millis(850), event -> {
            if (!playbackModel.hasSolution() || playbackModel.getCurrentStep() >= playbackModel.getMaxStep()) {
                pausePlayback();
                return;
            }
            playbackModel.next();
            refreshPlaybackView();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.setRate(speedSlider.getValue());
    }

    private void chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose puzzle input file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
            statusLabel.setText("Status: Ready");
            currentPuzzle = null;
            currentResult = null;
            currentInputPath = null;
            playbackModel = new StepPlaybackModel(null);
            boardView.showPuzzle(null);
            solutionLabel.setText("-");
            costLabel.setText("-");
            iterationsLabel.setText("-");
            timeLabel.setText("-");
            stepLabel.setText("Step 0 / 0");
            jumpField.clear();
            setPlaybackEnabled(false);
            saveButton.setDisable(true);
        }
    }

    private void solve() {
        if (selectedFile == null) {
            showAlert(Alert.AlertType.WARNING, "No file selected", "Choose a .txt input file first.");
            return;
        }

        SearchAlgorithm algorithm = SearchAlgorithm.fromInput(algorithmBox.getValue());
        HeuristicType heuristicType = algorithm == SearchAlgorithm.UCS ? null : heuristicBox.getValue();
        File inputFile = selectedFile;

        setSolving(true);
        statusLabel.setText("Status: Solving...");
        currentResult = null;
        saveButton.setDisable(true);
        setPlaybackEnabled(false);
        pausePlayback();

        Task<SolveOutput> task = new Task<SolveOutput>() {
            @Override
            protected SolveOutput call() throws Exception {
                Puzzle puzzle = new PuzzleParser().parse(inputFile.toPath());
                SearchResult result = runSearch(puzzle, algorithm, heuristicType);
                return new SolveOutput(puzzle, result, algorithm, heuristicType, inputFile.toPath());
            }
        };

        task.setOnSucceeded(event -> {
            setSolving(false);
            SolveOutput output = task.getValue();
            applyResult(output);
        });
        task.setOnFailed(event -> {
            setSolving(false);
            Throwable exception = task.getException();
            if (exception instanceof parser.InvalidPuzzleException) {
                statusLabel.setText("Status: Invalid input");
                showAlert(Alert.AlertType.ERROR, "Invalid input", exception.getMessage());
            } else {
                statusLabel.setText("Status: Error");
                showAlert(Alert.AlertType.ERROR, "Solver error", exception == null ? "Unknown error." : exception.getMessage());
            }
        });

        Thread worker = new Thread(task, "gui-solver-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private SearchResult runSearch(Puzzle puzzle, SearchAlgorithm algorithm, HeuristicType heuristicType) {
        if (algorithm == SearchAlgorithm.UCS) {
            return new UniformCostSolver().solve(puzzle);
        }
        Heuristic heuristic = Heuristics.create(heuristicType, puzzle);
        if (algorithm == SearchAlgorithm.GBFS) {
            return new GreedyBestFirstSolver(heuristic).solve(puzzle);
        }
        return new AStarSolver(heuristic).solve(puzzle);
    }

    private void applyResult(SolveOutput output) {
        currentPuzzle = output.puzzle;
        currentResult = output.result;
        currentAlgorithm = output.algorithm;
        currentHeuristic = output.heuristicType;
        currentInputPath = output.inputPath;
        playbackModel = new StepPlaybackModel(currentResult);
        jumpField.clear();

        algorithmResultLabel.setText(currentAlgorithm.getDisplayName());
        heuristicResultLabel.setText(currentHeuristic == null ? "-" : currentHeuristic.name());
        solutionLabel.setText(currentResult.isFound() ? currentResult.getSolutionMoves() : "-");
        costLabel.setText(currentResult.isFound() ? String.valueOf(currentResult.getTotalCost()) : "-");
        iterationsLabel.setText(currentResult.getIterationCount() + " iterasi");
        timeLabel.setText(currentResult.getExecutionTimeMillis() + " ms");

        if (currentResult.isFound()) {
            statusLabel.setText("Status: Solved");
            setPlaybackEnabled(true);
            saveButton.setDisable(false);
            refreshPlaybackView();
        } else {
            statusLabel.setText("Status: No solution");
            setPlaybackEnabled(false);
            saveButton.setDisable(false);
            boardView.showPuzzle(currentPuzzle);
            stepLabel.setText("Step 0 / 0");
            jumpField.clear();
            showAlert(Alert.AlertType.INFORMATION, "No solution found", "The selected solver did not find a valid solution.");
        }
    }

    private void moveStep(int delta) {
        if (!playbackModel.hasSolution()) {
            return;
        }
        playbackModel.jumpTo(playbackModel.getCurrentStep() + delta);
        refreshPlaybackView();
    }

    private void jumpToStep() {
        if (!playbackModel.hasSolution()) {
            return;
        }
        try {
            int target = Integer.parseInt(jumpField.getText().trim());
            playbackModel.jumpTo(target);
            refreshPlaybackView();
        } catch (NumberFormatException exception) {
            showAlert(Alert.AlertType.WARNING, "Invalid step", "Enter a valid step number.");
        }
    }

    private void togglePlayback() {
        if (!playbackModel.hasSolution()) {
            return;
        }
        if (timeline.getStatus() == Timeline.Status.RUNNING) {
            pausePlayback();
        } else {
            if (playbackModel.getCurrentStep() >= playbackModel.getMaxStep()) {
                playbackModel.jumpTo(0);
                refreshPlaybackView();
            }
            playPauseButton.setText("Pause");
            timeline.play();
        }
    }

    private void pausePlayback() {
        if (timeline != null) {
            timeline.stop();
        }
        playPauseButton.setText("Play");
    }

    private void refreshPlaybackView() {
        if (!playbackModel.hasSolution() || currentPuzzle == null) {
            return;
        }
        boardView.showStep(currentPuzzle, playbackModel.getCurrentNode());
        stepLabel.setText("Step " + playbackModel.getCurrentStep() + " / " + playbackModel.getMaxStep());
        jumpField.setPromptText("0-" + playbackModel.getMaxStep());
    }

    private void saveResults() {
        if (currentPuzzle == null || currentResult == null) {
            showAlert(Alert.AlertType.WARNING, "Nothing to save", "Solve a puzzle before saving results.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save solution");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        chooser.setInitialFileName(defaultSolutionFileName());
        File file = chooser.showSaveDialog(stage);
        if (file == null) {
            return;
        }

        Path solutionPath = file.toPath();
        Path iterationPath = iterationPathFor(solutionPath);
        SolutionWriter writer = new SolutionWriter();
        try {
            writer.writeSolution(solutionPath, currentInputPath, currentPuzzle, currentAlgorithm, currentHeuristic, currentResult);
            writer.writeExploredSnapshots(iterationPath, currentInputPath, currentPuzzle, currentAlgorithm, currentHeuristic, currentResult);
            showAlert(Alert.AlertType.INFORMATION, "Saved", "Saved solution and iteration snapshots.");
        } catch (IOException exception) {
            showAlert(Alert.AlertType.ERROR, "Save failed", exception.getMessage());
        }
    }

    private String defaultSolutionFileName() {
        String algorithmName = currentAlgorithm == null ? "solver" : currentAlgorithm.getDisplayName().replace("*", "star");
        String heuristicName = currentHeuristic == null ? "" : "_" + currentHeuristic;
        return "solution_" + algorithmName + heuristicName + ".txt";
    }

    private Path iterationPathFor(Path solutionPath) {
        String fileName = solutionPath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        String base = dotIndex <= 0 ? fileName : fileName.substring(0, dotIndex);
        Path parent = solutionPath.getParent();
        if (parent == null) {
            return Path.of(base + "_iterations.txt");
        }
        return parent.resolve(base + "_iterations.txt");
    }

    private void setSolving(boolean solving) {
        chooseFileButton.setDisable(solving);
        algorithmBox.setDisable(solving);
        heuristicBox.setDisable(solving || "UCS".equals(algorithmBox.getValue()));
        solveButton.setDisable(solving);
        saveButton.setDisable(solving || currentResult == null);
    }

    private void setPlaybackEnabled(boolean enabled) {
        previousButton.setDisable(!enabled);
        playPauseButton.setDisable(!enabled);
        nextButton.setDisable(!enabled);
        jumpField.setDisable(!enabled);
        jumpButton.setDisable(!enabled);
        if (!enabled) {
            pausePlayback();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(title);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private VBox card() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14));
        box.setStyle("-fx-background-color: " + SURFACE_LOW + "; -fx-border-color: " + OUTLINE + "; -fx-background-radius: 6; -fx-border-radius: 6;");
        return box;
    }

    private VBox metricCard(String title, Label value) {
        VBox box = card();
        value.setStyle(monoFont() + "-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: " + PRIMARY + ";");
        box.getChildren().addAll(smallCaps(title), value);
        box.setMinWidth(120);
        return box;
    }

    private HBox statRow(String title, Label value) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle(bodyFont() + "-fx-text-fill: #414755;");
        value.setStyle(monoFont() + "-fx-font-weight: 800; -fx-text-fill: " + TEXT + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox row = new HBox(8, titleLabel, spacer, value);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label smallCaps(String text) {
        Label label = new Label(text);
        label.setStyle(displayFont() + "-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #414755;");
        return label;
    }

    private Region separator() {
        Region region = new Region();
        region.setPrefWidth(1);
        region.setMinWidth(1);
        region.setMaxWidth(1);
        region.setPrefHeight(28);
        region.setStyle("-fx-background-color: " + OUTLINE + ";");
        return region;
    }

    private void stylePrimaryButton(Button button) {
        button.setStyle(displayFont() + "-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 5; -fx-padding: 8 14;");
    }

    private void styleActionButton(Button button) {
        button.setStyle(displayFont() + "-fx-background-color: " + SECONDARY + "; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 5; -fx-padding: 8 22;");
    }

    private void styleSecondaryButton(Button button) {
        button.setStyle(displayFont() + "-fx-background-color: " + SURFACE + "; -fx-text-fill: " + PRIMARY + "; -fx-border-color: " + PRIMARY + "; -fx-background-radius: 5; -fx-border-radius: 5; -fx-padding: 7 12;");
    }

    private void stylePrimaryRoundButton(Button button) {
        button.setStyle(displayFont() + "-fx-background-color: " + PRIMARY + "; -fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 18; -fx-padding: 8 18;");
    }

    private String displayFont() {
        return "-fx-font-family: '" + DISPLAY_FONT + "'; ";
    }

    private String bodyFont() {
        return "-fx-font-family: '" + BODY_FONT + "'; ";
    }

    private String monoFont() {
        return "-fx-font-family: '" + MONO_FONT + "'; ";
    }

    private static final class SolveOutput {
        private final Puzzle puzzle;
        private final SearchResult result;
        private final SearchAlgorithm algorithm;
        private final HeuristicType heuristicType;
        private final Path inputPath;

        private SolveOutput(
                Puzzle puzzle,
                SearchResult result,
                SearchAlgorithm algorithm,
                HeuristicType heuristicType,
                Path inputPath
        ) {
            this.puzzle = puzzle;
            this.result = result;
            this.algorithm = algorithm;
            this.heuristicType = heuristicType;
            this.inputPath = inputPath;
        }
    }
}
