package io.picimako.drupal.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Contains configuration for mapping {@link ParagraphNodeType}s and {@link ModifierNodeType}s to step definition
 * methods, so that when adding a component based on a node type it calls the appropriate step definition methods.
 * <p>
 * In case a component needs to have custom logic to add it than the rest of the components, additional step
 * definition classes might need to be injected into this class to be able to work with them.
 */
public class ComponentAdder {

    private static final Map<ParagraphNodeType, Consumer<DrupalPageSteps>> COMPONENT_ADDERS = new HashMap<>();
    private static final Map<ModifierNodeType, Consumer<DrupalPageSteps>> MODIFIER_ADDERS = new HashMap<>();
    private final DrupalPageSteps drupalPageSteps;

    static {
        COMPONENT_ADDERS.put(ParagraphNodeType.CONTAINER, DrupalPageSteps::i_add_a_container);
        COMPONENT_ADDERS.put(ParagraphNodeType.LAYOUT, DrupalPageSteps::i_add_a_layout);
        COMPONENT_ADDERS.put(ParagraphNodeType.IMAGE, steps -> steps.i_add_X_component(ParagraphNodeType.IMAGE));
        COMPONENT_ADDERS.put(ParagraphNodeType.YOUTUBE_VIDEO, steps -> steps.i_add_X_component(ParagraphNodeType.YOUTUBE_VIDEO));

        MODIFIER_ADDERS.put(ModifierNodeType.ABSOLUTE_HEIGHT_MODIFIER,
            steps -> steps.i_add_X_modifier(ModifierNodeType.ABSOLUTE_HEIGHT_MODIFIER));
        MODIFIER_ADDERS.put(ModifierNodeType.COLORS_MODIFIER, steps -> steps.i_add_X_modifier(ModifierNodeType.COLORS_MODIFIER));
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
        if (node.isModifierNode()) {
            MODIFIER_ADDERS.get(node.getType()).accept(drupalPageSteps);
        } else {
            COMPONENT_ADDERS.get(node.getType()).accept(drupalPageSteps);
        }
    }
}
