package io.picimako.drupal.context.util;

/**
 * Utility methods for working with String objects.
 */
public final class StringUtils {

    private StringUtils() {
        //Utility class
    }

    /**
     * Checks whether the argument text is null or blank.
     *
     * @param text the text to validate
     * @return true if the text is null or blank, false otherwise
     */
    public static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
