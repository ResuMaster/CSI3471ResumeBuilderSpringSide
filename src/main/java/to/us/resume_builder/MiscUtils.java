package to.us.resume_builder;

import java.util.Random;

/**
 * This class contains miscellaneous utility functions used throughout the
 * program.
 */
public final class MiscUtils {
    /**
     * Get a random alphanumeric string of the specified length.
     *
     * @param length The length of the string to generate.
     *
     * @return The desired random string.
     */
    public static String randomAlphanumericString(int length) {
        int leftLimit = 48;  // Numeral '0'
        int rightLimit = 122; // Letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))  // filter out non-alphanumerics
            .limit(length)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }
}
