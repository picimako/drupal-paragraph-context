package io.picimako.drupal.context.treeview;

import io.picimako.drupal.context.ConfigurationNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link TreeViewBasedNodeCreator}.
 */
@RunWith(Parameterized.class)
public class NodeCreatorValidNodeKindTest {

    @Parameter
    public String nodeValue;

    private TreeViewBasedNodeCreator nodeCreator;

    @Before
    public void setup() {
        nodeCreator = new TreeViewBasedNodeCreator();
    }

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {"---* url:"},
            {"---* url: "},
            {"---* url: something"},
            {"---* url: something, color:"},
            {"---* url: something, color: "},
            {"---* url: something, color: rgba(0\\,0\\,0\\,0)"},

            {"* url:\" something\""},
            {"---* url:\" \""},
            {"---* url:\" something\""},
            {"---* url:\" something\", color:"},
            {"---* url:\" something\", color:\" \""},
            {"---* url:\" something\", color:\" rgba(0\\,0\\,0\\,0)\""},
        });
    }

    @Test
    public void shouldThrowExceptionAsBeingInvalidNodes() {
        assertThat(nodeCreator.createNode(nodeValue)).isInstanceOf(ConfigurationNode.class);
    }
}
