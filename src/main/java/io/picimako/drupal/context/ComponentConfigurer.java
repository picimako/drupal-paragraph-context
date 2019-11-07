package io.picimako.drupal.context;

import io.picimako.drupal.context.steps.DrupalConfigurationSteps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Invokes configuration methods that are predefined based on {@link NodeType}s.
 */
public class ComponentConfigurer {

    private static final Map<NodeType, BiConsumer<ConfigurationNode, DrupalConfigurationSteps>> NODE_TYPE_TO_CONFIGURER;

    /**
     * Please note that in the current implementation this is not injected anywhere into the current class.
     * <p>
     * I think this is something that somewhat depends on specific project needs and frameworks that are used on those
     * projects, so it is up to the users how they implement the injection logic.
     */
    private DrupalConfigurationSteps configSteps;

    static {
        Map<NodeType, BiConsumer<ConfigurationNode, DrupalConfigurationSteps>> configurer = new HashMap<>();
        configurer.put(ParagraphNodeType.IMAGE, ImageComponentConfigurer::configureImage);
        NODE_TYPE_TO_CONFIGURER = Map.copyOf(configurer);
    }

    public ComponentConfigurer(DrupalConfigurationSteps configSteps) {
        this.configSteps = configSteps;
    }

    /**
     * Retrieves the configuration logic for the argument node type and executes the configuration based on the
     * properties in the provided configuration node.
     *
     * @param type the node type that is being configured
     * @param node the configuration node that provides configuration properties
     */
    public void configure(NodeType type, ConfigurationNode node) {
        NODE_TYPE_TO_CONFIGURER.get(type).accept(node, configSteps);
    }

    //Individual component configuration methods, potentially delegating to other step definition or support classes
    //to execute the actual configuration logic.

    @FunctionalInterface
    public interface Configurer {
        void configure(ConfigurationNode node, DrupalConfigurationSteps steps);
    }
}
