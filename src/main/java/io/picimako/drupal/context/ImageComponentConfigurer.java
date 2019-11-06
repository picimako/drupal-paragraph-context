package io.picimako.drupal.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains configuration logic for an Image component.
 */
public final class ImageComponentConfigurer {

    public static final Map<String, ComponentConfigurer.Configurer> IMAGE_COMPONENT_CONFIGURER;

    static {
        IMAGE_COMPONENT_CONFIGURER = new HashMap<>();
        IMAGE_COMPONENT_CONFIGURER.put("name", (node, steps) -> steps.i_add_the_image_named_X(node.get("name")));
        IMAGE_COMPONENT_CONFIGURER.put("link", (node, steps) -> steps.i_add_X_to_the_link_field(node.get("link")));
    }

    private ImageComponentConfigurer() {
    }

    /**
     * Goes through the property values that might be defined for this component, and if one is available then
     * delegates to another method that actually configures the component with that property.
     *
     * @param node  the configuration node
     * @param steps the step definitions class to delegate to
     */
    public static void configureImage(ConfigurationNode node, DrupalConfigurationSteps steps) {
        IMAGE_COMPONENT_CONFIGURER.keySet().forEach(property -> {
            if (node.hasProperty(property)) {
                IMAGE_COMPONENT_CONFIGURER.get(property).configure(node, steps);
            }
        });
    }
}
