package io.picimako.drupal.context.treeview;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ConfigurationNodeConfigParser;
import io.picimako.drupal.context.Node;

/**
 * Validates the input component tree String for various violations.
 * <p>
 * This is used before the actual content creation, so that content creation won't even start if there is a problem
 * with the component tree definition.
 *
 * @see ComponentTreeBasedContentAssembler
 */
public class TreeViewBasedComponentTreeValidator {

    private static final String EXCEPTION_MESSAGE_FORMAT = "%s\nParent was: [%s]\nChild was: [%s]";
    private final TreeViewBasedNodeCreator nodeCreator = new TreeViewBasedNodeCreator();

    /**
     * Validates the argument component tree for violations.
     * <p>
     * The followings are considered as violations:
     * <ul>
     * <li>
     * Modifier nodes must be defined at the same level as the previous paragraph and modifier nodes,
     * thus if a modifier node is defined on a lower level than the previous component node, it will thrown an exception.
     * <p>
     * This, at the same time, restricts the modifier definition to happen right after a paragraph, or if there are
     * multiple modifiers, then to have all modifiers at the same level with the paragraph, e.g.:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * --- IMAGE
     * ---@ IMAGE_FX_MODIFIER
     * ---@ COLORS_MODIFIER
     * </pre>
     * </li>
     * <li>If the current node is more than one level deeper than the previous node, it throws an exception because
     * we don't allow specifying components more than one level deeper under a given node.
     * This is allowed:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * --- IMAGE
     * </pre>
     * while this is not valid:
     * <pre>
     * - CONTAINER
     * -- LAYOUT
     * ----- IMAGE
     * </pre></li>
     *
     * </ul>
     * ConfigurationNode validation is handled in {@link TreeViewBasedNodeCreator} and {@link ConfigurationNodeConfigParser}.
     *
     * @param componentTree
     */
    public void validateTree(String componentTree) {
        TreeViewAssemblerContext ctx = new TreeViewAssemblerContext();
        for (String line : componentTree.split("\n")) {
            Node node = nodeCreator.createNode(line);
            if (node instanceof ComponentNode) {
                ComponentNode currentNode = (ComponentNode) node;
                validateCurrentNode(currentNode, ctx.getPreviousComponentNode());
                ctx.setPreviousComponentNode(currentNode);
            }
        }
    }

    private void validateCurrentNode(ComponentNode currentNode, ComponentNode previousNode) {
        if (currentNode.isDeeperThan(previousNode)) {
            if (!currentNode.isOneLevelDeeperThan(previousNode)) {
                throwException("Child defined more than 1 level deeper than its immediate parent "
                    + "is not considered a valid child node.", previousNode, currentNode);
            }
        }
    }

    private void throwException(String message, ComponentNode previousNode, ComponentNode currentNode) {
        throw new IllegalArgumentException(String.format(EXCEPTION_MESSAGE_FORMAT, message, previousNode, currentNode));
    }
}
