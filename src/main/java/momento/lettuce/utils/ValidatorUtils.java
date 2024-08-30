package momento.lettuce.utils;

public class ValidatorUtils {
  public static void ensureInIntegerRange(long value, String argumentName) {
    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
      throw MomentoToLettuceExceptionMapper.createIntegerOutOfRangeException(argumentName, value);
    }
  }
}
