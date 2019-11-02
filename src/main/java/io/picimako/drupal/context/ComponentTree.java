package io.picimako.drupal.context;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.Optional;

import static io.picimako.drupal.context.ComponentNode.ABSENT;

/**
 * An abstract representation of a component tree defined in Gherkin steps from which, after traversal,
 * component context selector can be built.
 * <p>
 * Under the hood it uses a directed {@link MutableGraph} (not allowing self-loops) to store the component nodes
 * and edges between them.
 * <p>
 * For this particular problem, in the tree each node has only one parent node.
 * <p>
 * {@link ConfigurationNode}s are not stored here, only {@link ComponentNode}s.
 */
public class ComponentTree {

    private final MutableGraph<ComponentNode> graph;
    private final ComponentTreeBranchTraverser branchTraverser;

    /**
     * Creates an empty, directed mutable graph that doesn't allow self-loops.
     */
    public ComponentTree() {
        graph = GraphBuilder.directed().allowsSelfLoops(false).build();
        branchTraverser = new ComponentTreeBranchTraverser(graph);
    }

    public MutableGraph<ComponentNode> getGraph() {
        return graph;
    }

    /**
     * Adds {@code currentNode} to the component tree, then based on what the {@code previousNode} was, creates an edge
     * (or not) between them:
     * <ol>
     *     <li>If the previous node is {@link ComponentNode#ABSENT}, it means that the current node is the first root element,
     *     thus edge creation is not needed, only the addition of the current node to the graph.</li>
     *     <li>If the current node is at the root level, then no edge is created since it has no parent node,
     *     but the occurrence of it at the root level still gets calculated.</li>
     *     <li>If the current node is one level deeper than the previous node, then it is an immediate child of it,
     *     thus an edge can be created between them.</li>
     *     <li>If the current node is on a higher level than the previous one, finds that parent of the previous node
     *     that is one level higher of the current node, and creates an edge between that parent and the current node.<br>
     *     It doesn't matter whether the current node is 1 or more levels higher than the previous node.<br>
     *     The last feasible parent can be a root level component, not higher than that.
     *     In that case it simply won't find a suitable parent.</li>
     *     <li>If the current and previous nodes are at the same level, then they are siblings, thus an edge can be
     *     created between the current node and the immediate parent of the previous node.</li>
     * </ol>
     *
     * @param currentNode  the node to process. At this point it is without any edge to other nodes.
     * @param previousNode the previously process node
     */
    public void addNode(ComponentNode currentNode, ComponentNode previousNode) {
        graph.addNode(currentNode);
        if (previousNode != ABSENT) { //1.
            if (currentNode.isAtRootLevel()) { //2.
                //Takes into account that if the current node is at root level, then no edge should be created.
                calculateRootLevelOccurrenceCount(currentNode);
            } else if (currentNode.isOneLevelDeeperThan(previousNode)) { //3.
                graph.putEdge(previousNode, currentNode);
            } else if (currentNode.isHigherThan(previousNode)) { //4.
                createEdgeWithCommonParent(previousNode, currentNode)
                        .ifPresent(parent -> calculateOccurrenceCountUnderParent(currentNode, parent));
            } else if (currentNode.isAtSameLevelAs(previousNode)) { //5.
                createEdgeWithCommonParent(previousNode, currentNode)
                        .ifPresent(parent -> calculateOccurrenceCountUnderParent(currentNode, parent));
            }
        }
    }

    private Optional<ComponentNode> createEdgeWithCommonParent(ComponentNode previousNode, ComponentNode currentNode) {
        Optional<ComponentNode> parent = branchTraverser.getPredecessorsFromBranchOf(previousNode)
                .stream()
                .filter(currentNode::isOneLevelDeeperThan)
                .findFirst();
        parent.ifPresent(parentNode -> graph.putEdge(parentNode, currentNode));
        return parent;
    }

    private void calculateRootLevelOccurrenceCount(ComponentNode currentNode) {
        long occurrenceCount = graph.nodes().stream()
                .filter(ComponentNode::isAtRootLevel)
                .filter(node -> node.hasSameTypeAs(currentNode))
                .count();
        currentNode.setOccurrenceCountUnderParent(occurrenceCount);
    }

    private void calculateOccurrenceCountUnderParent(ComponentNode currentNode, ComponentNode parent) {
        long occurrenceCount = graph.successors(parent)
                .stream()
                .filter(node -> node.hasSameTypeAs(currentNode))
                .count();
        currentNode.setOccurrenceCountUnderParent(occurrenceCount);
    }
}
