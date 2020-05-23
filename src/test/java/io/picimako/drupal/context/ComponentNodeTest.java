package io.picimako.drupal.context;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit test for {@link ComponentNode}.
 */
public class ComponentNodeTest {

    @Test
    public void shouldBeAtRootLevel() {
        ComponentNode node = new ComponentNode(1, ParagraphNodeType.CONTAINER);

        assertThat(node.isAtRootLevel()).isTrue();
    }

    @Test
    public void shouldNotBeAtRootLevel() {
        ComponentNode node = new ComponentNode(3, ParagraphNodeType.CONTAINER);

        assertThat(node.isAtRootLevel()).isFalse();
    }

    @Test
    public void shouldHaveInlineConfiguration() {
        ComponentNode node = new ComponentNode(3, ParagraphNodeType.CONTAINER);
        node.setInlineConfig(new ConfigurationNode(Map.of("width", "edge to edge")));

        assertThat(node.hasInlineConfig()).isTrue();
    }

    @Test
    public void shouldThrowExceptionDuringConstructionIfLevelIsLessThanOne() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new ComponentNode(0, ParagraphNodeType.CONTAINER))
                .withMessage("Node level should be at least one. It was: [0]");
    }

    @Test
    public void shouldBeOneLevelHigherThanAnotherNode() {
        ComponentNode nodeHigher = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeHigher.isHigherThan(nodeDeeper)).isTrue();
    }

    @Test
    public void shouldBeMoreThanOneLevelHigherThanAnotherNode() {
        ComponentNode nodeHigher = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(4, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeHigher.isHigherThan(nodeDeeper)).isTrue();
    }

    @Test
    public void shouldNotBeHigherThanAnotherNodeSiblings() {
        ComponentNode nodeHigher = new ComponentNode(3, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeHigher.isHigherThan(nodeDeeper)).isFalse();
    }

    @Test
    public void shouldNotBeHigherThanAnotherNodeDeeper() {
        ComponentNode nodeHigher = new ComponentNode(4, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(2, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeHigher.isHigherThan(nodeDeeper)).isFalse();
    }

    @Test
    public void shouldBeOneLevelDeeperThanAnotherNode() {
        ComponentNode nodeHigher = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeDeeper.isOneLevelDeeperThan(nodeHigher)).isTrue();
    }

    @Test
    public void shouldNotBeDeeperThanAnotherNodeSibling() {
        ComponentNode nodeHigher = new ComponentNode(3, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeDeeper.isOneLevelDeeperThan(nodeHigher)).isFalse();
    }

    @Test
    public void shouldNotBeDeeperThanAnotherNodeHigher() {
        ComponentNode nodeHigher = new ComponentNode(3, ParagraphNodeType.LAYOUT);
        ComponentNode nodeDeeper = new ComponentNode(2, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThat(nodeDeeper.isOneLevelDeeperThan(nodeHigher)).isFalse();
    }

    @Test
    public void shouldThrowExceptionIfOccurrenceCountUnderParentIsLessThanOne() {
        ComponentNode node = new ComponentNode(2, ParagraphNodeType.LAYOUT);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> node.setOccurrenceCountUnderParent(0))
                .withMessage("Occurrence count under parent should be greater than 0. It was: [0].");
    }
}
