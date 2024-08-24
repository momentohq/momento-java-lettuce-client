package momento.lettuce;

import static momento.lettuce.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class ScalarCommandTest extends BaseTestClass {
  @Test
  public void testGetMiss() {
    var key = randomString();
    var value = client.get(key).block();
    assertEquals(null, value);
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
