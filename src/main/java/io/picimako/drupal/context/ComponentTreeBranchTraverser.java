package io.picimako.drupal.context;

import com.google.common.graph.MutableGraph;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Traverses a certain branch of the underlying graph starting from a specified node,
 * and collects them into a collection.
 */
public class ComponentTreeBranchTraverser {

    private final MutableGraph<ComponentNode> graph;

    public ComponentTreeBranchTraverser(MutableGraph<ComponentNode> graph) {
        this.graph = graph;
    }

    /**
     * Returns all predecessors on all levels of the argument node.
     * <p>
     * In this component tree structure it means that it collects the nodes from the branch of that
     * node and returns it as a {@link Set} of nodes.
     * <p>
     * This logic is necessary since {@link com.google.common.graph.Graph#predecessors(Object)} returns only the direct
     * predecessors of a particular node.
     * <p>
     * In case of the following tree:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * --- IMAGE
     * --- RICH_TEXT
     * </pre>
     * where RICH_TEXT is the argument node, the branch nodes will be: LAYOUT, CONTAINER.
     * (It doesn't include the current node.)
     * <p>
     * In case the argument node is at root level and doesn't have a predecessor, this method returns an empty list.
     *
     * @param node the node whose branch nodes is collected
     * @return the set of nodes
     */
    public List<ComponentNode> getPredecessorsFromBranchOf(ComponentNode node) {
        List<ComponentNode> predecessors = new LinkedList<>();
        ComponentNode upMostPredecessor = node;
        while (hasPredecessor(upMostPredecessor)) {
            ComponentNode predecessor = getOnlyPredecessorOf(upMostPredecessor);
            predecessors.add(predecessor);
            upMostPredecessor = predecessor;
        }
        return predecessors;
    }

    /**
     * Gets all predecessor nodes of the argument node and {@code node} as a list.
     * <p>
     * Any successors of {@code node} are ignored.
     * <p>
     * If you have the following tree:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * --- IMAGE  <- this is the current node
     * </pre>
     * the list of nodes will be: CONTAINER, LAYOUT, IMAGE.
     * <p>
     * But if you have the following one:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * --- IMAGE
     * --- YOUTUBE_VIDEO  <- this is the current node
     * </pre>
     * the list of nodes will be: CONTAINER, LAYOUT, YOUTUBE_VIDEO.
     *
     * @param node the node to start the collection from
     * @return the list of nodes from the branch of {@code node}, except its successors
     */
    public List<ComponentNode> getAllNodesFromBranchOfLeaf(ComponentNode node) {
        List<ComponentNode> nodes = getPredecessorsFromBranchOf(node);
        Collections.reverse(nodes);
        nodes.add(node);
        return nodes;
    }

    public static ComponentTreeBranchTraverser on(MutableGraph<ComponentNode> graph) {
        return new ComponentTreeBranchTraverser(graph);
    }

    private boolean hasPredecessor(ComponentNode node) {
        return !graph.predecessors(node).isEmpty();
    }

    private ComponentNode getOnlyPredecessorOf(ComponentNode node) {
        return graph.predecessors(node).iterator().next();
    }
}
