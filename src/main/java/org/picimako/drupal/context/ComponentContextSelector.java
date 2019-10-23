package org.picimako.drupal.context;

import java.util.function.Function;

/**
 * Provides component CSS selectors for building context for certain actions on the Drupal editor.
 * <p>
 * An individual component's context selector is a(n) (as unique as possible) CSS selector of that component
 * parameterized with the occurrence count under its parent node, that is currently being worked with.
 * <p>
 * The default value of the index when is 1. See {@link ComponentNode}.
 * <p>
 * Each context selector is parameterized with an index with which one can target a given occurrence of a component
 * under a parent component, which index it will get when the context selector is being built.
 * <p>
 * Let's say we have the following content tree:
 * <pre>
 * - CONTAINER
 * -- LAYOUT
 * --- IMAGE
 * --- IMAGE
 * </pre>
 * The last IMAGE is the second occurrence of the IMAGE component under its parent.
 * <p>
 * And if we have the following tree:
 * <pre>
 * - CONTAINER
 * -- LAYOUT
 * --- IMAGE
 * --- YOUTUBE_VIDEO
 * --- IMAGE
 * </pre>
 * the last IMAGE component is still the second occurrence of IMAGE under its parent, despite that fact that it is the
 * third component under it.
 * <p>
 * NOTE: the CSS selectors in this class are dummy ones, just to give you a sense what would it look like, and also
 * to be able to properly unit test the thing.
 *
 * @see ComponentTreeBranchToCssContextSelectorConverter
 */
public enum ComponentContextSelector {
    CONTAINER(i -> ".container:nth-child(" + i + ")"),
    /**
     * Note that the index is not used in this case. It may happen that a Container can contain only one Layout.
     */
    LAYOUT(i -> ".layout"),
    IMAGE(i -> ".image-component:nth-child(" + i + ")"),
    CAROUSEL(i -> ".carousel:nth-child(" + i + ")"),
    CAROUSEL_ITEM(i -> ".carousel-item:nth-child(" + i + ")"),
    YOUTUBE_VIDEO(i -> ".youtube-video:nth-child(" + i + ")");

    private final Function<Long, String> selector;

    ComponentContextSelector(Function<Long, String> selector) {
        this.selector = selector;
    }

    public Function<Long, String> getCssSelector() {
        return selector;
    }
}
