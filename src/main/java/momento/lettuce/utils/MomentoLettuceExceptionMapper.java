package momento.lettuce.utils;

import io.lettuce.core.RedisCommandExecutionException;
import io.lettuce.core.RedisCommandTimeoutException;
import io.lettuce.core.RedisException;
import momento.sdk.exceptions.SdkException;

/** Maps Momento SDK exceptions to Lettuce exceptions. */
public class MomentoLettuceExceptionMapper {
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
  public static RedisException unexpectedResponseException(String response) {
    return new RedisCommandExecutionException("Unexpected response from Momento: " + response);
  }

  public static UnsupportedOperationException commandNotImplementedException(String commandName) {
    return new UnsupportedOperationException("Command not implemented: " + commandName);
  }

  public static UnsupportedOperationException argumentNotSupportedException(
      String commandName, String argumentName) {
    return new UnsupportedOperationException(
        "Argument not supported for command " + commandName + ": " + argumentName);
  }
}
