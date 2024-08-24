package momento.lettuce;

import java.util.UUID;

public class TestUtils {
  public static String randomString() {
    return UUID.randomUUID().toString();
  }
}
