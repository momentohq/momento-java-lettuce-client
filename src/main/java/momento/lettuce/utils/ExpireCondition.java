package momento.lettuce.utils;

import io.lettuce.core.ExpireArgs;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.protocol.CommandArgs;

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

  public boolean requiresExistingExpiry() {
    return requireExistingExpiry;
  }

  public boolean requiresNoExpiry() {
    return requiresNoExpiry;
  }

  public boolean requiresLessThan() {
    return requiresLessThan;
  }

  public boolean requiresGreaterThan() {
    return requiresGreaterThan;
  }

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
