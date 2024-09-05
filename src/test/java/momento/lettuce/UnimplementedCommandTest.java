package momento.lettuce;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

final class UnimplementedCommandTest extends BaseTestClass {
  @Test
  public void testZunion() {
    if (isRedisTest()) {
      return;
    }

    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          client.zunion("key1", "key2");
        });
  }
}
