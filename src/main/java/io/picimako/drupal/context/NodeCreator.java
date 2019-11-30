package io.picimako.drupal.context;

public interface NodeCreator {

    Node createNode(String line);

    default ComponentNode createComponentNode(String line) {
        return (ComponentNode) createNode(line);
    }

    default ConfigurationNode createConfigurationNode(String line) {
        return (ConfigurationNode) createNode(line);
    }
}
