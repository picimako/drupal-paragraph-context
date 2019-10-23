package org.picimako.drupal.context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.picimako.drupal.context.NodeType.CONTAINER;
import static org.picimako.drupal.context.NodeType.YOUTUBE_VIDEO;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link ComponentTreeBasedContentAssembler}.
 */
public class ComponentTreeBasedContentAssemblerTest {

    @Mock
    private NodeCreator nodeCreator;
    @Mock
    private ComponentAdder componentAdder;
    @Mock
    private ComponentConfigurer componentConfigurer;
    @Mock
    private ComponentContextSetter contextSetter;
    @Mock
    private DrupalPageSteps drupalPageSteps;
    @Spy
    private ComponentTree tree;

    private ComponentTreeBasedContentAssembler assembler;

    @Before
    public void setup() {
        initMocks(this);
        assembler = new ComponentTreeBasedContentAssembler(drupalPageSteps);
        setField(assembler, "nodeCreator", nodeCreator, NodeCreator.class);
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
        ComponentNode container = mockComponent("- CONTAINER", 1, CONTAINER);

        assembler.assembleContent(componentTree);

        verify(nodeCreator).createNode("- CONTAINER");
        verify(tree).addNode(container, null);
        verify(contextSetter, never()).setContext(any(), any());
        verify(componentAdder).addComponentToPage(container);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container);
    }

    @Test
    public void shouldAssembleMultiComponentNodeContent() {
        String componentTree = "- CONTAINER\n"
                + "-- LAYOUT\n"
                + "--- YOUTUBE_VIDEO";
        ComponentNode container = mockComponent("- CONTAINER", 1, CONTAINER);
        ComponentNode layout = mockComponent("-- LAYOUT", 2, NodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent("--- YOUTUBE_VIDEO", 3, YOUTUBE_VIDEO);

        assembler.assembleContent(componentTree);

        verifyComponent("- CONTAINER", container, null);
        verifyComponent("-- LAYOUT", layout, container);

        verify(nodeCreator).createNode("--- YOUTUBE_VIDEO");
        verify(tree).addNode(youtubeVideo, layout);
        verify(contextSetter, never()).setContext(any(ComponentTree.class), eq(youtubeVideo));
        verify(componentAdder).addComponentToPage(youtubeVideo);
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo);
    }

    @Test
    public void shouldAssembleContentContainingConfigurationNodes() {
        String componentTree = "- CONTAINER\n"
                + "-* bg-image:background.png\n"
                + "-- LAYOUT\n"
                + "--- YOUTUBE_VIDEO\n"
                + "---* title:an_awesome_youtube_video";
        ComponentNode container = mockComponent("- CONTAINER", 1, CONTAINER);
        mockConfiguration("-* bg-image:background.png", Map.of("bg-image", "background.png"));
        ComponentNode layout = mockComponent("-- LAYOUT", 2, NodeType.LAYOUT);
        ComponentNode youtubeVideo = mockComponent("--- YOUTUBE_VIDEO", 3, YOUTUBE_VIDEO);
        mockConfiguration("---* title:an_awesome_youtube_video", Map.of("title", "an_awesome_youtube_video"));

        assembler.assembleContent(componentTree);

        verifyComponent("- CONTAINER", container, null);
        verifyConfiguration("-* bg-image:background.png", CONTAINER, "bg-image", "background.png");
        verifyComponent("-- LAYOUT", layout, container);
        verifyComponent("--- YOUTUBE_VIDEO", youtubeVideo, layout);
        verifyConfiguration("---* title:an_awesome_youtube_video", YOUTUBE_VIDEO, "title", "an_awesome_youtube_video");
        verifyNoMoreInteractions(nodeCreator, tree, contextSetter, componentAdder);

        assertThat(tree.getGraph().nodes()).containsExactly(container, layout, youtubeVideo);
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
        verify(contextSetter).setContext(any(ComponentTree.class), eq(currentNode));
        verify(componentAdder).addComponentToPage(currentNode);
    }

    private void verifyConfiguration(String nodeString, NodeType nodeType, String key, String value) {
        verify(nodeCreator).createNode(nodeString);
        verify(componentConfigurer).configure(eq(nodeType), argThat(node -> node.get(key).equals(value)));
    }
}
