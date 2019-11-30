package io.picimako.drupal.context.table;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Stores the component and configuration definition for an input table entry.
 * <p>
 * In case of e.g. Cucumber this will need a proper Data table entry or row type converter so that the data table
 * can be passed properly into step definition method parameters.
 */
@Getter
@ToString
@EqualsAndHashCode
public final class ComponentAndConfiguration {
    private static final String ROOT_LEVEL_CONFIG_IDENTIFIER = "<";
    private final String component;
    private final String configuration;

    private ComponentAndConfiguration(String component, String configuration) {
        this.component = component;
        this.configuration = configuration;
    }

    private ComponentAndConfiguration(String component) {
        this(component, "");
    }

    /**
     * Returns whether the current entry is defined as a root level configuration meaning that the component part has
     * {@code <} as value. For example:
     * <pre>
     * | Component | Configuration           |
     * | <         | title:"Some page title" |
     * </pre>
     *
     * @return true if this definition is a root level configuration, false otherwise
     */
    public boolean hasRootLevelConfiguration() {
        return ROOT_LEVEL_CONFIG_IDENTIFIER.equals(component);
    }

    /**
     * Returns whether the current entry has a component definition, meaning it is not blank.
     *
     * @return true if it has component definition, false otherwise
     */
    public boolean hasComponentDefinition() {
        return !component.isBlank();
    }

    /**
     * Returns whether the current entry has a configuration definition, meaning it is not blank.
     *
     * @return true if it has configuration definition, false otherwise
     */
    public boolean hasConfiguration() {
        return !configuration.isBlank();
    }

    public static ComponentAndConfiguration create(String component, String configuration) {
        return new ComponentAndConfiguration(component, configuration);
    }

    public static ComponentAndConfiguration create(String component) {
        return new ComponentAndConfiguration(component);
    }
}
