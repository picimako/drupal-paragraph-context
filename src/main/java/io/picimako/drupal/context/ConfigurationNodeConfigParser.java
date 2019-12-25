package io.picimako.drupal.context;

import com.google.common.base.Splitter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static io.picimako.drupal.context.util.StringUtils.isBlank;
import static java.util.stream.Collectors.toList;

/**
 * Parses a configuration value (consisting of key-value pairs) coming from a {@link ConfigurationNode}
 * and collects them in a String/String map.
 */
public class ConfigurationNodeConfigParser {

    public static final String CONFIG_ITEM_DELIMITER = ",";
    public static final String CONFIG_KEY_VALUE_DELIMITER = ":";
    private static final String ESCAPED_CONFIG_ITEM_DELIMITER_PATTERN = "\\\\" + CONFIG_ITEM_DELIMITER;
    private static final Pattern QUOTED_VALUE_PATTERN = Pattern.compile("^\"(?<value>.*)\"$");
    private final Splitter keyValuePairSplitter = Splitter.onPattern("(?<!\\\\)" + CONFIG_ITEM_DELIMITER);
    private final Splitter keyValueSplitter = Splitter.on(CONFIG_KEY_VALUE_DELIMITER).limit(2);

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
            .map(kv -> kv.replaceAll(ESCAPED_CONFIG_ITEM_DELIMITER_PATTERN, CONFIG_ITEM_DELIMITER))
            .collect(toList());

        checkArgument(isAllKeyValuePairHaveKeyValueDelimiter(keyValuePairs),
            "There is at least one configuration entry that doesn't have a key or a value part.");
        return keyValuePairs;
    }

    private boolean isAllKeyValuePairHaveKeyValueDelimiter(List<String> keyValuePairs) {
        return keyValuePairs.stream().allMatch(kvp -> kvp.contains(CONFIG_KEY_VALUE_DELIMITER));
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
        Map<String, String> split = new LinkedHashMap<>();
        keyValuePairs.forEach(kvp -> {
            List<String> splitList = keyValueSplitter.splitToList(kvp.stripLeading());
            split.put(splitList.get(0), parseValue(splitList.get(1)));
        });
        return split;
    }

    /**
     * Retrieves the actual value from the argument configuration value. If the value is also enclosed in
     * quotation marks, it gets the value from between them.
     *
     * @param value a configuration value
     * @return the actual configuration value
     */
    private String parseValue(String value) {
        Matcher quotedValueMatcher = QUOTED_VALUE_PATTERN.matcher(value);
        return quotedValueMatcher.matches() ? quotedValueMatcher.group("value") : value;
    }
}
