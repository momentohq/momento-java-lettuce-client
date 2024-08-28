package momento.lettuce;

import static momento.lettuce.TestUtils.randomString;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.time.Duration;
import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configurations;
import momento.sdk.exceptions.MomentoErrorCode;
import momento.sdk.responses.cache.control.CacheCreateResponse;
import momento.sdk.responses.cache.control.CacheDeleteResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

public class BaseTestClass {
  protected static RedisReactiveCommands<String, String> client;
  private static CacheClient momentoCacheClient = null;
  private static final String cacheName = "java-lettuce-integration-test-default-" + randomString();

  protected static boolean isRedisTest() {
    var redis = System.getenv("REDIS");
    return redis != null && (redis.equals("1") || redis.toLowerCase().equals("true"));
  }

  private static String getRedisHost() {
    return System.getenv("REDIS_HOST") != null ? System.getenv("REDIS_HOST") : "localhost";
  }

  private static String getRedisPort() {
    return System.getenv("REDIS_PORT") != null ? System.getenv("REDIS_PORT") : "6379";
  }

  private static String getRedisUri() {
    return String.format("redis://%s:%s/0", getRedisHost(), getRedisPort());
  }

  private static RedisReactiveCommands<String, String> buildRedisClient() {
    RedisClient redisClient = RedisClient.create(getRedisUri());
    return redisClient.connect().reactive();
  }

  protected static RedisReactiveCommands<String, String> buildMomentoClient(
      Duration clientTimeout) {
    momentoCacheClient =
        new CacheClient(
            CredentialProvider.fromEnvVar("MOMENTO_API_KEY"),
            Configurations.Laptop.latest().withTimeout(clientTimeout),
            Duration.ofMinutes(1));
    ensureCacheExists(momentoCacheClient, cacheName);
    return MomentoRedisReactiveClient.create(momentoCacheClient, cacheName);
  }

  protected static RedisReactiveCommands<String, String> buildMomentoClient() {
    return buildMomentoClient(Duration.ofSeconds(5));
  }

  private static void ensureCacheExists(CacheClient client, String cacheName) {
    var createCacheResponse = client.createCache(cacheName).join();
    if (createCacheResponse instanceof CacheCreateResponse.Success) {
      System.out.println("Cache created: " + cacheName);
    } else if (createCacheResponse instanceof CacheCreateResponse.Error error) {
      if (error.getErrorCode() == MomentoErrorCode.ALREADY_EXISTS_ERROR) {
        System.out.println("Cache already exists: " + cacheName);
      } else {
        throw new RuntimeException("Failed to create cache: " + error.toString());
      }
    } else {
      throw new RuntimeException("Unexpected response: " + createCacheResponse.toString());
    }
  }

  @BeforeAll
  static void beforeAll() {
    if (isRedisTest()) {
      System.out.println("Setting up client vs Redis");
      client = buildRedisClient();
    } else {
      System.out.println("Setting up client vs Momento");
      client = buildMomentoClient();
    }
  }

  private static void cleanupTestCache() {
    var deleteCacheResponse = momentoCacheClient.deleteCache(cacheName).join();
    if (deleteCacheResponse instanceof CacheDeleteResponse.Error error) {
      throw new RuntimeException("Failed to delete test cache: " + error.toString());
    }
  }

  @AfterAll
  static void afterAll() {
    if (!isRedisTest()) {
      cleanupTestCache();
      momentoCacheClient.close();
    }
  }
}
