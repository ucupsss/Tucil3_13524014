package util;

import solver.SearchNode;
import solver.SearchState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PathReconstructor {
    private PathReconstructor() {
    }

    public static List<SearchNode> reconstructNodes(SearchNode finalNode) {
        List<SearchNode> nodes = new ArrayList<SearchNode>();
        SearchNode current = finalNode;
        while (current != null) {
            nodes.add(current);
            current = current.getParent();
        }
        Collections.reverse(nodes);
        return nodes;
    }

    public static List<SearchState> reconstructStates(SearchNode finalNode) {
        List<SearchNode> nodes = reconstructNodes(finalNode);
        List<SearchState> states = new ArrayList<SearchState>();
        for (SearchNode node : nodes) {
            states.add(node.getState());
        }
        return states;
    }

    public static String reconstructMoves(SearchNode finalNode) {
        List<SearchNode> nodes = reconstructNodes(finalNode);
        StringBuilder builder = new StringBuilder();
        for (SearchNode node : nodes) {
            if (node.getMoveFromParent() != null) {
                builder.append(node.getMoveFromParent().getCode());
            }
        }
        return builder.toString();
    }
}
