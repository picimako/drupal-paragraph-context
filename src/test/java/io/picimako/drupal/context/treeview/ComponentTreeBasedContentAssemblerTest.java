package io.picimako.drupal.context.treeview;

import io.picimako.drupal.context.ComponentAdder;
import io.picimako.drupal.context.ComponentConfigurer;
import io.picimako.drupal.context.ComponentContextSetter;
import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ComponentTree;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.ModifierNodeType;
import io.picimako.drupal.context.NodeType;
import io.picimako.drupal.context.ParagraphNodeType;
import io.picimako.drupal.context.steps.DrupalConfigurationSteps;
import io.picimako.drupal.context.steps.DrupalPageSteps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link ComponentTreeBasedContentAssembler}.
 */
public class ComponentTreeBasedContentAssemblerTest {

    @Mock
    private TreeViewBasedNodeCreator nodeCreator;
    @Mock
    private ComponentAdder componentAdder;
    @Mock
    private ComponentConfigurer componentConfigurer;
    @Mock
    private ComponentContextSetter contextSetter;
    @Mock
    private DrupalPageSteps drupalPageSteps;
    @Mock
    private DrupalConfigurationSteps configurationSteps;
    @Spy
    private ComponentTree tree;

    private ComponentTreeBasedContentAssembler assembler;

    @Before
    public void setup() {
        initMocks(this);
        assembler = new ComponentTreeBasedContentAssembler(drupalPageSteps, configurationSteps);
        setField(assembler, "nodeCreator", nodeCreator, TreeViewBasedNodeCreator.class);
        setField(assembler, "componentAdder", componentAdder, ComponentAdder.class);
        setField(assembler, "componentConfigurer", componentConfigurer, ComponentConfigurer.class);
        setField(assembler, "contextSetter", contextSetter, ComponentContextSetter.class);
        setField(assembler, "tree", tree, ComponentTree.class);
    }

    @Test
    public void shouldThrowExceptionWhenThereIsNoComponentTreeToProcess() {
        String emptyString = "";
        assertThatIllegalArgumentException()
            .isThrownBy(() -> assembler.assembleContent(emptyString))
            .withMessage("There is no component tree to process. It should not be blank.");
    }

    @Test
    public void shouldAssembleSingleNodeContent() {
        String componentTree = "- CONTAINER";
        ComponentNode container = mockComponent("- CONTAINER", 1, ParagraphNodeType.CONTAINER);

        assembler.assembleContent(componentTree);

        verify(nodeCreator).createNode("- CONTAINER");
        verify(tree).addNode(container, ComponentNode.ABSENT);
        verify(tree).getParentNode(container);
        verify(contextSetter, never()).setContext(any(), any(), anyBoolean());
        verify(componentAdder).addComponentToPage(ComponentNode.ABSENT, container);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container);
    }

    @Test
    public void shouldAssembleMultiComponentNodeContent() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "----@ COLORS_MODIFIER";
        ComponentNode container = mockComponent("- CONTAINER", 1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = mockComponent("-- LAYOUT", 2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent("--- YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode colorsModifier = mockComponent("----@ COLORS_MODIFIER", 4, ModifierNodeType.COLORS_MODIFIER);
        colorsModifier.setModifierNode(true);

        assembler.assembleContent(componentTree);

        verifyComponent("- CONTAINER", container, ComponentNode.ABSENT);
        verifyComponent("-- LAYOUT", layout, container);
        verifyComponent("--- YOUTUBE_VIDEO", youtubeVideo, layout);

        verify(nodeCreator).createNode("----@ COLORS_MODIFIER");
        verify(tree).addNode(colorsModifier, youtubeVideo);
        verify(contextSetter, never()).setContext(any(ComponentTree.class), eq(colorsModifier), eq(true));
        verify(componentAdder).addComponentToPage(youtubeVideo, colorsModifier);
        verify(tree).getParentNode(colorsModifier);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo, colorsModifier);
    }

    @Test
    public void shouldAssembleContentContainingConfigurationNodes() {
        String componentTree = "- CONTAINER\n"
            + "-* bg-image:background.png\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "---* title:an_awesome_youtube_video";
        ComponentNode container = mockComponent("- CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockConfiguration("-* bg-image:background.png", Map.of("bg-image", "background.png"));
        ComponentNode layout = mockComponent("-- LAYOUT", 2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent("--- YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockConfiguration("---* title:an_awesome_youtube_video", Map.of("title", "an_awesome_youtube_video"));

        assembler.assembleContent(componentTree);

        verifyComponent("- CONTAINER", container, ComponentNode.ABSENT);
        verifyConfiguration("-* bg-image:background.png", ParagraphNodeType.CONTAINER, "bg-image", "background.png");
        verify(contextSetter).setContext(any(ComponentTree.class), eq(container), eq(false));
        verifyComponent("-- LAYOUT", layout, container);
        verifyComponent("--- YOUTUBE_VIDEO", youtubeVideo, layout);
        verifyConfiguration("---* title:an_awesome_youtube_video", ParagraphNodeType.YOUTUBE_VIDEO, "title", "an_awesome_youtube_video");
        verify(contextSetter).setContext(any(ComponentTree.class), eq(youtubeVideo), eq(false));
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo);
    }

    @Test
    public void shouldNotSetContextForRootLevelConfiguration() {
        String componentTree = "* title:an_awesome_youtube_video";
        mockConfiguration("* title:an_awesome_youtube_video", Map.of("title", "an_awesome_youtube_video"));

        assembler.assembleContent(componentTree);

        verifyConfiguration("* title:an_awesome_youtube_video", ParagraphNodeType.ABSENT, "title", "an_awesome_youtube_video");
        verify(contextSetter, never()).setContext(any(ComponentTree.class), eq(ComponentNode.ABSENT), eq(false));
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);
    }

    private ComponentNode mockComponent(String nodeString, int level, NodeType nodeType) {
        ComponentNode node = new ComponentNode(level, nodeType);
        when(nodeCreator.createNode(nodeString)).thenReturn(node);
        return node;
    }

    private void mockConfiguration(String nodeString, Map<String, String> configuration) {
        ConfigurationNode node = new ConfigurationNode(configuration);
        when(nodeCreator.createNode(nodeString)).thenReturn(node);
    }

    private void verifyComponent(String nodeString, ComponentNode currentNode, ComponentNode previousNode) {
        verify(nodeCreator).createNode(nodeString);
        verify(tree).addNode(currentNode, previousNode);
        verify(tree).getParentNode(currentNode);
        verify(contextSetter).setContext(any(ComponentTree.class), eq(currentNode), eq(true));
        verify(componentAdder).addComponentToPage(previousNode, currentNode);
    }

    private void verifyConfiguration(String nodeString, NodeType nodeType, String key, String value) {
        verify(nodeCreator).createNode(nodeString);
        verify(componentConfigurer).configure(eq(nodeType), argThat(node -> node.get(key).equals(value)));
    }
}
