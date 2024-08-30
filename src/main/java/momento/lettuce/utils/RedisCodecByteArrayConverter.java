package momento.lettuce.utils;

import io.lettuce.core.codec.RedisCodec;
import java.nio.ByteBuffer;

/**
 * Utility class for encoding and decoding keys and values using a {@link RedisCodec}, for
 * conversion between {@link ByteBuffer} and byte arrays for use with Momento.
 *
 * @param <K> the type of keys handled by this codec
 * @param <V> the type of values handled by this codec
 */
public class RedisCodecByteArrayConverter<K, V> {
  private final RedisCodec<K, V> codec;

  /**
   * Constructs a new {@code RedisCodecByteArrayConverter} with the specified {@code RedisCodec}.
   *
   * @param codec the RedisCodec used for encoding and decoding keys and values
   */
  public RedisCodecByteArrayConverter(RedisCodec<K, V> codec) {
    this.codec = codec;
  }

  /**
   * Encodes the given key to a byte array using the configured {@code RedisCodec}.
   *
   * @param key the key to encode
   * @return a byte array representing the encoded key
   */
  public byte[] encodeKeyToBytes(K key) {
    return convertByteBufferToArray(codec.encodeKey(key));
  }

  /**
   * Encodes the given key to a string using the configured {@code RedisCodec}.
   *
   * Since Momento collections expect a String-valued key, we have to convert the key to a String.
   * @param key the key to encode
   * @return a string representing the encoded key
   */
  public String encodeKeyToString(K key) {
    return key.toString();
  }

  /**
   * Decodes the given byte array into a key using the configured {@code RedisCodec}.
   *
   * @param bytes the byte array to decode
   * @return the decoded key
   */
  public K decodeKeyFromBytes(byte[] bytes) {
    return codec.decodeKey(ByteBuffer.wrap(bytes));
  }

  /**
   * Encodes the given value to a byte array using the configured {@code RedisCodec}.
   *
   * @param value the value to encode
   * @return a byte array representing the encoded value
   */
  public byte[] encodeValueToBytes(V value) {
    return convertByteBufferToArray(codec.encodeValue(value));
  }

  /**
   * Decodes the given byte array into a value using the configured {@code RedisCodec}.
   *
   * @param bytes the byte array to decode
   * @return the decoded value
   */
  public V decodeValueFromBytes(byte[] bytes) {
    return codec.decodeValue(ByteBuffer.wrap(bytes));
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
