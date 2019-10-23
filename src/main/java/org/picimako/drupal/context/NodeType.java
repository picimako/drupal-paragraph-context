package org.picimako.drupal.context;

/**
 * This enum is meant to mirror the entries in {@link ComponentContextSelector}, so that context selectors can be
 * converted properly to node types, and that they have a clear distinction in their usage.
 * <p>
 * Based on a node type additional validation can be put in place so that certain components cannot be put onto
 * certain level(s) or under certain component(s).
 */
public enum NodeType {
    CONTAINER,
    LAYOUT,
    IMAGE,
    CAROUSEL,
    CAROUSEL_ITEM,
    YOUTUBE_VIDEO;

    /**
     * Converts this node type to a {@link ComponentContextSelector} by its name.
     *
     * @return the component context selector
     */
    public ComponentContextSelector toContextSelector() {
        return ComponentContextSelector.valueOf(this.name());
    }
}
