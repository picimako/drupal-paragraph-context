package io.picimako.drupal.context;

import io.picimako.drupal.context.treeview.TreeViewBasedComponentTreeValidator;
import io.picimako.drupal.context.treeview.TreeViewBasedNodeCreator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link TreeViewBasedComponentTreeValidator}.
 */
public class TreeViewBasedComponentTreeValidatorTest {

    @Mock
    private TreeViewBasedNodeCreator nodeCreator;
    private TreeViewBasedComponentTreeValidator componentTreeValidator;

    @Before
    public void setup() {
        initMocks(this);
        componentTreeValidator = new TreeViewBasedComponentTreeValidator();
        setField(componentTreeValidator, "nodeCreator", nodeCreator, TreeViewBasedNodeCreator.class);
    }

    @Test
    public void shouldNotFailValidationForTreeWithoutModifier() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO";

        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);

        when(nodeCreator.createNode("- CONTAINER")).thenReturn(container);
        when(nodeCreator.createNode("-- LAYOUT")).thenReturn(layout);
        when(nodeCreator.createNode("--- YOUTUBE_VIDEO")).thenReturn(youtubeVideo);

        assertThatCode(() -> componentTreeValidator.validateTree(componentTree)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotFailValidationForTreeWithModifier() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "---@ COLORS_MODIFIER";

        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode colorsModifier = new ComponentNode(3, ModifierNodeType.COLORS_MODIFIER);
        colorsModifier.setModifierNode(true);

        when(nodeCreator.createNode("- CONTAINER")).thenReturn(container);
        when(nodeCreator.createNode("-- LAYOUT")).thenReturn(layout);
        when(nodeCreator.createNode("--- YOUTUBE_VIDEO")).thenReturn(youtubeVideo);
        when(nodeCreator.createNode("---@ COLORS_MODIFIER")).thenReturn(colorsModifier);

        assertThatCode(() -> componentTreeValidator.validateTree(componentTree)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotFailValidationForTreeWithModifierAndConfigurationNodes() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "---@ COLORS_MODIFIER\n"
            + "---* color:#00CC00";

        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode colorsModifier = new ComponentNode(3, ModifierNodeType.COLORS_MODIFIER);
        colorsModifier.setModifierNode(true);
        ConfigurationNode colorsModifierConfig = new ConfigurationNode(Map.of("color", "#00CC00"));

        when(nodeCreator.createNode("- CONTAINER")).thenReturn(container);
        when(nodeCreator.createNode("-- LAYOUT")).thenReturn(layout);
        when(nodeCreator.createNode("--- YOUTUBE_VIDEO")).thenReturn(youtubeVideo);
        when(nodeCreator.createNode("---@ COLORS_MODIFIER")).thenReturn(colorsModifier);
        when(nodeCreator.createNode("---* color:#00CC00")).thenReturn(colorsModifierConfig);

        assertThatCode(() -> componentTreeValidator.validateTree(componentTree)).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowExceptionWhenModifierIsDefinedOnADeeperLevelThanPreviousComponentNode() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "-----@ COLORS_MODIFIER";
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode colorsModifier = new ComponentNode(5, ModifierNodeType.COLORS_MODIFIER);
        colorsModifier.setModifierNode(true);

        when(nodeCreator.createNode("- CONTAINER")).thenReturn(container);
        when(nodeCreator.createNode("-- LAYOUT")).thenReturn(layout);
        when(nodeCreator.createNode("--- YOUTUBE_VIDEO")).thenReturn(youtubeVideo);
        when(nodeCreator.createNode("-----@ COLORS_MODIFIER")).thenReturn(colorsModifier);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> componentTreeValidator.validateTree(componentTree))
            .withMessage("Modifier node should be at the same level as the previous paragraph or modifier node.\n"
                + "Parent was: [ComponentNode(level=3, type=YOUTUBE_VIDEO, occurrenceCountUnderParent=1, isModifierNode=false)]\n"
                + "Child was: [ComponentNode(level=5, type=COLORS_MODIFIER, occurrenceCountUnderParent=1, isModifierNode=true)]");
    }

    @Test
    public void shouldThrowExceptionWhenComponentIsDefinedMoreThanOneLevelDeeperThanPreviousComponentNode() {
        String componentTree = "- CONTAINER\n"
            + "-- LAYOUT\n"
            + "--- YOUTUBE_VIDEO\n"
            + "----- IMAGE";

        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode youtubeVideo = new ComponentNode(3, ParagraphNodeType.YOUTUBE_VIDEO);
        ComponentNode image = new ComponentNode(5, ParagraphNodeType.IMAGE);

        when(nodeCreator.createNode("- CONTAINER")).thenReturn(container);
        when(nodeCreator.createNode("-- LAYOUT")).thenReturn(layout);
        when(nodeCreator.createNode("--- YOUTUBE_VIDEO")).thenReturn(youtubeVideo);
        when(nodeCreator.createNode("----- IMAGE")).thenReturn(image);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> componentTreeValidator.validateTree(componentTree))
            .withMessageStartingWith("Child defined more than 1 level deeper than its immediate parent is not "
                + "considered a valid child node.\n"
                + "Parent was: [ComponentNode(level=3, type=YOUTUBE_VIDEO, occurrenceCountUnderParent=1, isModifierNode=false)]\n"
                + "Child was: [ComponentNode(level=5, type=IMAGE, occurrenceCountUnderParent=1, isModifierNode=false)]");
    }
}
