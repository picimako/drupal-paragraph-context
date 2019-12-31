package io.picimako.drupal.context.util;


/**
 * Utility class for validating objects.
 */
public final class Preconditions {

    private Preconditions() {
        //Util class
    }

    /**
     * Checks whether the condition given is true.
     * If it evaluates to false then it throws an exception with the provided error message.
     *
     * @param condition    the condition to evaluate
     * @param errorMessage the error message
     */
    public static void check(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
