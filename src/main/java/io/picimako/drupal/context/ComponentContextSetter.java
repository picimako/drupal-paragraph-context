package io.picimako.drupal.context;

/**
 * It sets the component context for further configuration actions. It invokes a dedicated method from a pre-configured
 * step definitions class that sets an underlying field, property, variable, etc. storing the context.
 */
public class ComponentContextSetter {
    private final ComponentTreeBranchToCssContextSelectorConverter converter =
            new ComponentTreeBranchToCssContextSelectorConverter();
    private final DrupalPageSteps steps;

    public ComponentContextSetter(DrupalPageSteps steps) {
        this.steps = steps;
    }

    /**
     * To be able to set the component context you need to have it stored somewhere for the currently executed test.
     * <p>
     * It might be more complex to handle this in case of parallel test execution, depending on what frameworks you use
     * and how.
     *
     * @param tree        the tree to build the context from
     * @param currentNode the current component node
     */
    public void setContext(ComponentTree tree, ComponentNode currentNode) {
        steps.i_work_with_X(converter.convert(tree, currentNode));
    }
}
