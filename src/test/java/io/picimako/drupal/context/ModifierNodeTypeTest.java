package io.picimako.drupal.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link ModifierNodeType}.
 */
public class ModifierNodeTypeTest {

    @Test
    public void shouldConvertNodeTypeToContextSelector() {
        assertThat(ModifierNodeType.COLORS_MODIFIER.toContextSelector()).isEqualTo(ComponentContextSelector.COLORS_MODIFIER);
    }
}