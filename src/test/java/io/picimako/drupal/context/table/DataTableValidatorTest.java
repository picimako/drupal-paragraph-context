package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ModifierNodeType;
import io.picimako.drupal.context.NodeType;
import io.picimako.drupal.context.ParagraphNodeType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;

import static io.picimako.drupal.context.table.ComponentAndConfiguration.create;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link DataTableValidator}.
 */
public class DataTableValidatorTest {

    @Mock
    private TableBasedNodeCreator nodeCreator;
    private DataTableValidator validator;

    @Before
    public void setup() {
        initMocks(this);
        validator = new DataTableValidator();
        setField(validator, "nodeCreator", nodeCreator, TableBasedNodeCreator.class);
    }

    @Test
    public void shouldNotFailValidationForTableWithoutModifier() {
        List<ComponentAndConfiguration> ccs = List.of(create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);

        assertThatCode(() -> validator.validateTree(ccs)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotFailValidationForTableWithModifier() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"), create(">>>@ COLORS_MODIFIER"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockModifier(">>>@ COLORS_MODIFIER", 3, ModifierNodeType.COLORS_MODIFIER);

        assertThatCode(() -> validator.validateTree(ccs)).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotFailValidationForTableWithModifierAndConfigurationEntries() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"),
            create(">>>@ COLORS_MODIFIER", "color:#00CC00"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);

        mockModifier(">>>@ COLORS_MODIFIER", 3, ModifierNodeType.COLORS_MODIFIER);

        assertThatCode(() -> validator.validateTree(ccs)).doesNotThrowAnyException();
    }

    @Test
    public void shouldFailValidationWhenNoTableEntryHasComponentDefinition() {
        List<ComponentAndConfiguration> ccs = List.of(create("", "color:#00CC00"), create(""));

        assertThatIllegalArgumentException().isThrownBy(() -> validator.validateTree(ccs))
            .withMessage("None of the entries in the input data table has a component defined.");
    }

    @Test
    public void shouldSkipMultiRowConfigurationEntries() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"),
            create(">>> YOUTUBE_VIDEO"),
            create("", "color:#00CC00, "),
            create(">>>@ COLORS_MODIFIER", "color:#00CC00"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockModifier(">>>@ COLORS_MODIFIER", 3, ModifierNodeType.COLORS_MODIFIER);

        assertThatCode(() -> validator.validateTree(ccs)).doesNotThrowAnyException();
    }

    @Test
    public void shouldSkipRootLevelConfigurationEntry() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("<", "color:#00CC00, "), create("> CONTAINER"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);

        assertThatCode(() -> validator.validateTree(ccs)).doesNotThrowAnyException();
        verify(nodeCreator).createComponentNode("> CONTAINER");
        verify(nodeCreator, never()).createComponentNode("<");
    }

    @Test
    public void shouldThrowExceptionWhenModifierIsDefinedOnADeeperLevelThanPreviousComponentNode() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"), create(">>>>>@ COLORS_MODIFIER"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockModifier(">>>>>@ COLORS_MODIFIER", 5, ModifierNodeType.COLORS_MODIFIER);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> validator.validateTree(ccs))
            .withMessage("Modifier node should be at the same level as the previous paragraph or modifier node.\n"
                + "Parent was: [ComponentNode(level=3, type=YOUTUBE_VIDEO, occurrenceCountUnderParent=1, isModifierNode=false)]\n"
                + "Child was: [ComponentNode(level=5, type=COLORS_MODIFIER, occurrenceCountUnderParent=1, isModifierNode=true)]");
    }

    @Test
    public void shouldThrowExceptionWhenComponentIsDefinedMoreThanOneLevelDeeperThanPreviousComponentNode() {
        List<ComponentAndConfiguration> ccs = List.of(
            create("> CONTAINER"), create(">> LAYOUT"), create(">>> YOUTUBE_VIDEO"), create(">>>>> IMAGE"));

        mockComponent("> CONTAINER", 1, ParagraphNodeType.CONTAINER);
        mockComponent(">> LAYOUT", 2, ParagraphNodeType.LAYOUT);
        mockComponent(">>> YOUTUBE_VIDEO", 3, ParagraphNodeType.YOUTUBE_VIDEO);
        mockComponent(">>>>> IMAGE", 5, ParagraphNodeType.IMAGE);

        assertThatIllegalArgumentException()
            .isThrownBy(() -> validator.validateTree(ccs))
            .withMessageStartingWith("Child defined more than 1 level deeper than its immediate parent is not "
                + "considered a valid child node.\n"
                + "Parent was: [ComponentNode(level=3, type=YOUTUBE_VIDEO, occurrenceCountUnderParent=1, isModifierNode=false)]\n"
                + "Child was: [ComponentNode(level=5, type=IMAGE, occurrenceCountUnderParent=1, isModifierNode=false)]");
    }

    private ComponentNode mockComponent(String nodeString, int level, NodeType nodeType) {
        ComponentNode node = new ComponentNode(level, nodeType);
        when(nodeCreator.createComponentNode(nodeString)).thenReturn(node);
        return node;
    }

    private ComponentNode mockModifier(String nodeString, int level, NodeType nodeType) {
        ComponentNode node = new ComponentNode(level, nodeType);
        node.setModifierNode(true);
        when(nodeCreator.createComponentNode(nodeString)).thenReturn(node);
        return node;
    }
}
