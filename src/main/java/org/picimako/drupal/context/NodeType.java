package org.picimako.drupal.context;

/**
 * Represents the type of a {@link ComponentNode}.
 */
public interface NodeType {

    /**
     * Converts this node type to a {@link ComponentContextSelector} based on its name.
     *
     * @return the component context selector
     */
    ComponentContextSelector toContextSelector();
}
