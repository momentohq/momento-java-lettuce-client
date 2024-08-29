package momento.lettuce;

import io.lettuce.core.ExpireArgs;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import java.time.Duration;
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

  Mono<Boolean> pexpire(K k, long l);

  Mono<Boolean> pexpire(K k, long l, ExpireArgs expireArgs);

  Mono<Boolean> pexpire(K k, Duration duration);

  Mono<Boolean> pexpire(K k, Duration duration, ExpireArgs expireArgs);

  Mono<Long> unlink(K... ks);
}
