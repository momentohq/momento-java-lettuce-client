package momento.lettuce;

import static momento.lettuce.TestUtils.generateListOfRandomStrings;
import static momento.lettuce.TestUtils.randomString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import momento.sdk.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

final class ListTest extends BaseTestClass {

  @Test
  public void testLPushHappyPath() {
    var key = randomString();
    var values = generateListOfRandomStrings(3);
    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(3, lPushResponse);
  }

  @Test
  public void testLRangeHappyPath() {
    var key = randomString();
    var values = generateListOfRandomStrings(5);

    // Reverse the list using Collections.reverse()
    List<String> valuesReversed = new ArrayList<>(values);
    Collections.reverse(valuesReversed);

    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(5, lPushResponse);

    // Fetch the whole list
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(5, lRangeResponse.size());

    // The values backwards should be the same as the original values due to the reduce semantics of
    // LPUSH
    assertEquals(valuesReversed, lRangeResponse);

    // Test positive offsets
    lRangeResponse = client.lrange(key, 2, 4).collectList().block();
    assertEquals(3, lRangeResponse.size());
    assertEquals(valuesReversed.subList(2, 5), lRangeResponse);

    // Test negative offsets
    lRangeResponse = client.lrange(key, -3, -1).collectList().block();
    assertEquals(3, lRangeResponse.size());
    assertEquals(valuesReversed.subList(2, 5), lRangeResponse);
  }

  @Test
  public void LRangeOffsetsNotInIntegerRangeTest() {
    // While Lettuce accepts longs for the offsets, Momento only supports integers.
    // Thus we should throw an exception if the offsets are out of integer range.
    if (isRedisTest()) {
      return;
    }

    // Test exception thrown when the offsets are out of integer range
    var key = randomString();
    var values = generateListOfRandomStrings(5);

    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(5, lPushResponse);

    long lessThanIntegerMin = (long) Integer.MIN_VALUE - 1;
    long moreThanIntegerMax = (long) Integer.MAX_VALUE + 1;
    assertThrows(
        InvalidArgumentException.class,
        () -> client.lrange(key, lessThanIntegerMin, 1).collectList().block());
    assertThrows(
        InvalidArgumentException.class,
        () -> client.lrange(key, 1, moreThanIntegerMax).collectList().block());
  }
}
