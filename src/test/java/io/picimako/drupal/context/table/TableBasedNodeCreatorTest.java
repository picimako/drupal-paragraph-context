package io.picimako.drupal.context.table;

import io.picimako.drupal.context.ComponentNode;
import io.picimako.drupal.context.ConfigurationNode;
import io.picimako.drupal.context.ConfigurationNodeConfigParser;
import io.picimako.drupal.context.ModifierNodeType;
import io.picimako.drupal.context.Node;
import io.picimako.drupal.context.ParagraphNodeType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link TableBasedNodeCreator}.
 */
public class TableBasedNodeCreatorTest {

    @Mock
    public ConfigurationNodeConfigParser parser;
    private TableBasedNodeCreator nodeCreator;

    @Before
    public void setup() {
        initMocks(this);
        nodeCreator = new TableBasedNodeCreator();
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionForCreateNode() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> nodeCreator.createNode(""));
    }

    @Test
    public void shouldConvertParagraphStringToComponentNode() {
        Node node = new ComponentNode(3, ParagraphNodeType.LAYOUT);
        assertThat(nodeCreator.createComponentNode(">>> LAYOUT")).isEqualToComparingFieldByField(node);
    }

    @Test
    public void shouldConvertModifierStringToComponentNode() {
        ComponentNode node = new ComponentNode(3, ModifierNodeType.COLORS_MODIFIER);
        node.setModifierNode(true);
        assertThat(nodeCreator.createComponentNode(">>>@ COLORS_MODIFIER")).isEqualToComparingFieldByField(node);
    }

    @Test
    public void shouldThrowExceptionIfTheComponentStringIsInAnIncorrectFormat() {
        assertThatIllegalArgumentException().isThrownBy(() -> nodeCreator.createComponentNode("--- LAYOUT"))
            .withMessage("The component node is not in a supported format.");
    }

    @Test
    public void shouldConvertStringToConfigurationNode() {
        ReflectionTestUtils.setField(nodeCreator, "parser", parser, ConfigurationNodeConfigParser.class);
        when(parser.parseConfigurationValues("url:someUrl, color:blue"))
            .thenReturn(Map.of("url", "someUrl", "color", "blue"));

        ConfigurationNode node = nodeCreator.createConfigurationNode("url:someUrl, color:blue");
        assertThat(node.get("url")).isEqualTo("someUrl");
        assertThat(node.get("color")).isEqualTo("blue");
    }

    @Test
    public void shouldConvertQuotedStringToConfigurationNode() {
        ReflectionTestUtils.setField(nodeCreator, "parser", parser, ConfigurationNodeConfigParser.class);
        when(parser.parseConfigurationValues("url:\" someUrl \", color:\" blue \""))
            .thenReturn(Map.of("url", " someUrl ", "color", " blue "));

        ConfigurationNode node = nodeCreator.createConfigurationNode("url:\" someUrl \", color:\" blue \"");
        assertThat(node.get("url")).isEqualTo(" someUrl ");
        assertThat(node.get("color")).isEqualTo(" blue ");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationHasNoKeyValueDelimiter() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> nodeCreator.createConfigurationNode("asdsa"))
            .withMessage("The configuration node doesn't contain a valid key-value pair. "
                + "They should be in the following format: <key>:<value>");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationEndsWithAKeyValuePairSeparator() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> nodeCreator.createConfigurationNode("url: someurl,"))
            .withMessage("The configuration node ends with a ':',"
                + " which is not considered a valid configuration node value.");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationEndsWithAKeyValuePairSeparatorAndWhitespace() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> nodeCreator.createConfigurationNode("url: someurl, "))
            .withMessage("The configuration node ends with a ':',"
                + " which is not considered a valid configuration node value.");
    }
}
