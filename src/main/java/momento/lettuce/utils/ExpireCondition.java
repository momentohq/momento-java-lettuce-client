package momento.lettuce.utils;

import io.lettuce.core.ExpireArgs;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandArgs;

/**
 * Represents the conditions under which an expiration operation can be performed.
 *
 * <p>Because {@link ExpireArgs} does not provide accessors but rather only serializes directly to
 * Redis protocol arguments, we provide this class to extract the conditions from the serialized
 * arguments.
 */
public class ExpireCondition {
  private final boolean requireExistingExpiry;
  private final boolean requiresNoExpiry;
  private final boolean requiresLessThan;
  private final boolean requiresGreaterThan;

  public ExpireCondition(
      boolean requiresExistingExpiry,
      boolean requiresNoExpiry,
      boolean requiresLessThan,
      boolean requiresGreaterThan) {
    this.requireExistingExpiry = requiresExistingExpiry;
    this.requiresNoExpiry = requiresNoExpiry;
    this.requiresLessThan = requiresLessThan;
    this.requiresGreaterThan = requiresGreaterThan;
  }

  /**
   * Returns whether the operation requires an existing expiry.
   *
   * @return Whether the operation requires an existing expiry.
   */
  public boolean requiresExistingExpiry() {
    return requireExistingExpiry;
  }

  /**
   * Returns whether the operation requires no expiry set.
   *
   * @return Whether the operation requires no expiry set.
   */
  public boolean requiresNoExpiry() {
    return requiresNoExpiry;
  }

  /**
   * Returns whether the operation requires the provided expiry to be less than the existing expiry.
   *
   * @return Whether the operation requires the provided expiry to be less than the existing expiry.
   */
  public boolean requiresLessThan() {
    return requiresLessThan;
  }

  /**
   * Returns whether the operation requires the provided expiry to be greater than the existing
   * expiry.
   *
   * @return Whether the operation requires the provided expiry to be greater than the existing
   *     expiry.
   */
  public boolean requiresGreaterThan() {
    return requiresGreaterThan;
  }

  /**
   * Extracts the conditions from the given {@link ExpireArgs}.
   *
   * @param expireArgs The {@link ExpireArgs} to extract conditions from.
   * @return The extracted conditions.
   */
  public static ExpireCondition fromExpireArgs(ExpireArgs expireArgs) {
    CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8);
    expireArgs.build(args);
    var commandString = args.toCommandString();
    var requiresExistingExpiry = commandString.contains("XX");
    var requiresNoExpiry = commandString.contains("NX");
    var requiresLessThan = commandString.contains("LT");
    var requiresGreaterThan = commandString.contains("GT");
    return new ExpireCondition(
        requiresExistingExpiry, requiresNoExpiry, requiresLessThan, requiresGreaterThan);
  }
}
