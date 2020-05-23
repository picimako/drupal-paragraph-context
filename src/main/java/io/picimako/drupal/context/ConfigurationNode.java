package io.picimako.drupal.context;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Represents a node in the component tree (or an inline configuration in case of tree view based layout)
 * that holds information for configuring a given component.
 * <p>
 * Configuration nodes are always one-liners and represent a list of String/String key-value mappings.
 * See pattern in the {@link NodeCreator} implementations.
 */
@EqualsAndHashCode
@ToString
public class ConfigurationNode implements Node {

    public static final ConfigurationNode EMPTY = new ConfigurationNode(Collections.emptyMap());
    private final Map<String, String> configurations;

    public ConfigurationNode(Map<String, String> configurations) {
        this.configurations = Map.copyOf(requireNonNull(configurations));
    }

    public String get(String key) {
        return configurations.get(key);
    }

    public boolean hasProperty(String property) {
        return configurations.containsKey(property);
    }
}
