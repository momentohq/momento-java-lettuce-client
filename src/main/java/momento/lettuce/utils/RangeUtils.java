package momento.lettuce.utils;

/** Provides utility methods for working with ranges. */
public class RangeUtils {
  /**
   * Adjusts the end range from inclusive to exclusive.
   *
   * <p>Since the Redis end offset is inclusive, we need to increment it by 1. That is, unless it
   * refers to "end of list" (-1), in which case we pass null to Momento.
   *
   * @param endRange the end range to adjust
   * @return the adjusted end range
   */
  public static Integer adjustEndRangeFromInclusiveToExclusive(int endRange) {
    if (endRange == -1) {
      return null;
    } else {
      return endRange + 1;
    }
  }

  /**
   * Adjusts the end range from inclusive to exclusive.
   *
   * @param endRange the end range to adjust. Should be in the range of an integer.
   * @return the adjusted end range
   */
  public static Integer adjustEndRangeFromInclusiveToExclusive(long endRange) {
    return adjustEndRangeFromInclusiveToExclusive((int) endRange);
  }
}
