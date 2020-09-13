package io.picimako.drupal.context.converter;

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts a component tree to its table view representation.
 */
public final class TreeViewToDataTableConverter {

    private static final Pattern PARAGRAPH_NODE_PATTERN = Pattern.compile("(?<level>-+) (?<type>[A-Z_]+)(?<inlineconfig> >> (?<config>.*))?");
    private static final Pattern MODIFIER_NODE_PATTERN = Pattern.compile("(?<level>-+)@ (?<type>[A-Z_]+)");
    private static final Pattern CONFIGURATION_NODE_PATTERN = Pattern.compile("(?<level>-*)\\* (?<config>.*)");

    /**
     * Converts a tree view based component collection to a table view based representation.
     * <p>
     * Takes into account all possible combination of node definitions:
     * <ul>
     *     <li>Root-level configuration</li>
     *     <li>multiline root-level configuration</li>
     *     <li>Paragraph</li>
     *     <li>Modifier</li>
     *     <li>Paragraph inline configuration</li>
     *     <li>Single line configuration</li>
     *     <li>Multiline configuration</li>
     *     <li>Multiple configurations in a single line</li>
     * </ul>
     *
     * @param nodes the list of nodes from the tree view representation to convert
     * @return the converted table view of the component tree
     */
    public String convert(List<String> nodes) {
        final List<RowItem> table = new ArrayList<>();
        int currentTableRow = -1;
        for (int i = 0; i < nodes.size(); i++) {
            Matcher paragraphMatcher = PARAGRAPH_NODE_PATTERN.matcher(nodes.get(i));
            if (paragraphMatcher.matches()) {
                table.add(new RowItem());
                currentTableRow++;
                table.get(currentTableRow).component = convertLevelOf(paragraphMatcher) + " " + paragraphMatcher.group("type");
                if (paragraphMatcher.group("inlineconfig") != null) {
                    table.get(currentTableRow).configuration = paragraphMatcher.group("config");
                }
            } else {
                Matcher configMatcher = CONFIGURATION_NODE_PATTERN.matcher(nodes.get(i));
                if (configMatcher.matches()) {
                    if (configMatcher.group("level").isBlank()) { //root level configuration
                        table.add(new RowItem());
                        currentTableRow++;
                        if (currentTableRow == 0) {
                            table.get(currentTableRow).component = "<";
                        }
                        table.get(currentTableRow).configuration = configMatcher.group("config");
                    } else {
                        if (!table.get(currentTableRow).configuration.isEmpty()) {
                            //previous node: paragraph without inline config -> put config into the same row as the paragraph
                            //previous node: paragraph with inline config -> create new table row
                            //previous node: configuration node -> create new table row
                            table.add(new RowItem());
                            currentTableRow++;
                        }
                        table.get(currentTableRow).configuration = configMatcher.group("config");
                    }
                } else { //Check for modifier
                    Matcher modifierNodeMatcher = MODIFIER_NODE_PATTERN.matcher(nodes.get(i));
                    if (modifierNodeMatcher.matches()) {
                        table.add(new RowItem());
                        currentTableRow++;
                        table.get(currentTableRow).component = convertLevelOf(modifierNodeMatcher)
                            + "@ "
                            + modifierNodeMatcher.group("type");
                    }
                }
            }
        }

        return table.stream()
            .map(row -> "| " + row.component + " | " + row.configuration + " |")
            .collect(joining("\n"));
    }

    private String convertLevelOf(Matcher matcher) {
        return matcher.group("level").replaceAll("-", ">");
    }

    /**
     * A row item for the table view to store the component and configuration information.
     */
    public static final class RowItem {
        String component = "";
        String configuration = "";
    }
}
