package org.picimako.drupal.context;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object for storing information that is required during the content assembly,
 * and provides some convenience methods as well.
 */
@Getter
@Setter
public final class AssemblerContext {
    private String[] nodes;
    private Node node;
    private int index;
    @Builder.Default
    private ComponentNode previousComponentNode = ComponentNode.ABSENT;

    public AssemblerContext() {
    }

    public AssemblerContext(String[] nodes) {
        this.nodes = nodes;
    }

    public String getStringNode(int index) {
        return nodes[index];
    }

    public int nodeCount() {
        return nodes.length;
    }

    public boolean hasNextNode() {
        return index != nodes.length - 1;
    }
}
