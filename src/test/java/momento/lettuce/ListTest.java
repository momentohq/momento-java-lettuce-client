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
  private static List<String> reverseList(List<String> list) {
    List<String> reversedList = new ArrayList<>(list);
    Collections.reverse(reversedList);
    return reversedList;
  }

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
    var valuesReversed = reverseList(values);

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

  @Test
  public void testLPushMultipleTimes() {
    var key = randomString();
    var values = generateListOfRandomStrings(3);
    var valuesReversed = reverseList(values);
    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(3, lPushResponse);

    // Push a new list of values
    var newValues = generateListOfRandomStrings(3);
    var newValuesReversed = reverseList(newValues);
    lPushResponse = client.lpush(key, newValues.toArray(new String[0])).block();
    assertEquals(6, lPushResponse);

    // Verify the list is the concatenation of the two lists in reverse order
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(6, lRangeResponse.size());
    // should be newValuesReversed + valuesReversed; make a new list with this order
    var expectedValues = new ArrayList<>(newValuesReversed);
    expectedValues.addAll(valuesReversed);
    assertEquals(expectedValues, lRangeResponse);
  }

  @Test
  public void pExpireWorksOnListValues() {
    // Add a list to the cache
    var key = randomString();
    var values = generateListOfRandomStrings(3);
    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(3, lPushResponse);

    // Verify it's there with lrange
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(3, lRangeResponse.size());

    // Set the expiry so low it will expire before we can check it
    var pExpireResponse = client.pexpire(key, 1).block();
    assertEquals(true, pExpireResponse);

    // Wait for the key to expire
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // Verify it's gone
    lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(0, lRangeResponse.size());
  }

  @Test
  public void testLTrimShouldDeleteListOnEmptyRange() {
    var key = randomString();
    var values = generateListOfRandomStrings(10);

    // Positive offsets
    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(10, lPushResponse);

    // Trim on an empty range deletes the list
    var lTrimResponse = client.ltrim(key, 5, 4).block();
    assertEquals("OK", lTrimResponse);

    // Verify the list is empty
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(0, lRangeResponse.size());

    // Negative offsets
    lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(10, lPushResponse);

    // Trim on an empty range deletes the list
    lTrimResponse = client.ltrim(key, -4, -5).block();
    assertEquals("OK", lTrimResponse);

    // Verify the list is empty
    lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(0, lRangeResponse.size());
  }

  @Test
  public void testLTrimShouldTrimList() {
    var key = randomString();
    var values = generateListOfRandomStrings(10);
    var valuesReversed = reverseList(values);

    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(10, lPushResponse);

    var lTrimResponse = client.ltrim(key, 2, 4).block();
    assertEquals("OK", lTrimResponse);

    // Verify the list indices 2 to 4 inclusive
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(3, lRangeResponse.size());
    assertEquals(valuesReversed.subList(2, 5), lRangeResponse);
  }

  @Test
  public void testLTrimShouldWorkWithEndOfList() {
    var key = randomString();
    var values = generateListOfRandomStrings(10);
    var valuesReversed = reverseList(values);

    var lPushResponse = client.lpush(key, values.toArray(new String[0])).block();
    assertEquals(10, lPushResponse);

    var lTrimResponse = client.ltrim(key, 2, -1).block();
    assertEquals("OK", lTrimResponse);

    // Verify the list indices 2 to the end
    var lRangeResponse = client.lrange(key, 0, -1).collectList().block();
    assertEquals(8, lRangeResponse.size());
    assertEquals(valuesReversed.subList(2, 10), lRangeResponse);
  }
}
