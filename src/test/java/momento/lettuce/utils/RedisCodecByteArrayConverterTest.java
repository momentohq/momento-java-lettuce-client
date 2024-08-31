package momento.lettuce.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Test;

class RedisCodecByteArrayConverterTest {

  @Test
  void encodeAndDecodeKeyToBase64String() {
    var converter = new RedisCodecBase64Converter(StringCodec.UTF8);
    var key = "key";
    var encodedKey = converter.encodeKeyToBase64String(key);
    var decodedKey = converter.decodeKeyFromBase64String(encodedKey);
    assertEquals(key, decodedKey);
  }

  @Test
  void encodeAndDecodeValueToBase64String() {
    var converter = new RedisCodecBase64Converter(StringCodec.UTF8);
    var value = "Hello, world!";
    var encodedValue = converter.encodeValueToBase64String(value);
    var decodedValue = converter.decodeValueFromBase64String(encodedValue);
    assertEquals(value, decodedValue);
  }
}
