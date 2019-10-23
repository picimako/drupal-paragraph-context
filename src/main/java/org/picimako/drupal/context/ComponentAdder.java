package org.picimako.drupal.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Contains configuration for mapping {@link NodeType}s to step definition methods, so that when adding a component
 * based on a node type it calls the appropriate step definition methods.
 * <p>
 * In case some components need to have custom logic to add them than the rest of the components, additional step
 * definition classes might need to be injected into this class to be able to work with them.
 */
public class ComponentAdder {

    private static final Map<NodeType, Consumer<DrupalPageSteps>> COMPONENT_ADDERS = new HashMap<>();
    private final DrupalPageSteps drupalPageSteps;

    static {
        COMPONENT_ADDERS.put(NodeType.CONTAINER, DrupalPageSteps::i_add_a_container);
        COMPONENT_ADDERS.put(NodeType.LAYOUT, DrupalPageSteps::i_add_a_layout);
        COMPONENT_ADDERS.put(NodeType.IMAGE, steps -> steps.i_add_X_component(NodeType.IMAGE));
        COMPONENT_ADDERS.put(NodeType.YOUTUBE_VIDEO, steps -> steps.i_add_X_component(NodeType.YOUTUBE_VIDEO));
    }

    public ComponentAdder(DrupalPageSteps steps) {
        drupalPageSteps = steps;
    }

    /**
     * Adds the argument component to the page, invoking the underlying step definition methods in order to do that.
     *
     * @param node the component type to add
     */
    public void addComponentToPage(ComponentNode node) {
        COMPONENT_ADDERS.get(node.getType()).accept(drupalPageSteps);
    }
}
