package io.picimako.drupal.context;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit test for {@link NodeCreator}.
 */
public class NodeCreatorTest {

    @Mock
    public ConfigurationNodeConfigParser parser;
    private NodeCreator nodeCreator;

    @Before
    public void setup() {
        initMocks(this);
        nodeCreator = new NodeCreator();
    }

    @Test
    public void shouldConvertParagraphStringToComponentNode() {
        Node node = new ComponentNode(3, ParagraphNodeType.LAYOUT);
        assertThat(nodeCreator.createNode("--- LAYOUT")).isEqualToComparingFieldByField(node);
    }

    @Test
    public void shouldConvertModifierStringToComponentNode() {
        ComponentNode node = new ComponentNode(3, ModifierNodeType.COLORS_MODIFIER);
        node.setModifierNode(true);
        assertThat(nodeCreator.createNode("---@ COLORS_MODIFIER")).isEqualToComparingFieldByField(node);
    }

    @Test
    public void shouldConvertStringToConfigurationNode() {
        ReflectionTestUtils.setField(nodeCreator, "parser", parser, ConfigurationNodeConfigParser.class);
        when(parser.parseConfigurationValues("url:someUrl, color:blue"))
                .thenReturn(Map.of("url", "someUrl", "color", "blue"));

        ConfigurationNode node = (ConfigurationNode) nodeCreator.createNode("---* url:someUrl, color:blue");
        assertThat(node.get("url")).isEqualTo("someUrl");
        assertThat(node.get("color")).isEqualTo("blue");
    }

    @Test
    public void shouldConvertQuotedStringToConfigurationNode() {
        ReflectionTestUtils.setField(nodeCreator, "parser", parser, ConfigurationNodeConfigParser.class);
        when(parser.parseConfigurationValues("url:\" someUrl \", color:\" blue \""))
            .thenReturn(Map.of("url", " someUrl ", "color", " blue "));

        ConfigurationNode node = (ConfigurationNode) nodeCreator.createNode("---* url:\" someUrl \", color:\" blue \"");
        assertThat(node.get("url")).isEqualTo(" someUrl ");
        assertThat(node.get("color")).isEqualTo(" blue ");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationConsistsOnlyOfAKeyOrAValue() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> nodeCreator.createNode("--- asdsa"))
                .withMessage("The provided line from the component tree is not valid: [--- asdsa]");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationHasNoKeyValueDelimiter() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> nodeCreator.createNode("---* asdsa"))
                .withMessage("The configuration node doesn't contain a valid key-value pair. "
                        + "They should be in the following format: <key>:<value>");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationEndsWithAKeyValuePairSeparator() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> nodeCreator.createNode("---* url: someurl,"))
                .withMessage("The configuration node ends with a ':',"
                        + " which is not considered a valid configuration node value.");
    }

    @Test
    public void shouldThrowExceptionWhenConfigurationEndsWithAKeyValuePairSeparatorAndWhitespace() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> nodeCreator.createNode("---* url: someurl, "))
                .withMessage("The configuration node ends with a ':',"
                        + " which is not considered a valid configuration node value.");
    }

    @Test
    public void shouldBeConfigurationNode() {
        assertThat(NodeCreator.isConfigurationNode("-* url: something")).isTrue();
    }

    @Test
    public void shouldNotConvertParagraphToConfigurationNode() {
        assertThat(NodeCreator.isConfigurationNode("-- CONTAINER")).isFalse();
    }

    @Test
    public void shouldNotConvertModifierToConfigurationNode() {
        assertThat(NodeCreator.isConfigurationNode("--@ COLORS_MODIFIER")).isFalse();
    }
}
