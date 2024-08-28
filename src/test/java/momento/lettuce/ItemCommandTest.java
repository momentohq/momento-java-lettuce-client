package momento.lettuce;

import static momento.lettuce.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

final class ItemCommandTest extends BaseTestClass {
  @Test
  public void testUnlink() {
    // Set 3 keys in the cache
    var key1 = randomString();
    var value1 = randomString();
    var setResponse1 = client.set(key1, value1).block();
    assertEquals("OK", setResponse1);

    var key2 = randomString();
    var value2 = randomString();
    var setResponse2 = client.set(key2, value2).block();
    assertEquals("OK", setResponse2);

    var key3 = randomString();
    var value3 = randomString();
    var setResponse3 = client.set(key3, value3).block();
    assertEquals("OK", setResponse3);

    // Generate a random key for good measure
    var key4 = randomString();

    // Go unlink 2 of them and one that isn't there
    var unlinkResponse = client.unlink(key1, key2, key4).block();

    // Since Redis tells you which keys were removed, we can get the exact number.
    if (isRedisTest()) {
      assertEquals(2, unlinkResponse);
    } else {
      assertEquals(3, unlinkResponse);
    }

    // Verify 2 are gone but key3 still there
    var storedValue1 = client.get(key1).block();
    assertEquals(null, storedValue1);

    var storedValue2 = client.get(key2).block();
    assertEquals(null, storedValue2);

    var storedValue3 = client.get(key3).block();
    assertEquals(value3, storedValue3);
  }
}