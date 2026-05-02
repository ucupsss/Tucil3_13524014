package gui;

import solver.SearchNode;
import solver.SearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StepPlaybackModel {
    private final List<SearchNode> steps;
    private int currentStep;

    public StepPlaybackModel(SearchResult result) {
        if (result != null && result.isFound()) {
            this.steps = result.getSolutionPath();
        } else {
            this.steps = Collections.emptyList();
        }
        this.currentStep = 0;
    }

    public boolean hasSolution() {
        return !steps.isEmpty();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public int getMaxStep() {
        return Math.max(0, steps.size() - 1);
    }

    public SearchNode getCurrentNode() {
        if (!hasSolution()) {
            return null;
        }
        return steps.get(currentStep);
    }

    public List<SearchNode> getSteps() {
        return Collections.unmodifiableList(new ArrayList<SearchNode>(steps));
    }

    public void next() {
        jumpTo(currentStep + 1);
    }

    public void previous() {
        jumpTo(currentStep - 1);
    }

    public void jumpTo(int step) {
        if (!hasSolution()) {
            currentStep = 0;
            return;
        }
        currentStep = Math.max(0, Math.min(step, getMaxStep()));
    }
}
