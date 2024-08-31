package momento.lettuce.utils;

import io.lettuce.core.codec.RedisCodec;
import java.nio.ByteBuffer;
import java.util.Base64;

/**
 * Utility class for encoding and decoding keys and values using a {@link RedisCodec}, converting
 * between {@link ByteBuffer} and Base64-encoded strings.
 *
 * @param <K> the type of keys handled by this codec
 * @param <V> the type of values handled by this codec
 */
public class RedisCodecBase64Converter<K, V> {
  private final RedisCodec<K, V> redisCodec;
  private static final Base64.Encoder base64Encoder = Base64.getEncoder();
  private static final Base64.Decoder base64Decoder = Base64.getDecoder();

  /**
   * Constructs a new {@code RedisCodecBase64Converter} with the specified {@code RedisCodec}.
   *
   * @param redisCodec the RedisCodec used for encoding and decoding keys and values
   */
  public RedisCodecBase64Converter(RedisCodec<K, V> redisCodec) {
    this.redisCodec = redisCodec;
  }

  /**
   * Encodes the given key to a Base64-encoded string using the configured {@code RedisCodec}.
   *
   * @param key the key to encode
   * @return a Base64-encoded string representing the encoded key
   */
  public String encodeKeyToBase64String(K key) {
    byte[] bytes = convertByteBufferToArray(redisCodec.encodeKey(key));
    return base64Encoder.encodeToString(bytes);
  }

  /**
   * Decodes the given Base64-encoded string into a key using the configured {@code RedisCodec}.
   *
   * @param base64String the Base64-encoded string to decode
   * @return the decoded key
   */
  public K decodeKeyFromBase64String(String base64String) {
    byte[] bytes = base64Decoder.decode(base64String);
    return redisCodec.decodeKey(ByteBuffer.wrap(bytes));
  }

  /**
   * Encodes the given value to a Base64-encoded string using the configured {@code RedisCodec}.
   *
   * @param value the value to encode
   * @return a Base64-encoded string representing the encoded value
   */
  public String encodeValueToBase64String(V value) {
    byte[] bytes = convertByteBufferToArray(redisCodec.encodeValue(value));
    return base64Encoder.encodeToString(bytes);
  }

  /**
   * Decodes the given Base64-encoded string into a value using the configured {@code RedisCodec}.
   *
   * @param base64String the Base64-encoded string to decode
   * @return the decoded value
   */
  public V decodeValueFromBase64String(String base64String) {
    byte[] bytes = base64Decoder.decode(base64String);
    return redisCodec.decodeValue(ByteBuffer.wrap(bytes));
  }

  /**
   * Converts a {@link ByteBuffer} to a byte array, containing only the remaining bytes.
   *
   * @param buffer the ByteBuffer to convert
   * @return a byte array containing the data from the ByteBuffer
   */
  private static byte[] convertByteBufferToArray(ByteBuffer buffer) {
    byte[] bytes = new byte[buffer.remaining()];
    buffer.get(bytes);
    return bytes;
  }
}
