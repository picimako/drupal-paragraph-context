package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.ConfigurationNodeConfigParser;
import io.picimako.drupal.context.ModifierNodeType;
import io.picimako.drupal.context.Node;
import io.picimako.drupal.context.NodeCreator;
import io.picimako.drupal.context.NodeType;
import io.picimako.drupal.context.ParagraphNodeType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static io.picimako.drupal.context.ConfigurationNodeConfigParser.CONFIG_ITEM_DELIMITER;
import static io.picimako.drupal.context.ConfigurationNodeConfigParser.CONFIG_KEY_VALUE_DELIMITER;
import static java.lang.String.format;

/**
 * Parses cell values from the input table (components or configurations) and converts them to different {@link Node} types.
 * <p>
 * A {@link ComponentNode} representing a Paragraph should consist of either
 * <ul>
 *     <li>a single less-than sign (<, represents a root-level configuration) or</li>
 *     <li>should begin with at least 1 greater-than sign (>), followed by a whitespace,
 *     then an all upper snake case string, e.g.: {@code "> CONTAINER"} or {@code ">> LAYOUT"},
 *     meaning the following is not valid: {@code "CONTAINER"}.</li>
 * </ul>
 * A {@link ComponentNode} representing a Modifier should
 * <ul>
 *     <li>begin with at least 1 greater-than sign (>) and</li>
 *     <li>followed by an {@code @} symbol and a whitespace, then an all upper snake case string,
 *     e.g.: {@code ">>@ COLORS_MODIFIER"} or {@code ">>>@ ABSOLUTE_HEIGHT_MODIFIER"},
 *     meaning the following is not valid: {@code "COLORS_MODIFIER"}.</li>
 * </ul>
 * <p>
 * A {@link ConfigurationNode} should be in a format that may be parsed into a Map of String-String entries
 * basically a list of key-value pairs having the following pattern: {@code keyA:valueA, keyB:valueB}.
 * <p>
 * (There is no validation here for the format of the key-value pairs.)
 *
 * @see ConfigurationNodeConfigParser#CONFIG_ITEM_DELIMITER
 * @see ConfigurationNodeConfigParser#CONFIG_KEY_VALUE_DELIMITER
 */
public class TableBasedNodeCreator implements NodeCreator {

    private static final Pattern PARAGRAPH_NODE_PATTERN = Pattern.compile("(?<level>>+) (?<type>[A-Z_]+)");
    private static final Pattern MODIFIER_NODE_PATTERN = Pattern.compile("(?<level>>+)@ (?<type>[A-Z_]+)");
    private static final Pattern CONFIGURATION_NODE_PATTERN = Pattern.compile("(?<config>.*)");
    private static final String CONFIGURATION_NODE_ENDING_PATTERN = format(".*%s *$", CONFIG_ITEM_DELIMITER);

    private static final String INCORRECT_NODE_DEFINITION_ENDING_MESSAGE = format("The configuration node ends with a '%s',"
        + " which is not considered a valid configuration node value.", CONFIG_KEY_VALUE_DELIMITER);
    private static final String INCORRECT_KEY_VALUE_PAIR_DEFINITION_MESSAGE = format("The configuration node doesn't contain a valid"
        + " key-value pair. They should be in the following format: <key>%s<value>", CONFIG_KEY_VALUE_DELIMITER);

    private static final String LEVEL = "level";
    private static final String TYPE = "type";
    private static final String CONFIG = "config";

    private final ConfigurationNodeConfigParser parser = new ConfigurationNodeConfigParser();

    @Override
    public Node createNode(String line) {
        throw new UnsupportedOperationException("Please use the methods dedicated specifically to creating component"
            + " and configuration nodes.");
    }

    /**
     * Converts the argument String component value to a {@link ComponentNode} object.
     * <p>
     * It doesn't (need to) handle root-level configuration because in that case no component node is created but
     * {@link ComponentNode#ABSENT} is used instead by default.
     * <p>
     *
     * @param component the string value of a component definition to convert
     * @return the component node
     * @throws IllegalArgumentException when the node is not in a correct format
     */
    @Override
    public ComponentNode createComponentNode(String component) {
        ComponentNode node;
        Matcher paragraphNodeMatcher = PARAGRAPH_NODE_PATTERN.matcher(component);
        if (paragraphNodeMatcher.matches()) {
            node = createParagraphNode(paragraphNodeMatcher);
        } else {
            Matcher modifierNodeMatcher = MODIFIER_NODE_PATTERN.matcher(component);
            if (modifierNodeMatcher.matches()) {
                node = createModifierNode(modifierNodeMatcher);
            } else {
                throw new IllegalArgumentException("The component node is not in a supported format.");
            }
        }
        return node;
    }

    /**
     * Converts the argument String configuration value to a {@link ConfigurationNode} object.
     * <p>
     * Configuration values (at the right hand side of the colon symbol) may be enclosed by {@code "} characters.
     *
     * @param configuration the string value of a configuration definition to convert
     * @return the configuration node
     * @throws IllegalArgumentException when the line doesn't contain a key-value separator, or
     *                                  when the key-value pairs end with a separator followed by 0 or more whitespaces or,
     *                                  when the line has the pattern neither of a component nor a configuration.
     *                                  Or, finally, when for some reason the configuration value doesn't match the pattern that
     *                                  is supposed to match anything.
     */
    @Override
    public ConfigurationNode createConfigurationNode(String configuration) {
        Matcher configurationNodeMatcher = CONFIGURATION_NODE_PATTERN.matcher(configuration);
        if (configurationNodeMatcher.matches()) {
            validatePresenceOfKeyValueDelimiter(configuration);
            validateNodeDefinitionEnding(configuration);
            return new ConfigurationNode(parser.parseConfigurationValues(configurationNodeMatcher.group(CONFIG)));
        } else {
            throw new IllegalArgumentException("Congrats! You did some big magic here that the argument cell value doesn't match the .* regexp.");
        }
    }

    private ComponentNode createParagraphNode(Matcher nodeMatcher) {
        NodeType nodeType = ParagraphNodeType.valueOf(nodeMatcher.group(TYPE));
        return new ComponentNode(nodeMatcher.group(LEVEL).length(), nodeType);
    }

    private ComponentNode createModifierNode(Matcher nodeMatcher) {
        NodeType nodeType = ModifierNodeType.valueOf(nodeMatcher.group(TYPE));
        ComponentNode node = new ComponentNode(nodeMatcher.group(LEVEL).length(), nodeType);
        node.setModifierNode(true);
        return node;
    }

    //------------ Validate configuration node ------------

    private void validatePresenceOfKeyValueDelimiter(String cell) {
        checkArgument(cell.contains(CONFIG_KEY_VALUE_DELIMITER), INCORRECT_KEY_VALUE_PAIR_DEFINITION_MESSAGE);
    }

    private void validateNodeDefinitionEnding(String cell) {
        checkArgument(violatesValueEndingPattern(cell), INCORRECT_NODE_DEFINITION_ENDING_MESSAGE);
    }

    private boolean violatesValueEndingPattern(String cell) {
        return !cell.matches(CONFIGURATION_NODE_ENDING_PATTERN);
    }
}
