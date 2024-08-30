package momento.lettuce.utils;

import static org.junit.jupiter.api.Assertions.*;

import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Test;

class RedisCodecByteArrayConverterTest {

  @Test
  void encodeAndDecodeKeyToBytes() {
    var converter = new RedisCodecByteArrayConverter(StringCodec.UTF8);
    var key = "key";
    var encodedKey = converter.encodeKeyToBytes(key);
    var decodedKey = converter.decodeKeyFromBytes(encodedKey);
    assertEquals(key, decodedKey);
  }

  @Test
  void encodeAndDecodeValueToBytes() {
    var converter = new RedisCodecByteArrayConverter(StringCodec.UTF8);
    var value = "Hello, world!";
    var encodedValue = converter.encodeValueToBytes(value);
    var decodedValue = converter.decodeValueFromBytes(encodedValue);
    assertEquals(value, decodedValue);
  }

  @Test
  void encodeKeyToString() {
    var converter = new RedisCodecByteArrayConverter(StringCodec.UTF8);
    var key = "key";
    var encodedKey = converter.encodeKeyToString(key);
    assertEquals(key, encodedKey);
  }
}
