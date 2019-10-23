package org.picimako.drupal.context;

import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Unit test for {@link ConfigurationNodeConfigParser}.
 */
public class ConfigurationNodeConfigParserTest {

    private ConfigurationNodeConfigParser parser = new ConfigurationNodeConfigParser();

    @Test
    public void shouldThrowExceptionIfConfigurationIsEmpty() {
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parseConfigurationValues(""));
    }

    @Test
    public void shouldThrowExceptionIfConfigurationIsNull() {
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parseConfigurationValues(null));
    }

    @Test
    public void shouldThrowExceptionIfConfigurationIsOnlyWhitespaces() {
        assertThatIllegalArgumentException().isThrownBy(() -> parser.parseConfigurationValues("   "));
    }

    @Test
    public void shouldParseConfigurationValues() {
        String configuration = "url:https://duckduckgo.com?param=value, image:someimage";
        Map<String, String> expectedConfiguration = Map.of("url", "https://duckduckgo.com?param=value", "image", "someimage");

        assertThat(parser.parseConfigurationValues(configuration)).containsAllEntriesOf(expectedConfiguration);
    }

    @Test
    public void shouldParseConfigurationContainingEscapedCommas() {
        String configuration = "path:/some/path, text:Overconfidence\\, this\\, and a small screwdriver.";
        Map<String, String> expectedConfiguration = Map.of("path", "/some/path", "text", "Overconfidence, this, and a small screwdriver.");

        assertThat(parser.parseConfigurationValues(configuration)).containsAllEntriesOf(expectedConfiguration);
    }

    @Test
    public void shouldNotTrimEntryValues() {
        String configuration = "url: https://duckduckgo.com?param=value, image: someimage";
        Map<String, String> expectedConfiguration = Map.of("url", " https://duckduckgo.com?param=value", "image", " someimage");

        assertThat(parser.parseConfigurationValues(configuration)).containsAllEntriesOf(expectedConfiguration);
    }

    @Test
    public void shouldNotTrimEntryValuesAtTheirEnds() {
        String configuration = "url:https://duckduckgo.com?param=value   , image:someimage   ";
        Map<String, String> expectedConfiguration = Map.of("url", "https://duckduckgo.com?param=value   ", "image", "someimage   ");

        assertThat(parser.parseConfigurationValues(configuration)).containsAllEntriesOf(expectedConfiguration);
    }

    @Test
    public void shouldThrowExceptionWhenAKeyValuePairDoesNotContainAKeyValueDelimiter() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> parser.parseConfigurationValues("url: something, color"))
                .withMessage("There is at least one configuration entry that doesn't have a key or a value part.");
    }
}
