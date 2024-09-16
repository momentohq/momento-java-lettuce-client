package momento.lettuce.example;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.time.Duration;
import momento.lettuce.MomentoRedisReactiveClient;
import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configurations;
import momento.sdk.exceptions.MomentoErrorCode;
import momento.sdk.responses.cache.control.CacheCreateResponse;

class Basic {
  private static final String API_KEY_ENV_VAR = "MOMENTO_API_KEY";
  private static final Duration DEFAULT_TTL = Duration.ofSeconds(60);
  private static final String CACHE_NAME = "cache";

  public static void main(String[] args) {
    // Create a Momento cache client
    try (final CacheClient cacheClient = setUpMomentoClient()) {
      ensureCacheExists(cacheClient);

      // Create a Redis client backed by the Momento cache client over the cache
      RedisReactiveCommands<String, String> redisClient =
          MomentoRedisReactiveClient.create(cacheClient, CACHE_NAME);

      // Perform operations vs Momento as if using a regular Redis client
      var setResult = redisClient.set("key", "value").block();
      System.out.println("Set result: " + setResult);

      var getResult = redisClient.get("key").block();
      System.out.println("Get result: " + getResult);

      var unlinkResult = redisClient.unlink("key").block();
      System.out.println("Unlink result: " + unlinkResult);

      var getResultAfterUnlink = redisClient.get("key").block();
      System.out.println("Get result after unlink: " + getResultAfterUnlink);
    }
  }

  private static CacheClient setUpMomentoClient() {
    return CacheClient.create(
        CredentialProvider.fromEnvVar(API_KEY_ENV_VAR), Configurations.Laptop.v1(), DEFAULT_TTL);
  }

  private static void ensureCacheExists(CacheClient client) {
    var createCacheResponse = client.createCache(CACHE_NAME).join();

    if (createCacheResponse instanceof CacheCreateResponse.Success) {
      System.out.println("Cache created: " + CACHE_NAME);
    } else if (createCacheResponse instanceof CacheCreateResponse.Error error) {
      if (error.getErrorCode() == MomentoErrorCode.ALREADY_EXISTS_ERROR) {
        System.out.println("Cache already exists: " + CACHE_NAME);
      } else {
        System.out.println("Failed to create cache: " + CACHE_NAME);
      }
    } else {
      System.out.println("Unknown response type: " + createCacheResponse);
    }
  }
}
