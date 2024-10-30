package momento.lettuce.example.doc_examples;

import java.time.Duration;
import momento.lettuce.MomentoRedisReactiveClient;
import momento.lettuce.MomentoRedisReactiveCommands;
import momento.sdk.CacheClient;
import momento.sdk.auth.CredentialProvider;
import momento.sdk.config.Configurations;

class LimitedApiExample {
  public static void main(String[] args) {
    // Create a Momento cache client
    try (final CacheClient cacheClient =
        CacheClient.create(
            CredentialProvider.fromEnvVar("MOMENTO_API_KEY"),
            Configurations.Laptop.v1(),
            Duration.ofSeconds(60))) {
      final String cacheName = "cache";

      // This interface provides type safety as it only allows the user to interact with the
      // RedisReactiveCommands
      // commands that are supported by the MomentoRedisReactiveCommands class
      MomentoRedisReactiveCommands<String, String> redisClient =
          MomentoRedisReactiveClient.create(cacheClient, cacheName);
    }
  }
}
