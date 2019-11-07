package io.picimako.drupal.context;

import io.picimako.drupal.context.steps.DrupalPageSteps;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains configuration for mapping {@link ParagraphNodeType}s and {@link ModifierNodeType}s to step definition
 * methods, so that when adding a component based on a node type it calls the appropriate step definition methods.
 * <p>
 * In case a component needs to have custom logic to add it than the rest of the components, additional step
 * definition classes might need to be injected into this class to be able to work with them.
 */
public class ComponentAdder {

    private static final Map<ParagraphNodeType, ComponentInserter> COMPONENT_ADDERS = new HashMap<>();
    private static final Map<ModifierNodeType, ComponentInserter> MODIFIER_ADDERS = new HashMap<>();
    private final DrupalPageSteps drupalPageSteps;

    static {
        COMPONENT_ADDERS.put(ParagraphNodeType.CONTAINER, (parent, node, steps) -> steps.i_add_a_container());
        COMPONENT_ADDERS.put(ParagraphNodeType.LAYOUT, (parent, node, steps) -> steps.i_add_a_layout());
        COMPONENT_ADDERS.put(ParagraphNodeType.IMAGE, (parent, node, steps) -> steps.i_add_X_component(ParagraphNodeType.IMAGE));
        COMPONENT_ADDERS.put(ParagraphNodeType.YOUTUBE_VIDEO, (parent, node, steps) -> steps.i_add_X_component(ParagraphNodeType.YOUTUBE_VIDEO));

        MODIFIER_ADDERS.put(ModifierNodeType.ABSOLUTE_HEIGHT_MODIFIER,
            (parent, node, steps) -> steps.i_add_X_modifier(ModifierNodeType.ABSOLUTE_HEIGHT_MODIFIER));
        MODIFIER_ADDERS.put(ModifierNodeType.COLORS_MODIFIER, (parent, node, steps) -> steps.i_add_X_modifier(ModifierNodeType.COLORS_MODIFIER));
    }

    public ComponentAdder(DrupalPageSteps steps) {
        drupalPageSteps = steps;
    }

    /**
     * Adds the argument component to the page, invoking the underlying step definition methods in order to do that.
     *
     * @param node the component type to add
     */
    public void addComponentToPage(ComponentNode parentNode, ComponentNode node) {
        if (node.isModifierNode()) {
            MODIFIER_ADDERS.get(node.getType()).insert(parentNode, node, drupalPageSteps);
        } else {
            COMPONENT_ADDERS.get(node.getType()).insert(parentNode, node, drupalPageSteps);
        }
    }

    @FunctionalInterface
    private interface ComponentInserter {
        void insert(ComponentNode parentNode, ComponentNode node, DrupalPageSteps steps);
    }
}
