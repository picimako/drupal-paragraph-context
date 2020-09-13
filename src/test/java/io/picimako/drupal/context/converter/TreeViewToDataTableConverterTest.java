package io.picimako.drupal.context.converter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import java.util.List;

/**
 * Unit test for {@link TreeViewToDataTableConverter}.
 */
public class TreeViewToDataTableConverterTest {

    private final TreeViewToDataTableConverter converter = new TreeViewToDataTableConverter();

    @Test
    public void shouldConvertSingleLineRootLevelConfiguration() {
        List<String> input = List.of("* title:\"Some page title\"");
        String output = "| < | title:\"Some page title\" |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertMultilineRootLevelConfiguration() {
        List<String> input = List.of("* title:\"Some page title\"", "* meta-keywords:keywords");
        String output =
            "| < | title:\"Some page title\" |\n"
                + "|  | meta-keywords:keywords |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertParagraphs() {
        List<String> input = List.of(
            "- CONTAINER",
            "-- LAYOUT",
            "--- IMAGE");
        String output =
            "| > CONTAINER |  |\n"
                + "| >> LAYOUT |  |\n"
                + "| >>> IMAGE |  |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertParagraphsAndSingleLineConfigurations() {
        List<String> input = List.of(
            "- CONTAINER",
            "-* bg:#fff",
            "-- LAYOUT",
            "--- IMAGE",
            "---* name:some-image.png");
        String output =
            "| > CONTAINER | bg:#fff |\n"
                + "| >> LAYOUT |  |\n"
                + "| >>> IMAGE | name:some-image.png |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertParagraphsAndMultilineConfigurations() {
        List<String> input = List.of(
            "- CONTAINER",
            "-- LAYOUT",
            "--- IMAGE",
            "---* name:some-image.png",
            "---* link:/some/path"
        );
        String output = "| > CONTAINER |  |\n"
            + "| >> LAYOUT |  |\n"
            + "| >>> IMAGE | name:some-image.png |\n"
            + "|  | link:/some/path |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertParagraphsAndMultipleConfigurationPropertiesInTheSameRow() {
        List<String> input = List.of(
            "- CONTAINER",
            "-- LAYOUT",
            "--- IMAGE",
            "---* name:some-image.png",
            "---* link:/some/path",
            "--- YOUTUBE_VIDEO",
            "---* title:\"Good title\", features:autoplay"
        );
        String output = "| > CONTAINER |  |\n"
            + "| >> LAYOUT |  |\n"
            + "| >>> IMAGE | name:some-image.png |\n"
            + "|  | link:/some/path |\n"
            + "| >>> YOUTUBE_VIDEO | title:\"Good title\", features:autoplay |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertModifiers() {
        List<String> input = List.of(
            "- CONTAINER",
            "-- LAYOUT",
            "--- IMAGE",
            "---@ PADDING_MODIFIER",
            "---* left:50px"
        );
        String output = "| > CONTAINER |  |\n"
            + "| >> LAYOUT |  |\n"
            + "| >>> IMAGE |  |\n"
            + "| >>>@ PADDING_MODIFIER | left:50px |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }


    @Test
    public void shouldConvertSingleInlineConfiguration() {
        List<String> input = List.of(
            "- CONTAINER",
            "-* bg:#fff",
            "-- LAYOUT",
            "--- IMAGE >> name:some-image.png");
        String output =
            "| > CONTAINER | bg:#fff |\n"
                + "| >> LAYOUT |  |\n"
                + "| >>> IMAGE | name:some-image.png |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }

    @Test
    public void shouldConvertMultipleInlineConfigurations() {
        List<String> input = List.of(
            "- CONTAINER",
            "-- LAYOUT",
            "--- IMAGE >> name:some-image.png, link:/some/path");
        String output = "| > CONTAINER |  |\n"
            + "| >> LAYOUT |  |\n"
            + "| >>> IMAGE | name:some-image.png, link:/some/path |";

        assertThat(converter.convert(input)).isEqualTo(output);
    }
}
