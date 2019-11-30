package io.picimako.drupal.context.treeview;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.Node;
import lombok.Getter;
import lombok.Setter;

/**
 * Context object for storing information that is required during the content assembly,
 * and provides some convenience methods as well.
 */
@Getter
@Setter
public final class TreeViewAssemblerContext {
    private String[] nodes;
    private Node node;
    private int index;
    private ComponentNode previousComponentNode = ComponentNode.ABSENT;

    public TreeViewAssemblerContext() {
    }

    public TreeViewAssemblerContext(String[] nodes) {
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
