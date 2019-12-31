package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ConfigurationNodeConfigParser;

import java.util.List;

import static io.picimako.drupal.context.util.Preconditions.check;

/**
 * Validates the input data table for various violations.
 * <p>
 * This is used before the actual content creation, so that content creation won't even start if there is a problem
 * with the definitions in the table.
 *
 * @see TableBasedContentAssembler
 */
public class DataTableValidator {

    private static final String EXCEPTION_MESSAGE_FORMAT = "%s\nParent was: [%s]\nChild was: [%s]";
    private final TableBasedNodeCreator nodeCreator = new TableBasedNodeCreator();

    /**
     * Validates the argument data table for violations.
     * <p>
     * The followings are considered as violations:
     * <ul>
     * <li>
     * Modifiers must be defined at the same level as the previous paragraphs and modifiers,
     * thus if a modifier is defines on a lower level than the previous component, it will thrown an exception.
     * <p>
     * This, at the same time, restricts the modifier definition to happen right after a paragraph, or if there are
     * multiple modifiers, then to have all modifiers at the same level with the paragraph, e.g.:
     * <pre>
     * | Component              | Configuration |
     * | > CONTAINER            |               |
     * | >> LAYOUT              |               |
     * | >>> IMAGE              |               |
     * | >>>@ IMAGE_FX_MODIFIER |               |
     * | >>>@ COLORS_MODIFIER   |               |
     * </pre>
     * </li>
     * <li>If the current component is more than one level deeper than the previous one, it throws an exception because
     * we don't allow specifying components more than one level deeper under a given component.
     * This is allowed:
     * <pre>
     * | Component   | Configuration |
     * | > CONTAINER |               |
     * | >> LAYOUT   |               |
     * | >>> IMAGE   |               |
     * </pre>
     * while this is not valid:
     * <pre>
     * | Component   | Configuration |
     * | > CONTAINER |               |
     * | >> LAYOUT   |               |
     * | >>>>> IMAGE |               |
     * </pre></li>
     * <li>If for some reason there is no component definition in the table, like the following, it throws an exception:
     * <pre>
     * | Component | Configuration      |
     * |           | title:"some title" |
     * |           | path:/some/path    |
     * </pre>
     * </li>
     * </ul>
     * ConfigurationNode validation is handled in {@link TableBasedNodeCreator} and {@link ConfigurationNodeConfigParser}.
     * Besides this during validation here only those entries are validated that are not root level configurations and have
     * an actual component definition.
     *
     * @param definitions the list of component and configuration definitions
     */
    public void validateTree(List<ComponentAndConfiguration> definitions) {
        ComponentNode previousComponentNode = ComponentNode.ABSENT;
        check(definitions.stream().anyMatch(ComponentAndConfiguration::hasComponentDefinition),
            "None of the entries in the input data table has a component defined.");
        for (ComponentAndConfiguration definition : definitions) {
            if (!definition.hasRootLevelConfiguration() && definition.hasComponentDefinition()) {
                ComponentNode currentNode = nodeCreator.createComponentNode(definition.getComponent());
                validateCurrentNode(currentNode, previousComponentNode);
                previousComponentNode = currentNode;
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
