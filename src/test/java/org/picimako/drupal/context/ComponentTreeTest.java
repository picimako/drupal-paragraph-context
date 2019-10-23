package org.picimako.drupal.context;

import com.google.common.graph.Traverser;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link ComponentTree}.
 */
public class ComponentTreeTest {

    @Mock
    private ComponentTreeBranchTraverser branchTraverser;

    private ComponentTree tree;

    @Before
    public void setup() {
        tree = new ComponentTree();
        initMocks(this);
    }

    @Test
    public void shouldAddNodeWithoutParentIfThereWasNoPreviousNodeProcessed() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        tree.addNode(container, null);

        Iterable<ComponentNode> traversed = traverseTreeFrom(container);
        assertThat(traversed).hasSize(1).first().isSameAs(container);
    }

    @Test
    public void shouldAddNodeWithoutEdgeIfTheNodeIsAtRootLevel() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode container2 = new ComponentNode(1, NodeType.CONTAINER);

        tree.addNode(container, null);
        tree.addNode(layout, container);
        tree.addNode(container2, layout);

        Iterable<ComponentNode> traversed = traverseTreeFrom(container);
        //container 2 won't get traversed since it has no edges to any other node
        assertThat(traversed).contains(container, layout);
        assertThat(tree.getGraph().predecessors(container2)).isEmpty();
        assertThat(container2.getOccurrenceCountUnderParent()).isEqualTo(2);
    }

    @Test
    public void shouldAddNodeWithEdgeToPreviousNodeWhenThisNodeIsOneLevelDeeperThanThePreviousNode() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);

        tree.addNode(container, null);
        tree.addNode(layout, container);

        Iterable<ComponentNode> traversed = traverseTreeFrom(container);
        assertThat(traversed).containsExactly(container, layout);
        assertThat(tree.getGraph().hasEdgeConnecting(container, layout)).isTrue();
    }

    @Test
    public void shouldNotAddNodeWithEdgeToPreviousNodeWhenThisNodeIsMoreThanOneLevelDeeperThanThePreviousNode() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode image = new ComponentNode(3, NodeType.IMAGE);

        tree.addNode(container, null);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> tree.addNode(image, container))
                .withMessageStartingWith("Child defined more than 1 level deeper than its immediate parent is not "
                        + "considered a valid child node.");
    }

    @Test
    public void shouldAddNodeWithEdgeToCommonParentOfPreviousNodeIfThisNodeIsOnAHigherLevelThanThePreviousNode() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, NodeType.IMAGE);
        ComponentNode layout2 = new ComponentNode(2, NodeType.LAYOUT);

        tree.addNode(container, null);
        tree.addNode(layout, container);
        tree.addNode(image, layout);
        tree.addNode(layout2, image);

        when(branchTraverser.getPredecessorsFromBranchOf(layout2)).thenReturn(List.of(container));
        setField(tree, "branchTraverser", branchTraverser, ComponentTreeBranchTraverser.class);

        Iterable<ComponentNode> traversedContainer1 = traverseTreeFrom(container);
        assertThat(traversedContainer1).contains(container, layout, image, layout2);

        assertThat(tree.getGraph().hasEdgeConnecting(container, layout)).isTrue();
        assertThat(tree.getGraph().hasEdgeConnecting(container, layout2)).isTrue();

        Iterable<ComponentNode> traversedContainer2 = traverseTreeFrom(layout2);
        assertThat(traversedContainer2).containsExactly(layout2);
    }

    @Test
    public void shouldAddNodeWithoutEdgeToCommonParentOfPreviousNodeIfTheCurrentNodeIsARootLevelOne() {
        ComponentNode container1 = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, NodeType.IMAGE);
        ComponentNode container2 = new ComponentNode(1, NodeType.CONTAINER);

        tree.addNode(container1, null);
        tree.addNode(layout, container1);
        tree.addNode(image, layout);
        tree.addNode(container2, image);

        Iterable<ComponentNode> traversedContainer1 = traverseTreeFrom(container1);
        assertThat(traversedContainer1).containsExactly(container1, layout, image);

        Iterable<ComponentNode> traversedContainer2 = traverseTreeFrom(container2);
        assertThat(traversedContainer2).containsExactly(container2);

        assertThat(tree.getGraph().predecessors(container2)).isEmpty();
    }

    @Test
    public void shouldAddNodeWithEdgeToCommonParentOfPreviousNodeIfThisNodeIsASiblingOfThePreviousNode() {
        ComponentNode container1 = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, NodeType.IMAGE);
        ComponentNode youtubeVideo = new ComponentNode(3, NodeType.YOUTUBE_VIDEO);

        tree.addNode(container1, null);
        tree.addNode(layout, container1);
        tree.addNode(image, layout);
        tree.addNode(youtubeVideo, image);

        when(branchTraverser.getPredecessorsFromBranchOf(youtubeVideo)).thenReturn(List.of(container1, layout));
        setField(tree, "branchTraverser", branchTraverser, ComponentTreeBranchTraverser.class);

        Iterable<ComponentNode> traversedContainer1 = traverseTreeFrom(container1);
        assertThat(traversedContainer1).contains(container1, layout, image, youtubeVideo);

        assertThat(tree.getGraph().hasEdgeConnecting(layout, image)).isTrue();
        assertThat(tree.getGraph().hasEdgeConnecting(layout, youtubeVideo)).isTrue();
    }

    @Test
    public void shouldCalculateAndSetOccurrenceCountUnderParent() {
        ComponentNode container = new ComponentNode(1, NodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, NodeType.IMAGE);
        ComponentNode layout2 = new ComponentNode(2, NodeType.LAYOUT);

        tree.addNode(container, null);
        tree.addNode(layout, container);
        tree.addNode(image, layout);
        tree.addNode(layout2, image);

        when(branchTraverser.getPredecessorsFromBranchOf(layout2)).thenReturn(List.of(container));
        setField(tree, "branchTraverser", branchTraverser, ComponentTreeBranchTraverser.class);

        assertThat(layout2.getOccurrenceCountUnderParent()).isEqualTo(2);
    }

    @Test
    public void shouldThrowExceptionWhenTheChildIsMoreThanOneLevelDeeperThanItsImmediateParent() {
        ComponentNode nodeHigher = new ComponentNode(2, NodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(4, NodeType.YOUTUBE_VIDEO);

        tree.addNode(nodeHigher, null);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> tree.addNode(nodeDeeper, nodeHigher))
                .withMessage("Child defined more than 1 level deeper than its immediate parent is not considered a valid child node.\n"
                        + "Parent was: [ComponentNode(level=2, type=LAYOUT, occurrenceCountUnderParent=1)]\n"
                        + "Child was: [ComponentNode(level=4, type=YOUTUBE_VIDEO, occurrenceCountUnderParent=1)]");
    }

    private Iterable<ComponentNode> traverseTreeFrom(ComponentNode startNode) {
        return Traverser.forTree(tree.getGraph()).depthFirstPreOrder(startNode);
    }
}
