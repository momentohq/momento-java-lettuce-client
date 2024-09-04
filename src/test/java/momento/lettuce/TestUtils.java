package momento.lettuce;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TestUtils {
  public static String randomString() {
    return UUID.randomUUID().toString();
  }

  public static List<String> generateListOfRandomStrings(int size) {
    return IntStream.range(0, size).mapToObj(i -> randomString()).collect(Collectors.toList());
  }
}
