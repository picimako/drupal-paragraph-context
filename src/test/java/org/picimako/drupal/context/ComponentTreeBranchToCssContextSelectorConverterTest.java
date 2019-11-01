package org.picimako.drupal.context;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

/**
 * Unit test for {@link ComponentTreeBranchToCssContextSelectorConverter}.
 */
public class ComponentTreeBranchToCssContextSelectorConverterTest {

    @Mock
    private CssContextSelectorAssembler assembler;
    private ComponentTreeBranchToCssContextSelectorConverter converter;

    @Before
    public void setup() {
        converter = new ComponentTreeBranchToCssContextSelectorConverter();
        initMocks(this);
    }

    @Test
    public void shouldConvertCurrentNodeIntoSingletonListIfItIsAtRootLevel() {
        ComponentTree tree = new ComponentTree();
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        tree.addNode(container, ComponentNode.ABSENT);

        when(assembler.createCssContextSelectorFrom(argThat(list -> list.size() == 1))).thenReturn(".container");
        setField(converter, "selectorAssembler", assembler, CssContextSelectorAssembler.class);

        assertThat(converter.convert(tree, container)).isEqualTo(".container");
    }

    @Test
    public void shouldConvertNodesFromBranchIfCurrentNodeIsNotAtRootLevel() {
        ComponentTree tree = new ComponentTree();
        ComponentNode container = new ComponentNode(1, ParagraphNodeType.CONTAINER);
        ComponentNode layout = new ComponentNode(2, ParagraphNodeType.LAYOUT);
        ComponentNode image = new ComponentNode(3, ParagraphNodeType.IMAGE);
        tree.addNode(container, ComponentNode.ABSENT);
        tree.addNode(layout, container);
        tree.addNode(image, layout);

        when(assembler.createCssContextSelectorFrom(argThat(list -> list.size() > 1))).thenReturn(".container .layout div.image");
        setField(converter, "selectorAssembler", assembler, CssContextSelectorAssembler.class);

        assertThat(converter.convert(tree, image)).isEqualTo(".container .layout div.image");
    }
}
