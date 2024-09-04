package momento.lettuce.utils;

import io.lettuce.core.ExpireArgs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RangeUtilsTest {
  @Test
  public void testAdjustEndRangeFromInclusiveToExclusive() {
    assertEquals(3, RangeUtils.adjustEndRangeFromInclusiveToExclusive(2));
    assertEquals(null, RangeUtils.adjustEndRangeFromInclusiveToExclusive(-1));
    assertEquals(-1, RangeUtils.adjustEndRangeFromInclusiveToExclusive(-2));
  }
}
