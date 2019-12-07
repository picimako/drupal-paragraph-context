package io.picimako.drupal.context.treeview;

import io.picimako.drupal.context.ComponentAdder;
import io.picimako.drupal.context.ComponentConfigurer;
import io.picimako.drupal.context.ComponentContextSetter;
import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ComponentTree;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.Node;
import io.picimako.drupal.context.steps.DrupalConfigurationSteps;
import io.picimako.drupal.context.steps.DrupalPageSteps;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses a component tree string from a given format, iterates through its lines and based on what type of node a
 * given line is in the tree (component or configuration), it acts accordingly:
 * <ul>
 *     <li>In case of Component nodes it saves the nodes in a {@link ComponentTree} and adds it to the content.</li>
 *     <li>In case of Configuration nodes it executes the Gherkin steps or any other action mapped to the previously
 *     parsed and saved Component Node.</li>
 * </ul>
 * <p>
 * An input tree may look like:
 * <pre>
 * - CONTAINER <- This is a Component node at root (1) level.
 * -- LAYOUT
 * --- IMAGE   <- This is also a Component node at the 3rd level.
 * --- RICH_TEXT
 * ---* text: some text
 * -- LAYOUT
 * --- CAROUSEL
 * ---- VIDEO
 * ----* url:https://some.url, initialTime:16    <- This is a Configuration node.
 *                                                  The level marker is only for consistency with the rest of the tree.
 * ---- VIDEO
 * ----* url:https://some.other/url, initialTime:10
 * -----@ ABSOLUTE_HEIGHT_MODIFIER    <- This is a Modifier Component node for the last VIDEO component.
 * </pre>
 */
public class ComponentTreeBasedContentAssembler {

    private final ComponentTree tree = new ComponentTree();
    private final TreeViewBasedNodeCreator nodeCreator = new TreeViewBasedNodeCreator();
    private final TreeViewBasedComponentTreeValidator componentTreeValidator = new TreeViewBasedComponentTreeValidator();
    private final ComponentConfigurer componentConfigurer;
    private final ComponentAdder componentAdder;
    private final ComponentContextSetter contextSetter;

    /**
     * Creates a new {@link ComponentTreeBasedContentAssembler} instance.
     * <p>
     * The parameter {@code steps} in this case is necessary because that is the dedicated step definitions class that
     * is responsible for providing methods for adding components and setting context selectors.
     * <p>
     * Of course in a different kind of framework/solution this step definitions class may be different or not be a
     * step definitions class at all.
     *
     * @param steps a step definitions class for handling component addition and context setting
     */
    public ComponentTreeBasedContentAssembler(DrupalPageSteps steps, DrupalConfigurationSteps configSteps) {
        requireNonNull(steps);
        this.componentAdder = new ComponentAdder(steps);
        this.contextSetter = new ComponentContextSetter(steps);
        this.componentConfigurer = new ComponentConfigurer(configSteps);
    }

    /**
     * Assembles a Drupal or other CMS content.
     * <p>
     * The steps it takes to parse nodes and execute configuration nodes are the following:
     * <ul>
     *     <li>Separates the input tree into lines.</li>
     *     <li>Iterates through the lines, parses and converts each of them to either a Component or a Configuration
     *     node.</li>
     *     <li><ul>
     *       <li>In case of Component nodes
     *          <ul>
     *           <li>it saves the nodes in a {@link ComponentTree},</li>
     *           <li>if there is still another, not yet processed node, besides the current node in the tree,
     *           then it sets the path leading to the current node as the context,</li>
     *           <li>invokes the methods that add the component to the actual content/page.</li>
     *          </ul></li>
     *       <li>In case of Configuration nodes it executes the Gherkin steps or any other actions
     *       mapped to the previously parsed and saved Component Node.
     *       <p>
     *       It also sets the component context but only when the configuration is not a root level one.</li>
     *      </ul></li>
     * </ul>
     * <p>
     * Please note that there is no validation for the following things (see the reasons for each):
     * <ul>
     *     <li>Whether there are only configuration nodes in the argument tree. It would require traversing
     *     the whole tree in advance just to validate this. Also I consider this a really big edge case.</li>
     *     <li>Whether a configuration node is put after a component node that is not configuration holder, or is not
     *     the proper configuration for that Component. That will be fairly evident when the test execution fails.</li>
     * </ul>
     */
    public void assembleContent(String componentTree) {
        checkArgument(!isBlank(componentTree), "There is no component tree to process. It should not be blank.");
        componentTreeValidator.validateTree(componentTree);

        TreeViewAssemblerContext assemblerCtx = new TreeViewAssemblerContext(componentTree.split("\n"));
        for (int i = 0; i < assemblerCtx.nodeCount(); i++) {
            //NOTE: somewhere inside this for loop additional logging might be placed to track the progress of the assembler
            String line = assemblerCtx.getStringNode(i);
            assemblerCtx.setIndex(i);
            Node node = nodeCreator.createNode(line);
            if (node instanceof ComponentNode) {
                ComponentNode currentNode = (ComponentNode) node;
                tree.addNode(currentNode, assemblerCtx.getPreviousComponentNode());
                setComponentContext(currentNode, assemblerCtx);
                componentAdder.addComponentToPage(tree.getParentNode(currentNode), currentNode);
                assemblerCtx.setPreviousComponentNode(currentNode);
            } else {
                if (assemblerCtx.getPreviousComponentNode() != ComponentNode.ABSENT) {
                    contextSetter.setContext(tree, assemblerCtx.getPreviousComponentNode(), false);
                }
                componentConfigurer.configure(assemblerCtx.getPreviousComponentNode().getType(), (ConfigurationNode) node);
            }
        }
    }

    /**
     * Component context setting should happen when there is still at least one other unprocessed
     * node in the tree besides {@code currentNode}, which can either be a configuration node
     * or a component node that is one level deeper than this node (TODO: no validation yet),
     * or a Modifier node.
     */
    private void setComponentContext(ComponentNode currentNode, TreeViewAssemblerContext context) {
        if (context.hasNextNode()) {
            contextSetter.setContext(tree, currentNode, true);
        }
    }
}
