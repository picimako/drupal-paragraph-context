package org.picimako.drupal.context;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Parses lines (representing components or configurations) from a String component tree and converts them to different
 * {@link Node} types.
 * <p>
 * A {@link ComponentNode} should begin with at least 1 hyphen, followed by a whitespace, then followed by an all upper
 * snake case string, e.g.: {@code "- CONTAINER"} or {@code "-- LAYOUT"}, meaning the following is not valid:
 * {@code "CONTAINER"}.
 * <p>
 * A {@link ConfigurationNode} should begin with at least 1 hyphen (should be aligned with the component it is referring to
 * but there is no validation for that), followed by a whitespace, and followed by a list of key-value pairs having
 * the following pattern: {@code keyA:valueA, keyB:valueB}. (There is also no validation here for the format of the
 * key-value pairs.)
 */
public class NodeCreator {

    private static final Pattern COMPONENT_NODE_PATTERN = Pattern.compile("(?<level>-+) (?<type>[A-Z_]+)");
    private static final Pattern CONFIGURATION_NODE_PATTERN = Pattern.compile("-+\\* (?<config>.*)");
    private static final String LEVEL = "level";
    private static final String TYPE = "type";
    private static final String CONFIG = "config";

    private final ConfigurationNodeConfigParser parser = new ConfigurationNodeConfigParser();

    /**
     * Converts the argument String values to different {@link Node} objects based on their defined pattern.
     * <p>
     * TODO: add option to enclose configuration value in " and " so that ending whitespaces are displayed properly
     *
     * @param line the line (component or configuration) to convert
     * @return the created node
     * @throws IllegalArgumentException when the line doesn't contain a key-value separator,
     *                                  or the key-value pair ends with a comma followed by 0 or more whitespaces,
     *                                  or the line has neither the pattern of a component nor a configuration node
     */
    public Node createNode(String line) {
        Node node;
        Matcher componentNodeMatcher = COMPONENT_NODE_PATTERN.matcher(line);
        if (componentNodeMatcher.matches()) {
            node = new ComponentNode(componentNodeMatcher.group(LEVEL).length(),
                    NodeType.valueOf(componentNodeMatcher.group(TYPE)));
        } else {
            Matcher configurationNodeMatcher = CONFIGURATION_NODE_PATTERN.matcher(line);
            if (configurationNodeMatcher.matches()) {
                checkArgument(line.contains(":"), "The configuration node doesn't contain a valid"
                        + " key-value pair. They should be in the following format: <key>:<value>");
                checkArgument(!line.matches(".*, *$"), "The configuration node ends with a comma,"
                        + " which is not considered a valid configuration node value.");
                node = new ConfigurationNode(parser.parseConfigurationValues(configurationNodeMatcher.group(CONFIG)));
            } else {
                throw new IllegalArgumentException("The provided line from the component tree is not valid: [" + line + "]");
            }
        }
        return node;
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
