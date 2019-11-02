package io.picimako.drupal.context;

import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit test for {@link CssContextSelectorAssembler}.
 */
public class CssContextSelectorAssemblerTest {

    private CssContextSelectorAssembler assembler = new CssContextSelectorAssembler();

    @Test
    public void shouldCreateSelectorFromOneNode() {
        List<ComponentNode> nodes = List.of(new ComponentNode(1, ParagraphNodeType.CONTAINER));
        String expectedCssSelector = ".container:nth-child(1)";

        assertThat(assembler.createCssContextSelectorFrom(nodes)).isEqualTo(expectedCssSelector);
    }

    @Test
    public void shouldCreateSelectorFromMultipleNodes() {
        List<ComponentNode> nodes = List.of(
                new ComponentNode(1, ParagraphNodeType.CONTAINER),
                new ComponentNode(2, ParagraphNodeType.LAYOUT),
                new ComponentNode(3, ParagraphNodeType.IMAGE));
        String expectedCssSelector = ".container:nth-child(1) .layout .image-component:nth-child(1)";

        assertThat(assembler.createCssContextSelectorFrom(nodes)).isEqualTo(expectedCssSelector);
    }

    @Test
    public void shouldCreateSelectorWithProperOccurrenceCount() {
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        container.setOccurrenceCountUnderParent(2);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);
        image.setOccurrenceCountUnderParent(3);

        List<ComponentNode> nodes = List.of(container, new ComponentNode(2, ParagraphNodeType.LAYOUT), image);
        String expectedCssSelector = ".container:nth-child(2) .layout .image-component:nth-child(3)";

        assertThat(assembler.createCssContextSelectorFrom(nodes)).isEqualTo(expectedCssSelector);
    }

    @Test
    public void shouldThrowExceptionIfThereIsNoNodeToConvert() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> assembler.createCssContextSelectorFrom(List.of()));
    }
}
