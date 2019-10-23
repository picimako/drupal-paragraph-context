package org.picimako.drupal.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Invokes configuration methods that are predefined based on {@link NodeType}s.
 */
public class ComponentConfigurer {

    private static final Map<NodeType, BiConsumer<ConfigurationNode, ComponentConfigurer>> NODE_TYPE_TO_CONFIGURER;

    /**
     * Please note that in the current implementation this is not injected anywhere into the current class.
     * <p>
     * I think this is something that somewhat depends on specific project needs and frameworks that are used on those
     * projects, so it is up to the users how they implement the injection logic.
     */
    private LayoutSteps layoutSteps;

    static {
        Map<NodeType, BiConsumer<ConfigurationNode, ComponentConfigurer>> configurer = new HashMap<>();
        configurer.put(NodeType.CONTAINER, (node, conf) -> conf.configureLayout(node));
        configurer.put(NodeType.YOUTUBE_VIDEO, (node, conf) -> conf.configureYoutubeVideo(node));
        NODE_TYPE_TO_CONFIGURER = Map.copyOf(configurer);
    }

    /**
     * Retrieves the configuration logic for the argument node type and executes the configuration based on the
     * properties in the provided configuration node.
     *
     * @param type the node type that is being configured
     * @param node the configuration node that provides configuration properties
     */
    public void configure(NodeType type, ConfigurationNode node) {
        NODE_TYPE_TO_CONFIGURER.get(type).accept(node, this);
    }

    //Individual component configuration methods, potentially delegating to other step definition or support classes
    //to execute the actual configuration logic.

    private void configureLayout(ConfigurationNode node) {
        layoutSteps.i_set_the_background_image_path_to_X(node.get("backgroundimagepath"));
        //other potential configuration
    }

    private void configureYoutubeVideo(ConfigurationNode node) {
        //custom logic for invoking the configuration of a youtube video component
    }
}
