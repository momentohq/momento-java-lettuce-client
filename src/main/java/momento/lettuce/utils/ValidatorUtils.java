package momento.lettuce.utils;

/** Provides utility methods for validating arguments. */
public class ValidatorUtils {
  /**
   * Ensures that the provided value is within the range of an integer.
   *
   * @param value The value to check.
   * @param argumentName The name of the argument.
   */
  public static void ensureInIntegerRange(long value, String argumentName) {
    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
      throw MomentoToLettuceExceptionMapper.createIntegerOutOfRangeException(argumentName, value);
    }
  }
}
