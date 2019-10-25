package org.picimako.drupal.context;

/**
 * This enum is meant to mirror the modifier  in {@link ComponentContextSelector}, so that context selectors can be
 * converted properly to node types, and that they have a clear distinction in their usage.
 * <p>
 * Based on a node type additional validation can be put in place so that certain components cannot be put onto
 * certain level(s) or under certain component(s).
 */
public enum ModifierNodeType implements NodeType {
    ABSOLUTE_HEIGHT_MODIFIER,
    COLORS_MODIFIER;

    @Override
    public ComponentContextSelector toContextSelector() {
        return ComponentContextSelector.valueOf(this.name());
    }
}
