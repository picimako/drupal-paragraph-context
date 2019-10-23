package org.picimako.drupal.context;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link NodeType}.
 */
public class NodeTypeTest {

    @Test
    public void shouldConvertNodeTypeToContextSelector() {
        assertThat(NodeType.LAYOUT.toContextSelector()).isEqualTo(ComponentContextSelector.LAYOUT);
    }
}
