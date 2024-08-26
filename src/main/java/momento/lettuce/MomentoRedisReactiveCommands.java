package momento.lettuce;

import io.lettuce.core.api.reactive.RedisReactiveCommands;
import reactor.core.publisher.Mono;

/**
 * The subset of {@link RedisReactiveCommands} that are also implemented by {@link
 * MomentoRedisReactiveCommands}.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public interface MomentoRedisReactiveCommands<K, V> {
  Mono<V> get(K k);

  Mono<String> set(K k, V v);

  Mono<Long> unlink(K... ks);
}
