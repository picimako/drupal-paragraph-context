package io.picimako.drupal.context;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.joining;

/**
 * Takes a collection of nodes (retrieved by traversing the current branch of the component tree), and creates a CSS
 * selector from them.
 */
public class CssContextSelectorAssembler {

    private static final String CSS_ANY_CHILD_SEPARATOR = " ";

    /**
     * Iterates through the argument nodes and converts each of them to a CSS selector based on the type and
     * the occurrence count of the node, then concatenates all into one CSS selector.
     * <p>
     * If the CSS selector of a {@link ComponentContextSelector} entry actually uses an index, it will be properly
     * applied from the occurrence count.
     *
     * @param nodes the tree nodes (from a branch of the tree) to traverse
     */
    public String createCssContextSelectorFrom(List<ComponentNode> nodes) {
        checkArgument(!nodes.isEmpty(), "There is no node to create CSS selector from."
                + " The provided collection of nodes is empty.");
        return nodes.stream().map(this::toCssSelector).collect(joining(CSS_ANY_CHILD_SEPARATOR));
    }

    private String toCssSelector(ComponentNode node) {
        return node.getType()
                .toContextSelector()
                .getCssSelector()
                .apply(node.getOccurrenceCountUnderParent());
    }
}
