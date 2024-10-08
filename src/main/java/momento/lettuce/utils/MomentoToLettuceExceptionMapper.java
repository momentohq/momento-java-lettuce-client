package momento.lettuce.utils;

import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import momento.sdk.exceptions.InvalidArgumentException;
import momento.sdk.exceptions.SdkException;

/** Maps Momento SDK exceptions to Lettuce exceptions. */
public class MomentoToLettuceExceptionMapper {
  /**
   * Maps a Momento SDK exception to a Lettuce exception.
   *
   * @param exception The Momento SDK exception.
   * @return The Lettuce exception.
   */
  public static RedisException mapException(SdkException exception) {
    switch (exception.getErrorCode()) {
        // The only specific exception in the Lettuce hierarchy we can map to is
        // RedisCommandTimeoutException.
      case TIMEOUT_ERROR -> {
        return new RedisCommandTimeoutException(exception.getCause());
      }
      default -> {
        return new RedisCommandExecutionException(exception.getMessage(), exception.getCause());
      }
    }
  }

  /**
   * Creates a Lettuce exception in the event an unexpected response alternative comes back from
   * Momento.
   *
   * @param response The response from Momento.
   * @return The Lettuce exception.
   */
  public static RedisException createUnexpectedResponseException(String response) {
    return new RedisCommandExecutionException("Unexpected response from Momento: " + response);
  }

  /**
   * Creates a Lettuce exception in the event a command is not implemented in Momento.
   *
   * @param commandName The name of the command.
   * @return The Lettuce exception.
   */
  public static UnsupportedOperationException createCommandNotImplementedException(
      String commandName) {
    return new UnsupportedOperationException("Command not implemented: " + commandName);
  }

  /**
   * Creates a Lettuce exception in the event an argument is not supported for a command.
   *
   * @param commandName The name of the command.
   * @param argumentName The name of the argument that is not supported.
   * @return The Lettuce exception.
   */
  public static UnsupportedOperationException createArgumentNotSupportedException(
      String commandName, String argumentName) {
    return new UnsupportedOperationException(
        "Argument not supported for command " + commandName + ": " + argumentName);
  }

  /**
   * Creates a Lettuce exception in the event an argument is out of range.
   *
   * @param argumentName The name of the parameter.
   * @param value The value that was out of range.
   * @return The Lettuce exception.
   */
  public static InvalidArgumentException createIntegerOutOfRangeException(
      String argumentName, long value) {
    return new InvalidArgumentException(
        "Argument out of range: "
            + argumentName
            + " must be between "
            + Integer.MIN_VALUE
            + " and "
            + Integer.MAX_VALUE
            + ", but was "
            + value);
  }
}
