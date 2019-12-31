package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentAdder;
import io.picimako.drupal.context.ComponentConfigurer;
import io.picimako.drupal.context.ComponentContextSetter;
import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ComponentTree;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.NodeCreator;
import io.picimako.drupal.context.steps.DrupalConfigurationSteps;
import io.picimako.drupal.context.steps.DrupalPageSteps;

import java.util.List;

import static io.picimako.drupal.context.util.Preconditions.check;
import static java.util.Objects.requireNonNull;

/**
 * This implementation of a content assembler is designed to work with tests where the input is in a specific table
 * format like a Cucumber data table and each row is converted into a specific type
 * (in this case {@link ComponentAndConfiguration}).
 * <p>
 * The table is expected to be in the following format:
 * <pre>
 * | Component             | Configuration                         |
 * | <                     | title:"Some page title"               |  <- Root level configuration
 * |                       | meta-keywords:keywords                |
 * | > CONTAINER           | bg:#fff                               |  <- Component with configuration
 * | >> LAYOUT             |                                       |  <- Component without configuration
 * | >>> IMAGE             | name:some-image.png                   |
 * |                       | link:/some/path                       |  <- Configuration for the last defined component, in this case IMAGE
 * | >>> YOUTUBE_VIDEO     | title:"Good title", features:autoplay |  <- Component with multiple configurations at once
 * | >>>@ PADDING_MODIFIER | left:50px                             |  <- Modifier with configuration
 * </pre>
 * It is up to the users whether they define the header row (Component/Configuration), it depends on how they want the
 * input file to look like and according to that how they parse the table.
 *
 * @see io.picimako.drupal.context.treeview.ComponentTreeBasedContentAssembler
 */
public class TableBasedContentAssembler {

    private final ComponentTree tree = new ComponentTree();
    private final NodeCreator nodeCreator = new TableBasedNodeCreator();
    private final DataTableValidator dataTableValidator = new DataTableValidator();
    private final ComponentConfigurer componentConfigurer;
    private final ComponentAdder componentAdder;
    private final ComponentContextSetter contextSetter;

    /**
     * Creates a new {@link TableBasedContentAssembler} instance.
     * <p>
     * The parameter {@code steps} in this case is necessary because that is the dedicated step definitions class that
     * is responsible for providing methods for adding components and setting context selectors.
     * <p>
     * Of course in a different kind of framework/solution this step definitions class may be different or not be a
     * step definitions class at all.
     *
     * @param steps a step definitions class for handling component addition and context setting
     */
    public TableBasedContentAssembler(DrupalPageSteps steps, DrupalConfigurationSteps configSteps) {
        requireNonNull(steps);
        this.componentAdder = new ComponentAdder(steps);
        this.contextSetter = new ComponentContextSetter(steps);
        this.componentConfigurer = new ComponentConfigurer(configSteps);
    }

    /**
     * Assembles a Drupal or other CMS content.
     * The input is the list of component and configuration entries, and is expected to not include the header row.
     * <p>
     * NOTE: Map as input type is not sufficient as multiple table rows might have empty component strings in case of
     * multi-row configuration definitions.
     * <p>
     * The steps it takes to parse definitions and execute configurations are the following: the method iterates through
     * the input list and based on what definitions are present it acts as follows:
     * <ul>
     *     <li>If the entry is a root level configuration (component defined as {@code <}) it executes the
     *     configuration.
     *     If such component is defined somewhere other than the first line (other than having ABSENT as parent), or
     *     the configuration part of this entry is empty, it will throw an exception.</li>
     *     <li>If the entry has an actual component definition like {@code >> LAYOUT} it will go through the following
     *     steps:
     *     <ul>
     *          <li>it saves the components in a {@link ComponentTree},</li>
     *          <li>if there is still another, not yet processed entry in the table,
     *           or the currently processed entry is the last one having both a component and a configuration defined,
     *           then it sets the path leading to the current node as the context,</li>
     *          <li>invokes the methods that add the component to the actual content/page.</li>
     *      </ul>
     *      </li>
     *      <li>If neither of the two preceding conditions is true, it means that the current entry has only
     *      a configuration part defined, which can happen when the configurations of a component are defined
     *      in multiple consecutive table rows.
     *      <p>
     *      It also sets the component context but only when the configuration is not a root level one.</li>
     * </ul>
     * <p>
     * Please note that there is no validation for the following:
     * <ul>
     *     <li>Whether a certain configuration property is defined for a component that is not configuration holder, or is not
     *     the proper configuration for that Component. That will be fairly evident when the test execution fails.</li>
     * </ul>
     */
    public void assembleContent(List<ComponentAndConfiguration> definitions) {
        check(!definitions.isEmpty(), "There is no table entry to process. It should not be empty.");
        dataTableValidator.validateTree(definitions);

        TableBasedAssemblerContext assemblerCtx = new TableBasedAssemblerContext();
        for (ComponentAndConfiguration definition : definitions) {
            if (definition.hasRootLevelConfiguration()) {
                processRootLevelConfiguration(assemblerCtx, definition);
            } else if (definition.hasComponentDefinition()) {
                processComponent(definitions, assemblerCtx, definition);
            } else { //Configuration node (multi-row)
                if (assemblerCtx.getPreviousComponentNode() != ComponentNode.ABSENT) {
                    contextSetter.setContext(tree, assemblerCtx.getPreviousComponentNode(), false);
                }
                processConfiguration(assemblerCtx, definition);
            }
        }
    }

    private void processRootLevelConfiguration(TableBasedAssemblerContext assemblerCtx, ComponentAndConfiguration definition) {
        check(assemblerCtx.getPreviousComponentNode() == ComponentNode.ABSENT,
            "Root level configuration should only be defined in the first row of the data table.");
        check(definition.hasConfiguration(),
            "Root level configuration definition is empty. It should contain some actual configurations.");
        processConfiguration(assemblerCtx, definition);
    }

    private void processComponent(List<ComponentAndConfiguration> definitions, TableBasedAssemblerContext assemblerCtx, ComponentAndConfiguration definition) {
        ComponentNode currentNode = nodeCreator.createComponentNode(definition.getComponent());
        tree.addNode(currentNode, assemblerCtx.getPreviousComponentNode());

        //Next row may contain either another component or a configuration for the current component
        boolean isThereANextRow = definition != definitions.get(definitions.size() - 1);
        if (isThereANextRow || definition.hasConfiguration()) {
            contextSetter.setContext(tree, currentNode, true);
        }
        componentAdder.addComponentToPage(tree.getParentNode(currentNode), currentNode);
        assemblerCtx.setPreviousComponentNode(currentNode);

        //If the current table entry has not just a component but a configuration definition, process that as well.
        if (definition.hasConfiguration()) {
            processConfiguration(assemblerCtx, definition);
        }
    }

    private void processConfiguration(TableBasedAssemblerContext assemblerCtx, ComponentAndConfiguration cc) {
        ConfigurationNode node = nodeCreator.createConfigurationNode(cc.getConfiguration());
        componentConfigurer.configure(assemblerCtx.getPreviousComponentNode().getType(), node);
    }
}
