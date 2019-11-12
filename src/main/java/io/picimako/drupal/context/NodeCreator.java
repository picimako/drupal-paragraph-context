package io.picimako.drupal.context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static io.picimako.drupal.context.ConfigurationNodeConfigParser.CONFIG_ITEM_DELIMITER;
import static io.picimako.drupal.context.ConfigurationNodeConfigParser.CONFIG_KEY_VALUE_DELIMITER;
import static java.lang.String.format;

/**
 * Parses lines (representing components or configurations) from a String component tree and converts them to different
 * {@link Node} types.
 * <p>
 * A {@link ComponentNode} representing a Paragraph should begin with at least 1 hyphen, followed by a whitespace,
 * then followed by an all upper snake case string, e.g.: {@code "- CONTAINER"} or {@code "-- LAYOUT"},
 * meaning the following is not valid: {@code "CONTAINER"}.
 * <p>
 * A {@link ComponentNode} representing a Modifier should begin with at least 1 hyphen, followed by an {@code @}
 * symbol and a whitespace, then followed by an all upper snake case string, e.g.: {@code "--@ COLORS_MODIFIER"} or
 * {@code "---@ ABSOLUTE_HEIGHT_MODIFIER"}, meaning the following is not valid: {@code "COLORS_MODIFIER"}.
 * <p>
 * A {@link ConfigurationNode} should begin with at least 1 hyphen (should be aligned with the component it is referring to
 * but there is no validation for that), or no hyphen for configuration that is not meant to be component specific but
 * specific to the content itself being created, followed by a whitespace, and followed by a list of key-value pairs having
 * the following pattern: {@code keyA:valueA, keyB:valueB}. (There is also no validation here for the format of the
 * key-value pairs.)
 */
public class NodeCreator {

    private static final Pattern PARAGRAPH_NODE_PATTERN = Pattern.compile("(?<level>-+) (?<type>[A-Z_]+)");
    private static final Pattern MODIFIER_NODE_PATTERN = Pattern.compile("(?<level>-+)@ (?<type>[A-Z_]+)");
    private static final Pattern CONFIGURATION_NODE_PATTERN = Pattern.compile("-*\\* (?<config>.*)");
    private static final String CONFIGURATION_NODE_ENDING_PATTERN = format(".*%s *$", CONFIG_ITEM_DELIMITER);

    private static final String INCORRECT_NODE_DEFINITION_ENDING_MESSAGE = format("The configuration node ends with a '%s',"
        + " which is not considered a valid configuration node value.", CONFIG_KEY_VALUE_DELIMITER);
    private static final String INCORRECT_KEY_VALUE_PAIR_DEFINITION_MESSAGE = format("The configuration node doesn't contain a valid"
        + " key-value pair. They should be in the following format: <key>%s<value>", CONFIG_KEY_VALUE_DELIMITER);

    private static final String LEVEL = "level";
    private static final String TYPE = "type";
    private static final String CONFIG = "config";

    private final ConfigurationNodeConfigParser parser = new ConfigurationNodeConfigParser();

    /**
     * Converts the argument String values to different {@link Node} objects based on their defined patterns.
     * <p>
     * Configuration values (at the right hand side of the colon symbol) may be enclosed by {@code "} characters,
     * so that extra characters like whitespaces at the beginning and at the end of the value are more prominent.
     *
     * @param line the line (component or configuration) to convert
     * @return the created node
     * @throws IllegalArgumentException when the line doesn't contain a key-value separator,
     *                                  or the key-value pair ends with a comma followed by 0 or more whitespaces,
     *                                  or the line has neither the pattern of a component nor a configuration node
     */
    public Node createNode(String line) {
        Node node;
        Matcher paragraphNodeMatcher = PARAGRAPH_NODE_PATTERN.matcher(line);
        if (paragraphNodeMatcher.matches()) {
            node = createParagraphNode(paragraphNodeMatcher);
        } else {
            Matcher modifierNodeMatcher = MODIFIER_NODE_PATTERN.matcher(line);
            if (modifierNodeMatcher.matches()) {
                node = createModifierNode(modifierNodeMatcher);
            } else {
                node = createConfigurationNode(line);
            }
        }
        return node;
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

    private Node createConfigurationNode(String line) {
        Matcher configurationNodeMatcher = CONFIGURATION_NODE_PATTERN.matcher(line);
        if (configurationNodeMatcher.matches()) {
            validatePresenceOfKeyValueDelimiter(line);
            validateNodeDefinitionEnding(line);
            return new ConfigurationNode(parser.parseConfigurationValues(configurationNodeMatcher.group(CONFIG)));
        } else {
            throw new IllegalArgumentException("The provided line from the component tree is not valid: [" + line + "]");
        }
    }

    //------------ Validate configuration node ------------

    private void validatePresenceOfKeyValueDelimiter(String line) {
        checkArgument(line.contains(CONFIG_KEY_VALUE_DELIMITER), INCORRECT_KEY_VALUE_PAIR_DEFINITION_MESSAGE);
    }

    private void validateNodeDefinitionEnding(String line) {
        checkArgument(violatesLineEndingPattern(line), INCORRECT_NODE_DEFINITION_ENDING_MESSAGE);
    }

    private boolean violatesLineEndingPattern(String line) {
        return !line.matches(CONFIGURATION_NODE_ENDING_PATTERN);
    }

    /**
     * Returns whether the String representation of a node is a configuration node.
     *
     * @param node the node to inspect
     * @return true if it is a configuration node, otherwise false
     */
    public static boolean isConfigurationNode(String node) {
        return CONFIGURATION_NODE_PATTERN.matcher(node).matches();
    }
}
