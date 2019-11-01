package org.picimako.drupal.context;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ComponentTreeBranchTraverser}.
 */
public class ComponentTreeBranchTraverserTest {

    private ComponentTreeBranchTraverser branchTraverser;
    private ComponentTree tree;

    @Before
    public void setup() {
        tree = new ComponentTree();
    }

    //getPredecessorsFromBranchOf

    @Test
    public void shouldReturnPredecessorsFromBranchOfANode() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(image, layout);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getPredecessorsFromBranchOf(image))
                .containsExactly(layout, container);
    }

    @Test
    public void shouldReturnPredecessorsFromBranchOfANodeWhenNodeIsSiblingOfThePreviousNode() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(image, layout);
        tree.addNode(youtubeVideo, image);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getPredecessorsFromBranchOf(image))
                .containsExactly(layout, container);
    }

    @Test
    public void shouldReturnAnEmptyListIfTheCurrentNodeIsAtRootLevelAndHasNoPredecessors() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode container2 = new ComponentNode(1, ParagraphNodeType.CONTAINER);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(container2, layout);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getPredecessorsFromBranchOf(container2)).isEmpty();
    }

    //getAllNodesFromBranchOf

    @Test
    public void shouldReturnAllNodesFromBranchOfALeafNode() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(image, layout);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getAllNodesFromBranchOfLeaf(image))
                .containsExactly(container, layout, image);
    }

    @Test
    public void shouldReturnAllNodesFromBranchOfANodeWhenNodeIsSiblingOfThePreviousNode() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(image, layout);
        tree.addNode(youtubeVideo, image);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getAllNodesFromBranchOfLeaf(youtubeVideo))
                .containsExactly(container, layout, youtubeVideo);
    }

    @Test
    public void shouldReturnAnSingletonListIfTheCurrentNodeIsHasNoPredecessors() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode container2 = new ComponentNode(1, ParagraphNodeType.CONTAINER);

        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(container2, layout);

        branchTraverser = ComponentTreeBranchTraverser.on(tree.getGraph());

        assertThat(branchTraverser.getAllNodesFromBranchOfLeaf(container2))
                .containsExactly(container2);
    }
}