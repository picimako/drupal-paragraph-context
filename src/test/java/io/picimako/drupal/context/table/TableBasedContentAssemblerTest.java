package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentAdder;
import io.picimako.drupal.context.ComponentConfigurer;
import io.picimako.drupal.context.ComponentContextSetter;
import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ComponentTree;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.ModifierNodeType;
import io.picimako.drupal.context.NodeCreator;
import io.picimako.drupal.context.NodeType;
import io.picimako.drupal.context.ParagraphNodeType;
import io.picimako.drupal.context.steps.DrupalConfigurationSteps;
import io.picimako.drupal.context.steps.DrupalPageSteps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.picimako.drupal.context.table.ComponentAndConfiguration.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link TableBasedContentAssembler}.
 */
public class TableBasedContentAssemblerTest {

    @Mock
    private TableBasedNodeCreator nodeCreator;
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

    private TableBasedContentAssembler assembler;

    @Before
    public void setup() {
        initMocks(this);
        assembler = new TableBasedContentAssembler(drupalPageSteps, configurationSteps);
        setField(assembler, "nodeCreator", nodeCreator, NodeCreator.class);
        setField(assembler, "componentAdder", componentAdder, ComponentAdder.class);
        setField(assembler, "componentConfigurer", componentConfigurer, ComponentConfigurer.class);
        setField(assembler, "contextSetter", contextSetter, ComponentContextSetter.class);
        setField(assembler, "tree", tree, ComponentTree.class);
    }

    @Test
    public void shouldThrowExceptionIfRootLevelConfigurationIsNotDefinedInTheFirstTableRow() {
        List<ComponentAndConfiguration> ccs = List.of(create("> CONTAINER"), create(">> LAYOUT"), create("<"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);

        assertThatIllegalArgumentException().isThrownBy(() -> assembler.assembleContent(ccs))
            .withMessage("Root level configuration should only be defined in the first row of the data table.");
    }

    @Test
    public void shouldExecuteRootLevelConfiguration() {
        assembler.assembleContent(List.of(create("<", "title:someTitle")));

        verify(nodeCreator).createConfigurationNode("title:someTitle");
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);
    }

    @Test
    public void shouldThrowExceptionIfRootLevelConfigurationHasNoConfiguration() {
        assertThatIllegalArgumentException().isThrownBy(() -> assembler.assembleContent(List.of(create("<"))))
            .withMessage("Root level configuration definition is empty. It should contain some actual configurations.");
    }

    @Test
    public void shouldExecuteMultiRowConfiguration() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("<", "title:someTitle"),
            create("", "path:/some/path"));
        assembler.assembleContent(ccs);

        verify(nodeCreator).createConfigurationNode("title:someTitle");
        verify(nodeCreator).createConfigurationNode("path:/some/path");
        verify(contextSetter).setContext(any(ComponentTree.class), any(ComponentNode.class), eq(false));
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);
    }

    @Test
    public void shouldThrowExceptionWhenThereIsComponentsToProcess() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> assembler.assembleContent(Collections.emptyList()))
            .withMessage("There is no table entry to process. It should not be empty.");
    }

    @Test
    public void shouldAssembleSingleNodeContent() {
        ComponentNode container = mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);

        assembler.assembleContent(List.of(create("> CONTAINER")));

        verify(nodeCreator).createComponentNode("> CONTAINER");
        verify(tree).addNode(container, ComponentNode.ABSENT);
        verify(tree).getParentNode(container);
        verify(contextSetter, never()).setContext(any(), any(), anyBoolean());
        verify(componentAdder).addComponentToPage(ComponentNode.ABSENT, container);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container);
    }

    @Test
    public void shouldAssembleMultiComponentNodeContent() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"), create(">>>>@ COLORS_MODIFIER")
        );
        ComponentNode container = mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode colorsModifier = mockComponent(">>>>@ COLORS_MODIFIER", 4, ModifierNodeType.COLORS_MODIFIER);
        colorsModifier.setModifierNode(true);

        assembler.assembleContent(ccs);

        verifyComponent("> CONTAINER", container, ComponentNode.ABSENT);
        verifyComponent(">> LAYOUT", layout, container);
        verifyComponent(">>> YOUTUBE_VIDEO", youtubeVideo, layout);

        verify(nodeCreator).createComponentNode(">>>>@ COLORS_MODIFIER");
        verify(tree).addNode(colorsModifier, youtubeVideo);
        verify(tree).getParentNode(colorsModifier);
        verify(contextSetter, never()).setContext(any(ComponentTree.class), eq(colorsModifier), eq(true));
        verify(componentAdder).addComponentToPage(youtubeVideo, colorsModifier);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo, colorsModifier);
    }

    @Test
    public void shouldAssembleContentContainingConfigurationNodes() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER", "bg-image:background.png"),
            create(">> LAYOUT"),
            create(">>> YOUTUBE_VIDEO", "title:an_awesome_youtube_video")
        );
        ComponentNode container = mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockConfiguration("bg-image:background.png", Map.of("bg-image", "background.png"));
        ComponentNode layout = mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockConfiguration("title:an_awesome_youtube_video", Map.of("title", "an_awesome_youtube_video"));

        assembler.assembleContent(ccs);

        verifyComponent("> CONTAINER", container, ComponentNode.ABSENT);
        verifyConfiguration("bg-image:background.png", ParagraphNodeType.CONTAINER, "bg-image", "background.png");
        verifyComponent(">> LAYOUT", layout, container);
        verifyComponent(">>> YOUTUBE_VIDEO", youtubeVideo, layout);
        verifyConfiguration("title:an_awesome_youtube_video", ParagraphNodeType.YOUTUBE_VIDEO, "title", "an_awesome_youtube_video");
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo);
    }

    private ComponentNode mockComponent(String nodeString, int level, NodeType nodeType) {
        ComponentNode node = new ComponentNode(level, nodeType);
        when(nodeCreator.createComponentNode(nodeString)).thenReturn(node);
        return node;
    }

    private void mockConfiguration(String nodeString, Map<String, String> configuration) {
        ConfigurationNode node = new ConfigurationNode(configuration);
        when(nodeCreator.createConfigurationNode(nodeString)).thenReturn(node);
    }

    private void verifyComponent(String nodeString, ComponentNode currentNode, ComponentNode previousNode) {
        verify(nodeCreator).createComponentNode(nodeString);
        verify(tree).addNode(currentNode, previousNode);
        verify(tree).getParentNode(currentNode);
        verify(contextSetter).setContext(any(ComponentTree.class), eq(currentNode), eq(true));
        verify(componentAdder).addComponentToPage(previousNode, currentNode);
    }

    private void verifyConfiguration(String nodeString, NodeType nodeType, String key, String value) {
        verify(nodeCreator).createConfigurationNode(nodeString);
        verify(componentConfigurer).configure(eq(nodeType), argThat(node -> node.get(key).equals(value)));
    }
}
