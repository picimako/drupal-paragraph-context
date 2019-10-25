package org.picimako.drupal.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ParagraphNodeType}.
 */
public class ParagraphNodeTypeTest {

    @Test
    public void shouldConvertNodeTypeToContextSelector() {
        assertThat(ParagraphNodeType.LAYOUT.toContextSelector()).isEqualTo(ComponentContextSelector.LAYOUT);
    }
}
