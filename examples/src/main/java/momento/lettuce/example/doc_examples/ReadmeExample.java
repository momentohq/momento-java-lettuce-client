package momento.lettuce.example.doc_examples;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.time.Duration;
import momento.lettuce.MomentoRedisReactiveClient;
import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configurations;

class ReadmeExample {
  public static void main(String[] args) {
    // Create a Momento cache client
    try (final CacheClient cacheClient =
        CacheClient.create(
            CredentialProvider.fromEnvVar("MOMENTO_API_KEY"),
            Configurations.Laptop.v1(),
            Duration.ofSeconds(60))) {
      final String cacheName = "cache";

      // Create a Redis client backed by the Momento cache client over the cache
      RedisReactiveCommands<String, String> redisClient =
          MomentoRedisReactiveClient.create(cacheClient, cacheName);

      // Perform operations vs Momento as if using a regular Redis client
      var setResult = redisClient.set("key", "value").block();
      System.out.println("Set result: " + setResult);

      var getResult = redisClient.get("key").block();
      System.out.println("Get result: " + getResult);
    }
  }
}
