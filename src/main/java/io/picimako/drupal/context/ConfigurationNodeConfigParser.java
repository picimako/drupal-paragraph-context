package io.picimako.drupal.context;

import com.google.common.base.Splitter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Parses a configuration value (consisting of key-value pairs) coming from a {@link ConfigurationNode}
 * and collects them in a String/String map.
 */
public class ConfigurationNodeConfigParser {

    private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("^\"(?<value>.*)\"$");
    private final Splitter keyValuePairSplitter = Splitter.onPattern("(?<!\\\\),");
    private final Splitter keyValueSplitter = Splitter.on(":").limit(2);

    /**
     * Parses the argument configuration value (consisting of key-value pairs) and collects them in a String/String map.
     * <p>
     * Leading and trailing whitespaces are trimmed, so it makes the pattern of the configuration values more relaxed.
     * <p>
     * The passed in value is expected in the following format: {@code key:value, otherkey:othervalue} or in certain
     * cases the values might need to be enclosed by quotation marks like {@code key:"value", otherkey:"othervalue"},
     * that is also possible.
     *
     * @param configuration the configuration key-value pairs
     * @return the configuration values collected into a map
     * @throws IllegalArgumentException when the argument configuration is blank
     */
    public Map<String, String> parseConfigurationValues(String configuration) {
        checkArgument(!isBlank(configuration), "The configuration value should not be blank.");
        return splitToKeysAndValues(splitToKeyValuePairs(configuration));
    }

    /**
     * Splits the input configuration node value to key-value pairs.
     * <p>
     * The separation happens at commas that are not escaped, then it removes all escaping from those
     * commas, so that the input value will not contain them.
     * <p>
     * Example:
     * <p>
     * The value
     * <pre>
     * url:https://duckduckgo.com?param=value, image:someimage
     * </pre>
     * is split into the following list items:
     * <pre>
     * [url:https://duckduckgo.com?param=value] and [image:someimage]
     * </pre>
     *
     * @param configuration the configuration value of a configuration node
     * @return the list of key-value pairs
     * @throws IllegalArgumentException when at least one key-value pair doesn't contain a key-value separator
     */
    private List<String> splitToKeyValuePairs(String configuration) {
        List<String> keyValuePairs = keyValuePairSplitter
            .splitToList(configuration)
            .stream()
            .map(kv -> kv.replaceAll("\\\\,", ","))
            .collect(toList());

        checkArgument(isAllKeyValuePairHaveKeyValueDelimiter(keyValuePairs),
            "There is at least one configuration entry that doesn't have a key or a value part.");
        return keyValuePairs;
    }

    private boolean isAllKeyValuePairHaveKeyValueDelimiter(List<String> keyValuePairs) {
        return keyValuePairs.stream().allMatch(kvp -> kvp.contains(":"));
    }

    /**
     * Splits the input key-value pairs into keys and values, and collects them in a Map.
     * <p>
     * The keys and values are separated alongside colons ({@code :}).
     * <p>
     * Example:
     * <p>
     * The values
     * <pre>
     * [url:https://duckduckgo.com?param=value] and [image:someimage]
     * </pre>
     * are split into the following Map entries:
     * <pre>
     * url -> https://duckduckgo.com?param=value
     * image -> someimage
     * </pre>
     *
     * @param keyValuePairs the key-value pairs to split further
     * @return the map of configuration key and value entries
     */
    private Map<String, String> splitToKeysAndValues(List<String> keyValuePairs) {
        return keyValuePairs.stream()
            .map(kvp -> StringUtils.stripStart(kvp, null))
            .collect(toMap(
                kv -> keyValueSplitter.splitToList(kv).get(0),
                this::parseValue));
    }

    /**
     * Retrieves the value from the argument key-value pair. If the value is also enclosed in quotation marks,
     * it gets the value from between them.
     *
     * @param kv a configuration key-value pair
     * @return the actual configuration value
     */
    private String parseValue(String kv) {
        String value = keyValueSplitter.splitToList(kv).get(1);
        Matcher quotedValueMatcher = QUOTED_VALUE_PATTERN.matcher(value);
        return quotedValueMatcher.matches() ? quotedValueMatcher.group("value") : value;
    }
}
