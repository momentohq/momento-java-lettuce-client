package momento.lettuce;

import static momento.lettuce.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.lettuce.core.RedisCommandTimeoutException;
import java.time.Duration;
import org.junit.jupiter.api.Test;

final class ScalarCommandTest extends BaseTestClass {
  @Test
  public void testGetMiss() {
    var key = randomString();
    var value = client.get(key).block();
    assertEquals(null, value);
  }

  @Test
  public void testMomentoTimeout() {
    if (isRedisTest()) {
      return;
    }

    var clientWithShortDeadline = buildMomentoClient(Duration.ofMillis(1));
    var key = randomString();
    assertThrows(
        RedisCommandTimeoutException.class, () -> clientWithShortDeadline.get(key).block());
  }

  @Test
  public void testSetAndGet() {
    var key = randomString();
    var value = randomString();
    var setResponse = client.set(key, value).block();
    assertEquals("OK", setResponse);

    var storedValue = client.get(key).block();
    assertEquals(storedValue, value);
  }
}
