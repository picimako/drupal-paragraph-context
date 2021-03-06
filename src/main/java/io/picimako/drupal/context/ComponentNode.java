package io.picimako.drupal.context;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static io.picimako.drupal.context.util.Preconditions.check;
import static java.util.Objects.requireNonNull;

/**
 * Represents a Component (paragraph or modifier) in the component tree, storing
 * <ul>
 *     <li>the level of the component to be able to properly link parent and child components,
 *     and calculate occurrence count,</li>
 *     <li>the type of the component, so that they can be converted to {@link ComponentContextSelector}s,</li>
 *     <li>the occurrence count under its parent, so that the component context selectors can be indexed properly.</li>
 *     <li>whether this node is a modifier or a paragraph</li>
 *     <li>the inline configuration in case of tree view based component tree</li>
 * </ul>
 */
@Getter
@ToString
public class ComponentNode implements Node {
    public static final ComponentNode ABSENT = new ComponentNode();

    private final int level;
    private final NodeType type;
    private long occurrenceCountUnderParent = 1;
    @Setter
    private boolean isModifierNode;
    @Setter
    private ConfigurationNode inlineConfig = ConfigurationNode.EMPTY;

    private ComponentNode() {
        this.level = 0;
        this.type = ParagraphNodeType.ABSENT;
    }

    /**
     * Creates a new {@link ComponentNode}.
     *
     * @param level the level on which the node is placed
     * @param type  the type of the node
     * @throws IllegalArgumentException when the provided node level is less than 1
     */
    public ComponentNode(int level, NodeType type) {
        check(level > 0, "Node level should be at least one. It was: [" + level + "]");
        this.level = level;
        this.type = requireNonNull(type);
    }

    /**
     * Sets the occurrence count of this node under its parent.
     *
     * @param occurrenceCountUnderParent the occurrence count
     * @throws IllegalArgumentException when the occurrence count is less than 1
     */
    public void setOccurrenceCountUnderParent(long occurrenceCountUnderParent) {
        check(occurrenceCountUnderParent > 0, "Occurrence count under parent should be "
            + "greater than 0. It was: [" + occurrenceCountUnderParent + "].");
        this.occurrenceCountUnderParent = occurrenceCountUnderParent;
    }

    /**
     * Checks whether the current node is one level deeper in the tree than the argument node.
     *
     * @param node the node to inspect
     * @return true if the current node is one level deeper than the argument node, otherwise false
     * @see ComponentNode#isDeeperThan(ComponentNode)
     */
    public boolean isOneLevelDeeperThan(ComponentNode node) {
        return level - node.getLevel() == 1;
    }

    /**
     * Checks whether the current node is deeper in the tree than the argument node.
     * <p>
     * A node is on a deeper level than an other one when its level number has a greater value. A deeper level means
     * a child relationship to higher ones.
     *
     * @param node the node to inspect
     * @return true if the current node is deeper than the argument node, otherwise false
     */
    public boolean isDeeperThan(ComponentNode node) {
        return level > node.getLevel();
    }

    /**
     * Checks whether the current node is on a higher level in the tree than the argument node.
     * <p>
     * A node is on a higher level than another one when its level number has a lesser value. A higher level means
     * a parent relationship to deeper ones.
     *
     * @param node the node to inspect
     * @return true if the current node is on a higher level than the argument node, otherwise false
     */
    public boolean isHigherThan(ComponentNode node) {
        return level < node.getLevel();
    }

    /**
     * Checks whether the current node is at the same level in the tree than the argument node,
     * or in other terms whether they are sibling nodes.
     *
     * @param node the node to inspect
     * @return true if the current node is at the same level as the argument node, otherwise false
     */
    public boolean isAtSameLevelAs(ComponentNode node) {
        return level == node.getLevel();
    }

    /**
     * Checks whether the current node is at root level, meaning it has a level value of 1.
     *
     * @return true if the node is at root level, otherwise false
     */
    public boolean isAtRootLevel() {
        return level == 1;
    }

    /**
     * Checks whether this node and the argument one have the same {@link NodeType}.
     *
     * @param node the node to inspect
     * @return true if the nodes have the same type, otherwise false
     */
    public boolean hasSameTypeAs(ComponentNode node) {
        return type == node.getType();
    }

    /**
     * Checks whether this node has an inline config.
     *
     * @return true if the node has an inline config, false otherwise
     */
    public boolean hasInlineConfig() {
        return inlineConfig != ConfigurationNode.EMPTY;
    }
}
