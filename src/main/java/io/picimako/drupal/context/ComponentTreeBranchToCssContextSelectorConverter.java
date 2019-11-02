package io.picimako.drupal.context;

import com.google.common.graph.Traverser;

import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Traverses the current branch of the tree and builds a CSS selector representing the current context on the
 * editor.
 * <p>
 * Below are some examples (based on the dummy selectors in {@link ComponentContextSelector}) for input tree branches
 * and output CSS selectors:
 * <pre>
 * Input:
 * - CONTAINER
 *
 * Output: .container:nth-child(1)
 * </pre>
 * <pre>
 * Input:
 * - CONTAINER
 * -- LAYOUT
 * --- IMAGE
 *
 * Output: .container:nth-child(1) .layout .image-component:nth-child(1)
 * </pre>
 * <pre>
 * Input:
 * - CONTAINER
 * -- LAYOUT
 * --- IMAGE
 * --- IMAGE  <- This is the current leaf node.
 *
 * Output: .container:nth-child(1) .layout .image-component:nth-child(2)  <- Note the altered index.
 * </pre>
 * If the tree contains more than one Container components, and the traversal is at the 2nd one,
 * it would alter the CSS selector in the following way:
 * <pre>
 * Input:
 * - CONTAINER
 *
 * Output: .container:nth-child(2)  <- Note the altered index.
 * </pre>
 */
public class ComponentTreeBranchToCssContextSelectorConverter {

    private final CssContextSelectorAssembler selectorAssembler = new CssContextSelectorAssembler();

    /**
     * Traverses the argument {@code componentTree}, and builds a CSS selector representing the current context
     * to work with.
     * <p>
     * The tree is traversed in a depth-first pre-order way, and only the currently used branch whose starting point is
     * determined by the following logic:
     * <p>
     * Get all the predecessors of the currently examined node, then get the one from them that is at the root level.
     * There must be only one such node.
     * <p>
     * If the current node is a root level one, then no traversal is needed because it is the only node
     * in that branch yet.
     *
     * @param componentTree the tree to traverse
     * @param currentNode   the node which marks the branch to traverse
     * @return the built CSS context selector
     * @see Traverser#depthFirstPreOrder(Object)
     */
    public String convert(ComponentTree componentTree, ComponentNode currentNode) {
        List<ComponentNode> nodes;
        if (currentNode.isAtRootLevel()) {
            nodes = singletonList(currentNode);
        } else {
            nodes = ComponentTreeBranchTraverser.on(componentTree.getGraph()).getAllNodesFromBranchOfLeaf(currentNode);
        }
        return selectorAssembler.createCssContextSelectorFrom(nodes);
    }
}
