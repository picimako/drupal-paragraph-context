package io.picimako.drupal.context;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents a node in the component tree that holds information for configuring a given component.
 * <p>
 * Configuration nodes are always one-liners and represent a list of String/String key-value mappings.
 * See pattern in: {@link NodeCreator}.
 */
@EqualsAndHashCode
@ToString
public class ConfigurationNode implements Node {

    private final Map<String, String> configurations;

    public ConfigurationNode(Map<String, String> configurations) {
        this.configurations = Map.copyOf(requireNonNull(configurations));
    }

    public String get(String key) {
        return configurations.get(key);
    }
}
