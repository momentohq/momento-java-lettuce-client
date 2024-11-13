package momento.lettuce;

import io.lettuce.core.AclCategory;
import io.lettuce.core.AclSetuserArgs;
import io.lettuce.core.BitFieldArgs;
import io.lettuce.core.ClientListArgs;
import io.lettuce.core.Consumer;
import io.lettuce.core.CopyArgs;
import io.lettuce.core.ExpireArgs;
import io.lettuce.core.FlushMode;
import io.lettuce.core.FunctionRestoreMode;
import io.lettuce.core.GeoAddArgs;
import io.lettuce.core.GeoArgs;
import io.lettuce.core.GeoCoordinates;
import io.lettuce.core.GeoRadiusStoreArgs;
import io.lettuce.core.GeoSearch;
import io.lettuce.core.GeoValue;
import io.lettuce.core.GeoWithin;
import io.lettuce.core.GetExArgs;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.KeyValue;
import io.lettuce.core.KillArgs;
import io.lettuce.core.LMPopArgs;
import io.lettuce.core.LMoveArgs;
import io.lettuce.core.LPosArgs;
import io.lettuce.core.Limit;
import io.lettuce.core.MapScanCursor;
import io.lettuce.core.MigrateArgs;
import io.lettuce.core.Range;
import io.lettuce.core.RestoreArgs;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.ScoredValue;
import io.lettuce.core.ScoredValueScanCursor;
import io.lettuce.core.ScriptOutputType;
import io.lettuce.core.SetArgs;
import io.lettuce.core.ShutdownArgs;
import io.lettuce.core.SortArgs;
import io.lettuce.core.StrAlgoArgs;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.StreamScanCursor;
import io.lettuce.core.StringMatchResult;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.TransactionResult;
import io.lettuce.core.UnblockType;
import io.lettuce.core.Value;
import io.lettuce.core.ValueScanCursor;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XAutoClaimArgs;
import io.lettuce.core.XClaimArgs;
import io.lettuce.core.XGroupCreateArgs;
import io.lettuce.core.XPendingArgs;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XTrimArgs;
import io.lettuce.core.ZAddArgs;
import io.lettuce.core.ZAggregateArgs;
import io.lettuce.core.ZPopArgs;
import io.lettuce.core.ZStoreArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.models.stream.ClaimedMessages;
import io.lettuce.core.models.stream.PendingMessage;
import io.lettuce.core.models.stream.PendingMessages;
import io.lettuce.core.output.CommandOutput;
import io.lettuce.core.output.KeyStreamingChannel;
import io.lettuce.core.output.KeyValueStreamingChannel;
import io.lettuce.core.output.ScoredValueStreamingChannel;
import io.lettuce.core.output.ValueStreamingChannel;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandType;
import io.lettuce.core.protocol.ProtocolKeyword;
import io.lettuce.core.protocol.RedisCommand;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.ImmediateEventExecutor;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import momento.lettuce.utils.ExpireCondition;
import momento.lettuce.utils.MomentoToLettuceExceptionMapper;
import momento.lettuce.utils.RangeUtils;
import momento.lettuce.utils.RedisCodecByteArrayConverter;
import momento.lettuce.utils.RedisResponse;
import momento.lettuce.utils.ValidatorUtils;
import momento.sdk.CacheClient;
import momento.sdk.responses.cache.DeleteResponse;
import momento.sdk.responses.cache.GetResponse;
import momento.sdk.responses.cache.SetResponse;
import momento.sdk.responses.cache.list.ListConcatenateFrontResponse;
import momento.sdk.responses.cache.list.ListFetchResponse;
import momento.sdk.responses.cache.list.ListRetainResponse;
import momento.sdk.responses.cache.ttl.UpdateTtlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * A Redis client that wraps a {@link CacheClient} and provides reactive commands.
 *
 * Supported commands are proxied to the {@link CacheClient} and the cache name provided at construction.
 *
 * @param <K> Key type.
 * @param <V> Value type.
 */
public class MomentoRedisReactiveClient<K, V>
    implements RedisReactiveCommands<K, V>, MomentoRedisReactiveCommands<K, V> {
  private final CacheClient client;
  private final String cacheName;
  private final RedisCodecByteArrayConverter<K, V> codec;

  /**
   * Creates a new {@link MomentoRedisReactiveClient}.
   *
   * @param client The cache client.
   * @param cacheName The name of the cache to store data in.
   * @param codec The codec to use for serializing and deserializing keys and values.
   */
  public MomentoRedisReactiveClient(CacheClient client, String cacheName, RedisCodec<K, V> codec) {
    this.client = client;
    this.cacheName = cacheName;
    this.codec = new RedisCodecByteArrayConverter<>(codec);
  }

  /**
   * Instantiates a new {@link MomentoRedisReactiveClient} with the provided {@link CacheClient} and
   * cache name.
   *
   * @param client The cache client.
   * @param cacheName The name of the cache to store data in.
   * @param codec The codec to use for serializing and deserializing keys and values.
   * @return A new {@link MomentoRedisReactiveClient}.
   * @param <K> Key type.
   * @param <V> Value type.
   */
  public static <K, V> MomentoRedisReactiveClient<K, V> create(
      CacheClient client, String cacheName, RedisCodec<K, V> codec) {
    return new MomentoRedisReactiveClient<>(client, cacheName, codec);
  }

  /**
   * Instantiates a new {@link MomentoRedisReactiveClient} with the provided {@link CacheClient} and
   * cache name.
   *
   * @param client The cache client.
   * @param cacheName The name of the cache to store data in.
   * @return A new {@link MomentoRedisReactiveClient}.
   */
  public static MomentoRedisReactiveClient<String, String> create(
      CacheClient client, String cacheName) {
    return new MomentoRedisReactiveClient<>(client, cacheName, StringCodec.UTF8);
  }

  @Override
  public void setTimeout(Duration duration) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setTimeout");
  }

  @Override
  public Mono<String> asking() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("asking");
  }

  @Override
  public Mono<String> auth(CharSequence charSequence) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("auth");
  }

  @Override
  public Mono<String> auth(String s, CharSequence charSequence) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("auth");
  }

  @Override
  public Mono<String> clusterAddSlots(int... ints) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterAddSlots");
  }

  @Override
  public Mono<String> clusterBumpepoch() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterBumpepoch");
  }

  @Override
  public Mono<Long> clusterCountFailureReports(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterCountFailureReports");
  }

  @Override
  public Mono<Long> clusterCountKeysInSlot(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterCountKeysInSlot");
  }

  @Override
  public Mono<String> clusterAddSlotsRange(Range<Integer>... ranges) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterAddSlotsRange");
  }

  @Override
  public Mono<String> clusterDelSlots(int... ints) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterDelSlots");
  }

  @Override
  public Mono<String> clusterDelSlotsRange(Range<Integer>... ranges) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterDelSlotsRange");
  }

  @Override
  public Mono<String> clusterFailover(boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterFailover");
  }

  @Override
  public Mono<String> clusterFailover(boolean b, boolean b1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterFailover");
  }

  @Override
  public Mono<String> clusterFlushslots() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterFlushslots");
  }

  @Override
  public Mono<String> clusterForget(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterForget");
  }

  @Override
  public Flux<K> clusterGetKeysInSlot(int i, int i1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterGetKeysInSlot");
  }

  @Override
  public Mono<String> clusterInfo() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterInfo");
  }

  @Override
  public Mono<Long> clusterKeyslot(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterKeyslot");
  }

  @Override
  public Mono<String> clusterMeet(String s, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterMeet");
  }

  @Override
  public Mono<String> clusterMyId() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterMyId");
  }

  @Override
  public Mono<String> clusterNodes() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterNodes");
  }

  @Override
  public Mono<String> clusterReplicate(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterReplicate");
  }

  @Override
  public Flux<String> clusterReplicas(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterReplicas");
  }

  @Override
  public Mono<String> clusterReset(boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterReset");
  }

  @Override
  public Mono<String> clusterSaveconfig() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterSaveconfig");
  }

  @Override
  public Mono<String> clusterSetConfigEpoch(long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterSetConfigEpoch");
  }

  @Override
  public Mono<String> clusterSetSlotImporting(int i, String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterSetSlotImporting");
  }

  @Override
  public Mono<String> clusterSetSlotMigrating(int i, String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterSetSlotMigrating");
  }

  @Override
  public Mono<String> clusterSetSlotNode(int i, String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterSetSlotNode");
  }

  @Override
  public Mono<String> clusterSetSlotStable(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "clusterSetSlotStable");
  }

  @Override
  public Mono<List<Object>> clusterShards() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterShards");
  }

  @Override
  public Flux<String> clusterSlaves(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterSlaves");
  }

  @Override
  public Flux<Object> clusterSlots() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clusterSlots");
  }

  @Override
  public Mono<String> select(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("select");
  }

  @Override
  public Mono<String> swapdb(int i, int i1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("swapdb");
  }

  @Override
  public StatefulRedisConnection<K, V> getStatefulConnection() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "getStatefulConnection");
  }

  @Override
  public Mono<Long> publish(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("publish");
  }

  @Override
  public Flux<K> pubsubChannels() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pubsubChannels");
  }

  @Override
  public Flux<K> pubsubChannels(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pubsubChannels");
  }

  @Override
  public Mono<Map<K, Long>> pubsubNumsub(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pubsubNumsub");
  }

  @Override
  public Flux<K> pubsubShardChannels() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "pubsubShardChannels");
  }

  @Override
  public Flux<K> pubsubShardChannels(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "pubsubShardChannels");
  }

  @Override
  public Mono<Map<K, Long>> pubsubShardNumsub(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("Long");
  }

  @Override
  public Mono<Long> pubsubNumpat() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pubsubNumpat");
  }

  @Override
  public Mono<Long> spublish(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("spublish");
  }

  @Override
  public Mono<V> echo(V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("echo");
  }

  @Override
  public Flux<Object> role() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("role");
  }

  @Override
  public Mono<String> ping() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("ping");
  }

  @Override
  public Mono<String> readOnly() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("readOnly");
  }

  @Override
  public Mono<String> readWrite() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("readWrite");
  }

  @Override
  public Mono<String> quit() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("quit");
  }

  @Override
  public Mono<Long> waitForReplication(int i, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "waitForReplication");
  }

  @Override
  public <T> Flux<T> dispatch(
      ProtocolKeyword protocolKeyword, CommandOutput<K, V, ?> commandOutput) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("dispatch");
  }

  @Override
  public <T> Flux<T> dispatch(
      ProtocolKeyword protocolKeyword,
      CommandOutput<K, V, ?> commandOutput,
      CommandArgs<K, V> commandArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("dispatch");
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void reset() {}

  @Override
  public void setAutoFlushCommands(boolean b) {}

  @Override
  public void flushCommands() {}

  @Override
  public Mono<Set<AclCategory>> aclCat() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclCat");
  }

  @Override
  public Mono<Set<CommandType>> aclCat(AclCategory aclCategory) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclCat");
  }

  @Override
  public Mono<Long> aclDeluser(String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclDeluser");
  }

  @Override
  public Mono<String> aclDryRun(String s, String s1, String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclDryRun");
  }

  @Override
  public Mono<String> aclDryRun(String s, RedisCommand<K, V, ?> redisCommand) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclDryRun");
  }

  @Override
  public Mono<String> aclGenpass() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclGenpass");
  }

  @Override
  public Mono<String> aclGenpass(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclGenpass");
  }

  @Override
  public Mono<List<Object>> aclGetuser(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclGetuser");
  }

  @Override
  public Flux<String> aclList() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclList");
  }

  @Override
  public Mono<String> aclLoad() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclLoad");
  }

  @Override
  public Flux<Map<String, Object>> aclLog() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclLog");
  }

  @Override
  public Flux<Map<String, Object>> aclLog(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclLog");
  }

  @Override
  public Mono<String> aclLogReset() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclLogReset");
  }

  @Override
  public Mono<String> aclSave() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclSave");
  }

  @Override
  public Mono<String> aclSetuser(String s, AclSetuserArgs aclSetuserArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclSetuser");
  }

  @Override
  public Flux<String> aclUsers() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclUsers");
  }

  @Override
  public Mono<String> aclWhoami() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("aclWhoami");
  }

  @Override
  public <T> Flux<T> fcall(String s, ScriptOutputType scriptOutputType, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("fcall");
  }

  @Override
  public <T> Flux<T> fcall(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("fcall");
  }

  @Override
  public <T> Flux<T> fcallReadOnly(String s, ScriptOutputType scriptOutputType, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("fcallReadOnly");
  }

  @Override
  public <T> Flux<T> fcallReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("fcallReadOnly");
  }

  @Override
  public Mono<String> functionLoad(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionLoad");
  }

  @Override
  public Mono<String> functionLoad(String s, boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionLoad");
  }

  @Override
  public Mono<byte[]> functionDump() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionDump");
  }

  @Override
  public Mono<String> functionRestore(byte[] bytes) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionRestore");
  }

  @Override
  public Mono<String> functionRestore(byte[] bytes, FunctionRestoreMode functionRestoreMode) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionRestore");
  }

  @Override
  public Mono<String> functionFlush(FlushMode flushMode) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionFlush");
  }

  @Override
  public Mono<String> functionKill() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionKill");
  }

  @Override
  public Flux<Map<String, Object>> functionList() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionList");
  }

  @Override
  public Flux<Map<String, Object>> functionList(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("functionList");
  }

  @Override
  public Mono<Long> geoadd(K k, double v, double v1, V v2) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Long> geoadd(K k, double v, double v1, V v2, GeoAddArgs geoAddArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Long> geoadd(K k, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Long> geoadd(K k, GeoValue<V>... geoValues) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Long> geoadd(K k, GeoAddArgs geoAddArgs, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Long> geoadd(K k, GeoAddArgs geoAddArgs, GeoValue<V>... geoValues) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geoadd");
  }

  @Override
  public Mono<Double> geodist(K k, V v, V v1, GeoArgs.Unit unit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geodist");
  }

  @Override
  public Flux<Value<String>> geohash(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geohash");
  }

  @Override
  public Flux<Value<GeoCoordinates>> geopos(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geopos");
  }

  @Override
  public Flux<V> georadius(K k, double v, double v1, double v2, GeoArgs.Unit unit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadius");
  }

  @Override
  public Flux<GeoWithin<V>> georadius(
      K k, double v, double v1, double v2, GeoArgs.Unit unit, GeoArgs geoArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadius");
  }

  @Override
  public Mono<Long> georadius(
      K k,
      double v,
      double v1,
      double v2,
      GeoArgs.Unit unit,
      GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadius");
  }

  @Override
  public Flux<V> georadiusbymember(K k, V v, double v1, GeoArgs.Unit unit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadiusbymember");
  }

  @Override
  public Flux<GeoWithin<V>> georadiusbymember(
      K k, V v, double v1, GeoArgs.Unit unit, GeoArgs geoArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadiusbymember");
  }

  @Override
  public Mono<Long> georadiusbymember(
      K k, V v, double v1, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("georadiusbymember");
  }

  @Override
  public Flux<V> geosearch(K k, GeoSearch.GeoRef<K> geoRef, GeoSearch.GeoPredicate geoPredicate) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geosearch");
  }

  @Override
  public Flux<GeoWithin<V>> geosearch(
      K k, GeoSearch.GeoRef<K> geoRef, GeoSearch.GeoPredicate geoPredicate, GeoArgs geoArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geosearch");
  }

  @Override
  public Mono<Long> geosearchstore(
      K k,
      K k1,
      GeoSearch.GeoRef<K> geoRef,
      GeoSearch.GeoPredicate geoPredicate,
      GeoArgs geoArgs,
      boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("geosearchstore");
  }

  @Override
  public Mono<Long> pfadd(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pfadd");
  }

  @Override
  public Mono<String> pfmerge(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pfmerge");
  }

  @Override
  public Mono<Long> pfcount(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pfcount");
  }

  @Override
  public Mono<Long> hdel(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hdel");
  }

  @Override
  public Mono<Boolean> hexists(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexists");
  }

  @Override
  public Mono<V> hget(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hget");
  }

  @Override
  public Mono<Long> hincrby(K k, K k1, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hincrby");
  }

  @Override
  public Mono<Double> hincrbyfloat(K k, K k1, double v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hincrbyfloat");
  }

  @Override
  public Flux<KeyValue<K, V>> hgetall(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hgetall");
  }

  @Override
  public Mono<Long> hgetall(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hgetall");
  }

  @Override
  public Flux<K> hkeys(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hkeys");
  }

  @Override
  public Mono<Long> hkeys(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hkeys");
  }

  @Override
  public Mono<Long> hlen(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hlen");
  }

  @Override
  public Flux<KeyValue<K, V>> hmget(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hmget");
  }

  @Override
  public Mono<Long> hmget(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hmget");
  }

  @Override
  public Mono<String> hmset(K k, Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hmset");
  }

  @Override
  public Mono<K> hrandfield(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hrandfield");
  }

  @Override
  public Flux<K> hrandfield(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hrandfield");
  }

  @Override
  public Mono<KeyValue<K, V>> hrandfieldWithvalues(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "hrandfieldWithvalues");
  }

  @Override
  public Flux<KeyValue<K, V>> hrandfieldWithvalues(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "hrandfieldWithvalues");
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscan");
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hscanNovalues");
  }

  @Override
  public Mono<Boolean> hset(K k, K k1, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hset");
  }

  @Override
  public Mono<Long> hset(K k, Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hset");
  }

  @Override
  public Mono<Boolean> hsetnx(K k, K k1, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hsetnx");
  }

  @Override
  public Mono<Long> hstrlen(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hstrlen");
  }

  @Override
  public Flux<V> hvals(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hvals");
  }

  @Override
  public Mono<Long> hvals(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hvals");
  }

  @Override
  public Flux<Long> hexpire(K k, long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpire");
  }

  @Override
  public Flux<Long> hexpire(K k, long l, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpire");
  }

  @Override
  public Flux<Long> hexpire(K k, Duration duration, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpire");
  }

  @Override
  public Flux<Long> hexpire(K k, Duration duration, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpire");
  }

  @Override
  public Flux<Long> hexpireat(K k, long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpireat(K k, long l, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpireat(K k, Date date, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpireat(K k, Date date, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpireat(K k, Instant instant, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpireat(K k, Instant instant, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpireat");
  }

  @Override
  public Flux<Long> hexpiretime(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hexpiretime");
  }

  @Override
  public Flux<Long> hpersist(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpersist");
  }

  @Override
  public Flux<Long> hpexpire(K k, long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpire");
  }

  @Override
  public Flux<Long> hpexpire(K k, long l, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpire");
  }

  @Override
  public Flux<Long> hpexpire(K k, Duration duration, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpire");
  }

  @Override
  public Flux<Long> hpexpire(K k, Duration duration, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpire");
  }

  @Override
  public Flux<Long> hpexpireat(K k, long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpireat(K k, long l, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpireat(K k, Date date, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpireat(K k, Date date, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpireat(K k, Instant instant, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpireat(K k, Instant instant, ExpireArgs expireArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpireat");
  }

  @Override
  public Flux<Long> hpexpiretime(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpexpiretime");
  }

  @Override
  public Flux<Long> httl(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("httl");
  }

  @Override
  public Flux<Long> hpttl(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("hpttl");
  }

  @Override
  public Mono<Boolean> copy(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("copy");
  }

  @Override
  public Mono<Boolean> copy(K k, K k1, CopyArgs copyArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("copy");
  }

  @Override
  public Mono<Long> del(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("del");
  }

  @Override
  public Mono<Long> unlink(K... ks) {
    // Delete the keys from Momento
    // TODO: going forward we need to extract the batch operations under an abstraction that
    // handles rate limiting and concurrency concerns.
    var deleteFutures =
        Arrays.stream(ks)
            .map(k -> client.delete(cacheName, codec.encodeKeyToBytes(k)))
            .collect(Collectors.toList());

    // Wait for all the delete commands to complete
    var compositeFuture = CompletableFuture.allOf(deleteFutures.toArray(new CompletableFuture[0]));
    return Mono.fromFuture(compositeFuture)
        .then(
            Mono.defer(
                () -> {
                  var deletedKeys =
                      deleteFutures.stream()
                          .map(CompletableFuture::join)
                          .collect(Collectors.toList());

                  // If any of the delete commands was an error, then return an error.
                  for (var deleteResponse : deletedKeys) {
                    if (deleteResponse instanceof DeleteResponse.Error error) {
                      return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
                    }
                  }
                  return Mono.just((long) ks.length);
                }));
  }

  @Override
  public Mono<byte[]> dump(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("dump");
  }

  @Override
  public Mono<Long> exists(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("exists");
  }

  @Override
  public Mono<Boolean> expire(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expire");
  }

  @Override
  public Mono<Boolean> expire(K k, long l, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expire");
  }

  @Override
  public Mono<Boolean> expire(K k, Duration duration) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expire");
  }

  @Override
  public Mono<Boolean> expire(K k, Duration duration, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expire");
  }

  @Override
  public Mono<Boolean> expireat(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Boolean> expireat(K k, long l, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Boolean> expireat(K k, Date date) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Boolean> expireat(K k, Date date, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Boolean> expireat(K k, Instant instant) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Boolean> expireat(K k, Instant instant, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expireat");
  }

  @Override
  public Mono<Long> expiretime(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("expiretime");
  }

  @Override
  public Flux<K> keys(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("keys");
  }

  @Override
  public Mono<Long> keys(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("keys");
  }

  @Override
  public Mono<String> migrate(String s, int i, K k, int i1, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("migrate");
  }

  @Override
  public Mono<String> migrate(String s, int i, int i1, long l, MigrateArgs<K> migrateArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("migrate");
  }

  @Override
  public Mono<Boolean> move(K k, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("move");
  }

  @Override
  public Mono<String> objectEncoding(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("objectEncoding");
  }

  @Override
  public Mono<Long> objectFreq(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("objectFreq");
  }

  @Override
  public Mono<Long> objectIdletime(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("objectIdletime");
  }

  @Override
  public Mono<Long> objectRefcount(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("objectRefcount");
  }

  @Override
  public Mono<Boolean> persist(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("persist");
  }

  @Override
  public Mono<Boolean> pexpire(K k, long l) {
    return pexpire(k, Duration.ofMillis(l), new ExpireArgs());
  }

  @Override
  public Mono<Boolean> pexpire(K k, long l, ExpireArgs expireArgs) {
    return pexpire(k, Duration.ofMillis(l), expireArgs);
  }

  @Override
  public Mono<Boolean> pexpire(K k, Duration duration) {
    return pexpire(k, duration, new ExpireArgs());
  }

  @Override
  public Mono<Boolean> pexpire(K k, Duration duration, ExpireArgs expireArgs) {
    var expireCondition = ExpireCondition.fromExpireArgs(expireArgs);
    if (expireCondition.requiresNoExpiry()) {
      throw MomentoToLettuceExceptionMapper.createArgumentNotSupportedException(
          "pexpire", "ExpireArgs NX");
    }
    if (expireCondition.requiresGreaterThan()) {
      throw MomentoToLettuceExceptionMapper.createArgumentNotSupportedException(
          "pexpire", "ExpireArgs GT");
    } else if (expireCondition.requiresLessThan()) {
      throw MomentoToLettuceExceptionMapper.createArgumentNotSupportedException(
          "pexpire", "ExpireArgs LT");
    }

    var encodedKey = codec.encodeKeyToBytes(k);

    var updateTtlResponseFuture = client.updateTtl(cacheName, encodedKey, duration);
    return Mono.fromFuture(updateTtlResponseFuture)
        .flatMap(
            response -> {
              if (response instanceof UpdateTtlResponse.Set) {
                return Mono.just(true);
              } else if (response instanceof UpdateTtlResponse.Miss) {
                return Mono.just(false);
              } else if (response instanceof UpdateTtlResponse.Error error) {
                return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                        response.toString()));
              }
            });
  }

  @Override
  public Mono<Boolean> pexpireat(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Boolean> pexpireat(K k, long l, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Date date) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Date date, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Instant instant) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Instant instant, ExpireArgs expireArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpireat");
  }

  @Override
  public Mono<Long> pexpiretime(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pexpiretime");
  }

  @Override
  public Mono<Long> pttl(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("pttl");
  }

  @Override
  public Mono<K> randomkey() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("randomkey");
  }

  @Override
  public Mono<String> rename(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rename");
  }

  @Override
  public Mono<Boolean> renamenx(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("renamenx");
  }

  @Override
  public Mono<String> restore(K k, long l, byte[] bytes) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("restore");
  }

  @Override
  public Mono<String> restore(K k, byte[] bytes, RestoreArgs restoreArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("restore");
  }

  @Override
  public Flux<V> sort(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sort");
  }

  @Override
  public Mono<Long> sort(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sort");
  }

  @Override
  public Flux<V> sort(K k, SortArgs sortArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sort");
  }

  @Override
  public Mono<Long> sort(ValueStreamingChannel<V> valueStreamingChannel, K k, SortArgs sortArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sort");
  }

  @Override
  public Flux<V> sortReadOnly(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sortReadOnly");
  }

  @Override
  public Mono<Long> sortReadOnly(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sortReadOnly");
  }

  @Override
  public Flux<V> sortReadOnly(K k, SortArgs sortArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sortReadOnly");
  }

  @Override
  public Mono<Long> sortReadOnly(
      ValueStreamingChannel<V> valueStreamingChannel, K k, SortArgs sortArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sortReadOnly");
  }

  @Override
  public Mono<Long> sortStore(K k, SortArgs sortArgs, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sortStore");
  }

  @Override
  public Mono<Long> touch(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("touch");
  }

  @Override
  public Mono<Long> ttl(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("ttl");
  }

  @Override
  public Mono<String> type(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("type");
  }

  @Override
  public Mono<KeyScanCursor<K>> scan() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<StreamScanCursor> scan(KeyStreamingChannel<K> keyStreamingChannel) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scan");
  }

  @Override
  public Mono<V> blmove(K k, K k1, LMoveArgs lMoveArgs, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blmove");
  }

  @Override
  public Mono<V> blmove(K k, K k1, LMoveArgs lMoveArgs, double v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blmove");
  }

  @Override
  public Mono<KeyValue<K, List<V>>> blmpop(long l, LMPopArgs lmPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blmpop");
  }

  @Override
  public Mono<KeyValue<K, List<V>>> blmpop(double v, LMPopArgs lmPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blmpop");
  }

  @Override
  public Mono<KeyValue<K, V>> blpop(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blpop");
  }

  @Override
  public Mono<KeyValue<K, V>> blpop(double v, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("blpop");
  }

  @Override
  public Mono<KeyValue<K, V>> brpop(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("brpop");
  }

  @Override
  public Mono<KeyValue<K, V>> brpop(double v, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("brpop");
  }

  @Override
  public Mono<V> brpoplpush(long l, K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("brpoplpush");
  }

  @Override
  public Mono<V> brpoplpush(double v, K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("brpoplpush");
  }

  @Override
  public Mono<V> lindex(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lindex");
  }

  @Override
  public Mono<Long> linsert(K k, boolean b, V v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("linsert");
  }

  @Override
  public Mono<Long> llen(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("llen");
  }

  @Override
  public Mono<V> lmove(K k, K k1, LMoveArgs lMoveArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lmove");
  }

  @Override
  public Mono<KeyValue<K, List<V>>> lmpop(LMPopArgs lmPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lmpop");
  }

  @Override
  public Mono<V> lpop(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpop");
  }

  @Override
  public Flux<V> lpop(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpop");
  }

  @Override
  public Mono<Long> lpos(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpos");
  }

  @Override
  public Mono<Long> lpos(K k, V v, LPosArgs lPosArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpos");
  }

  @Override
  public Flux<Long> lpos(K k, V v, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpos");
  }

  @Override
  public Flux<Long> lpos(K k, V v, int i, LPosArgs lPosArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpos");
  }

  @Override
  public Mono<Long> lpush(K k, V... vs) {
    var encodedValues =
        Arrays.stream(vs).map(codec::encodeValueToBytes).collect(Collectors.toList());

    // Because Redis implements lpush as a reduction over left push, we need to reverse the order of
    // the values
    // before concatenating.
    Collections.reverse(encodedValues);

    var responseFuture =
        client.listConcatenateFrontByteArray(cacheName, codec.encodeKeyToBytes(k), encodedValues);
    return Mono.fromFuture(responseFuture)
        .flatMap(
            response -> {
              if (response instanceof ListConcatenateFrontResponse.Success success) {
                return Mono.just((long) success.getListLength());
              } else if (response instanceof ListConcatenateFrontResponse.Error error) {
                return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                        response.toString()));
              }
            });
  }

  @Override
  public Mono<Long> lpushx(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lpushx");
  }

  @Override
  public Flux<V> lrange(K k, long l, long l1) {
    ValidatorUtils.ensureInIntegerRange(l, "l");
    ValidatorUtils.ensureInIntegerRange(l1, "l1");
    Integer start = (int) l;
    Integer end = RangeUtils.adjustEndRangeFromInclusiveToExclusive(l1);

    var responseFuture = client.listFetch(cacheName, codec.encodeKeyToBytes(k), start, end);
    Mono<List<V>> mono =
        Mono.fromFuture(responseFuture)
            .flatMap(
                response -> {
                  if (response instanceof ListFetchResponse.Hit hit) {
                    List<V> result =
                        hit.valueListByteArray().stream()
                            .map(codec::decodeValueFromBytes)
                            .collect(Collectors.toList());
                    return Mono.just(result);

                  } else if (response instanceof ListFetchResponse.Miss) {
                    return Mono.just(Collections.emptyList());
                  } else if (response instanceof ListFetchResponse.Error error) {
                    return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
                  } else {
                    return Mono.error(
                        MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                            response.toString()));
                  }
                });
    return mono.flatMapMany(Flux::fromIterable);
  }

  @Override
  public Mono<Long> lrange(ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lrange");
  }

  @Override
  public Mono<Long> lrem(K k, long l, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lrem");
  }

  @Override
  public Mono<String> lset(K k, long l, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lset");
  }

  @Override
  public Mono<String> ltrim(K k, long l, long l1) {
    ValidatorUtils.ensureInIntegerRange(l, "l");
    ValidatorUtils.ensureInIntegerRange(l1, "l1");

    // When the range is empty, Redis dictates that the list should be deleted.
    if (((l >= 0 && l1 >= 0) || (l < 0 && l1 < 0)) && l > l1) {
      return unlink(k).then(Mono.just(RedisResponse.OK));
    }

    Integer start = (int) l;
    Integer end = RangeUtils.adjustEndRangeFromInclusiveToExclusive(l1);

    var responseFuture = client.listRetain(cacheName, codec.encodeKeyToBytes(k), start, end);
    return Mono.fromFuture(responseFuture)
        .flatMap(
            response -> {
              if (response instanceof ListRetainResponse.Success) {
                return Mono.just(RedisResponse.OK);
              } else if (response instanceof ListRetainResponse.Error error) {
                return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                        response.toString()));
              }
            });
  }

  @Override
  public Mono<V> rpop(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rpop");
  }

  @Override
  public Flux<V> rpop(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rpop");
  }

  @Override
  public Mono<V> rpoplpush(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rpoplpush");
  }

  @Override
  public Mono<Long> rpush(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rpush");
  }

  @Override
  public Mono<Long> rpushx(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("rpushx");
  }

  @Override
  public <T> Flux<T> eval(String s, ScriptOutputType scriptOutputType, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("eval");
  }

  @Override
  public <T> Flux<T> eval(byte[] bytes, ScriptOutputType scriptOutputType, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("eval");
  }

  @Override
  public <T> Flux<T> eval(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("eval");
  }

  @Override
  public <T> Flux<T> eval(byte[] bytes, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("eval");
  }

  @Override
  public <T> Flux<T> evalReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("evalReadOnly");
  }

  @Override
  public <T> Flux<T> evalReadOnly(
      byte[] bytes, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("evalReadOnly");
  }

  @Override
  public <T> Flux<T> evalsha(String s, ScriptOutputType scriptOutputType, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("evalsha");
  }

  @Override
  public <T> Flux<T> evalsha(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("evalsha");
  }

  @Override
  public <T> Flux<T> evalshaReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("evalshaReadOnly");
  }

  @Override
  public Flux<Boolean> scriptExists(String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptExists");
  }

  @Override
  public Mono<String> scriptFlush() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptFlush");
  }

  @Override
  public Mono<String> scriptFlush(FlushMode flushMode) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptFlush");
  }

  @Override
  public Mono<String> scriptKill() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptKill");
  }

  @Override
  public Mono<String> scriptLoad(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptLoad");
  }

  @Override
  public Mono<String> scriptLoad(byte[] bytes) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scriptLoad");
  }

  @Override
  public String digest(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("digest");
  }

  @Override
  public String digest(byte[] bytes) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("digest");
  }

  @Override
  public Mono<String> bgrewriteaof() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bgrewriteaof");
  }

  @Override
  public Mono<String> bgsave() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bgsave");
  }

  @Override
  public Mono<String> clientCaching(boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientCaching");
  }

  @Override
  public Mono<K> clientGetname() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientGetname");
  }

  @Override
  public Mono<Long> clientGetredir() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientGetredir");
  }

  @Override
  public Mono<Long> clientId() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientId");
  }

  @Override
  public Mono<String> clientKill(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientKill");
  }

  @Override
  public Mono<Long> clientKill(KillArgs killArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientKill");
  }

  @Override
  public Mono<String> clientList() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientList");
  }

  @Override
  public Mono<String> clientList(ClientListArgs clientListArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientList");
  }

  @Override
  public Mono<String> clientInfo() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientInfo");
  }

  @Override
  public Mono<String> clientNoEvict(boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientNoEvict");
  }

  @Override
  public Mono<String> clientPause(long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientPause");
  }

  @Override
  public Mono<String> clientSetname(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientSetname");
  }

  @Override
  public Mono<String> clientSetinfo(String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientSetinfo");
  }

  @Override
  public Mono<String> clientTracking(TrackingArgs trackingArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientTracking");
  }

  @Override
  public Mono<Long> clientUnblock(long l, UnblockType unblockType) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("clientUnblock");
  }

  @Override
  public Flux<Object> command() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("command");
  }

  @Override
  public Mono<Long> commandCount() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("commandCount");
  }

  @Override
  public Flux<Object> commandInfo(String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("commandInfo");
  }

  @Override
  public Flux<Object> commandInfo(CommandType... commandTypes) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("commandInfo");
  }

  @Override
  public Mono<Map<String, String>> configGet(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("String");
  }

  @Override
  public Mono<Map<String, String>> configGet(String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("String");
  }

  @Override
  public Mono<String> configResetstat() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("configResetstat");
  }

  @Override
  public Mono<String> configRewrite() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("configRewrite");
  }

  @Override
  public Mono<String> configSet(String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("configSet");
  }

  @Override
  public Mono<String> configSet(Map<String, String> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("configSet");
  }

  @Override
  public Mono<Long> dbsize() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("dbsize");
  }

  @Override
  public Mono<String> debugCrashAndRecover(Long aLong) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "debugCrashAndRecover");
  }

  @Override
  public Mono<String> debugHtstats(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugHtstats");
  }

  @Override
  public Mono<String> debugObject(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugObject");
  }

  @Override
  public Mono<Void> debugOom() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugOom");
  }

  @Override
  public Mono<String> debugReload() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugReload");
  }

  @Override
  public Mono<String> debugRestart(Long aLong) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugRestart");
  }

  @Override
  public Mono<String> debugSdslen(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugSdslen");
  }

  @Override
  public Mono<Void> debugSegfault() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("debugSegfault");
  }

  @Override
  public Mono<String> flushall() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushall");
  }

  @Override
  public Mono<String> flushall(FlushMode flushMode) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushall");
  }

  @Override
  public Mono<String> flushallAsync() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushallAsync");
  }

  @Override
  public Mono<String> flushdb() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushdb");
  }

  @Override
  public Mono<String> flushdb(FlushMode flushMode) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushdb");
  }

  @Override
  public Mono<String> flushdbAsync() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("flushdbAsync");
  }

  @Override
  public Mono<String> info() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("info");
  }

  @Override
  public Mono<String> info(String s) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("info");
  }

  @Override
  public Mono<Date> lastsave() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("lastsave");
  }

  @Override
  public Mono<Long> memoryUsage(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("memoryUsage");
  }

  @Override
  public Mono<String> replicaof(String s, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("replicaof");
  }

  @Override
  public Mono<String> replicaofNoOne() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("replicaofNoOne");
  }

  @Override
  public Mono<String> save() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("save");
  }

  @Override
  public Mono<Void> shutdown(boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("shutdown");
  }

  @Override
  public Mono<Void> shutdown(ShutdownArgs shutdownArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("shutdown");
  }

  @Override
  public Mono<String> slaveof(String s, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slaveof");
  }

  @Override
  public Mono<String> slaveofNoOne() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slaveofNoOne");
  }

  @Override
  public Flux<Object> slowlogGet() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slowlogGet");
  }

  @Override
  public Flux<Object> slowlogGet(int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slowlogGet");
  }

  @Override
  public Mono<Long> slowlogLen() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slowlogLen");
  }

  @Override
  public Mono<String> slowlogReset() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("slowlogReset");
  }

  @Override
  public Flux<V> time() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("time");
  }

  @Override
  public Mono<Long> sadd(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sadd");
  }

  @Override
  public Mono<Long> scard(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("scard");
  }

  @Override
  public Flux<V> sdiff(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sdiff");
  }

  @Override
  public Mono<Long> sdiff(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sdiff");
  }

  @Override
  public Mono<Long> sdiffstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sdiffstore");
  }

  @Override
  public Flux<V> sinter(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sinter");
  }

  @Override
  public Mono<Long> sinter(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sinter");
  }

  @Override
  public Mono<Long> sintercard(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sintercard");
  }

  @Override
  public Mono<Long> sintercard(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sintercard");
  }

  @Override
  public Mono<Long> sinterstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sinterstore");
  }

  @Override
  public Mono<Boolean> sismember(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sismember");
  }

  @Override
  public Flux<V> smembers(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("smembers");
  }

  @Override
  public Mono<Long> smembers(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("smembers");
  }

  @Override
  public Flux<Boolean> smismember(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("smismember");
  }

  @Override
  public Mono<Boolean> smove(K k, K k1, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("smove");
  }

  @Override
  public Mono<V> spop(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("spop");
  }

  @Override
  public Flux<V> spop(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("spop");
  }

  @Override
  public Mono<V> srandmember(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("srandmember");
  }

  @Override
  public Flux<V> srandmember(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("srandmember");
  }

  @Override
  public Mono<Long> srandmember(ValueStreamingChannel<V> valueStreamingChannel, K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("srandmember");
  }

  @Override
  public Mono<Long> srem(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("srem");
  }

  @Override
  public Flux<V> sunion(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sunion");
  }

  @Override
  public Mono<Long> sunion(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sunion");
  }

  @Override
  public Mono<Long> sunionstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sunionstore");
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<StreamScanCursor> sscan(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel, K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel, K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("sscan");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzmpop(long l, ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzmpop");
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> bzmpop(
      long l, long l1, ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzmpop");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzmpop(double v, ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzmpop");
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> bzmpop(
      double v, int i, ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzmpop");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmin(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzpopmin");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmin(double v, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzpopmin");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmax(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzpopmax");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmax(double v, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bzpopmax");
  }

  @Override
  public Mono<Long> zadd(K k, double v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Long> zadd(K k, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Long> zadd(K k, ScoredValue<V>... scoredValues) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, double v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, ScoredValue<V>... scoredValues) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zadd");
  }

  @Override
  public Mono<Double> zaddincr(K k, double v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zaddincr");
  }

  @Override
  public Mono<Double> zaddincr(K k, ZAddArgs zAddArgs, double v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zaddincr");
  }

  @Override
  public Mono<Long> zcard(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zcard");
  }

  @Override
  public Mono<Long> zcount(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zcount");
  }

  @Override
  public Mono<Long> zcount(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zcount");
  }

  @Override
  public Mono<Long> zcount(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zcount");
  }

  @Override
  public Flux<V> zdiff(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zdiff");
  }

  @Override
  public Mono<Long> zdiffstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zdiffstore");
  }

  @Override
  public Flux<ScoredValue<V>> zdiffWithScores(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zdiffWithScores");
  }

  @Override
  public Mono<Double> zincrby(K k, double v, V v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zincrby");
  }

  @Override
  public Flux<V> zinter(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinter");
  }

  @Override
  public Flux<V> zinter(ZAggregateArgs zAggregateArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinter");
  }

  @Override
  public Mono<Long> zintercard(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zintercard");
  }

  @Override
  public Mono<Long> zintercard(long l, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zintercard");
  }

  @Override
  public Flux<ScoredValue<V>> zinterWithScores(ZAggregateArgs zAggregateArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinterWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zinterWithScores(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinterWithScores");
  }

  @Override
  public Mono<Long> zinterstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinterstore");
  }

  @Override
  public Mono<Long> zinterstore(K k, ZStoreArgs zStoreArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zinterstore");
  }

  @Override
  public Mono<Long> zlexcount(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zlexcount");
  }

  @Override
  public Mono<Long> zlexcount(K k, Range<? extends V> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zlexcount");
  }

  @Override
  public Mono<List<Double>> zmscore(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zmscore");
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> zmpop(ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zmpop");
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> zmpop(int i, ZPopArgs zPopArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zmpop");
  }

  @Override
  public Mono<ScoredValue<V>> zpopmin(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zpopmin");
  }

  @Override
  public Flux<ScoredValue<V>> zpopmin(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zpopmin");
  }

  @Override
  public Mono<ScoredValue<V>> zpopmax(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zpopmax");
  }

  @Override
  public Flux<ScoredValue<V>> zpopmax(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zpopmax");
  }

  @Override
  public Mono<V> zrandmember(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrandmember");
  }

  @Override
  public Flux<V> zrandmember(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrandmember");
  }

  @Override
  public Mono<ScoredValue<V>> zrandmemberWithScores(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrandmemberWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrandmemberWithScores(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrandmemberWithScores");
  }

  @Override
  public Flux<V> zrange(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrange");
  }

  @Override
  public Mono<Long> zrange(ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrange");
  }

  @Override
  public Flux<ScoredValue<V>> zrangeWithScores(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangeWithScores");
  }

  @Override
  public Mono<Long> zrangeWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangeWithScores");
  }

  @Override
  public Flux<V> zrangebylex(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebylex");
  }

  @Override
  public Flux<V> zrangebylex(K k, Range<? extends V> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebylex");
  }

  @Override
  public Flux<V> zrangebylex(K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebylex");
  }

  @Override
  public Flux<V> zrangebylex(K k, Range<? extends V> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebylex");
  }

  @Override
  public Flux<V> zrangebyscore(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<V> zrangebyscore(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<V> zrangebyscore(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<V> zrangebyscore(K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<V> zrangebyscore(K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<V> zrangebyscore(K k, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebycore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangebyscore");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(
      K k, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebycoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      double v,
      double v1,
      long l,
      long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebycoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      String s,
      String s1,
      long l,
      long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrangestore(K k, K k1, Range<Long> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangestore");
  }

  @Override
  public Mono<Long> zrangestorebylex(K k, K k1, Range<? extends V> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrangestorebylex");
  }

  @Override
  public Mono<Long> zrangestorebyscore(K k, K k1, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrangestorebyscore");
  }

  @Override
  public Mono<Long> zrank(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrank");
  }

  @Override
  public Mono<ScoredValue<Long>> zrankWithScore(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrankWithScore");
  }

  @Override
  public Mono<Long> zrem(K k, V... vs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrem");
  }

  @Override
  public Mono<Long> zremrangebylex(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebylex");
  }

  @Override
  public Mono<Long> zremrangebylex(K k, Range<? extends V> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebylex");
  }

  @Override
  public Mono<Long> zremrangebyrank(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebyrank");
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebyscore");
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebyscore");
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zremrangebyscore");
  }

  @Override
  public Flux<V> zrevrange(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrange");
  }

  @Override
  public Mono<Long> zrevrange(
      ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrange");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangeWithScores(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangeWithScores");
  }

  @Override
  public Mono<Long> zrevrangeWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangeWithScores");
  }

  @Override
  public Flux<V> zrevrangebylex(K k, Range<? extends V> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebylex");
  }

  @Override
  public Flux<V> zrevrangebylex(K k, Range<? extends V> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebylex");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangebyscore");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, double v, double v1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, String s, String s1, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, double v, double v1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, String s, String s1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      double v,
      double v1,
      long l,
      long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      String s,
      String s1,
      long l,
      long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangebyscoreWithScores");
  }

  @Override
  public Mono<Long> zrevrangestore(K k, K k1, Range<Long> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrangestore");
  }

  @Override
  public Mono<Long> zrevrangestorebylex(K k, K k1, Range<? extends V> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangestorebylex");
  }

  @Override
  public Mono<Long> zrevrangestorebyscore(K k, K k1, Range<? extends Number> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "zrevrangestorebyscore");
  }

  @Override
  public Mono<Long> zrevrank(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrank");
  }

  @Override
  public Mono<ScoredValue<Long>> zrevrankWithScore(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zrevrankWithScore");
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, ScanCursor scanCursor) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscan");
  }

  @Override
  public Mono<Double> zscore(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zscore");
  }

  @Override
  public Flux<V> zunion(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunion");
  }

  @Override
  public Flux<V> zunion(ZAggregateArgs zAggregateArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunion");
  }

  @Override
  public Flux<ScoredValue<V>> zunionWithScores(ZAggregateArgs zAggregateArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunionWithScores");
  }

  @Override
  public Flux<ScoredValue<V>> zunionWithScores(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunionWithScores");
  }

  @Override
  public Mono<Long> zunionstore(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunionstore");
  }

  @Override
  public Mono<Long> zunionstore(K k, ZStoreArgs zStoreArgs, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("zunionstore");
  }

  @Override
  public Mono<Long> xack(K k, K k1, String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xack");
  }

  @Override
  public Mono<String> xadd(K k, Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xadd");
  }

  @Override
  public Mono<String> xadd(K k, XAddArgs xAddArgs, Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xadd");
  }

  @Override
  public Mono<String> xadd(K k, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xadd");
  }

  @Override
  public Mono<String> xadd(K k, XAddArgs xAddArgs, Object... objects) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xadd");
  }

  @Override
  public Mono<ClaimedMessages<K, V>> xautoclaim(K k, XAutoClaimArgs<K> xAutoClaimArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xautoclaim");
  }

  @Override
  public Flux<StreamMessage<K, V>> xclaim(K k, Consumer<K> consumer, long l, String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xclaim");
  }

  @Override
  public Flux<StreamMessage<K, V>> xclaim(
      K k, Consumer<K> consumer, XClaimArgs xClaimArgs, String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xclaim");
  }

  @Override
  public Mono<Long> xdel(K k, String... strings) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xdel");
  }

  @Override
  public Mono<String> xgroupCreate(XReadArgs.StreamOffset<K> streamOffset, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xgroupCreate");
  }

  @Override
  public Mono<String> xgroupCreate(
      XReadArgs.StreamOffset<K> streamOffset, K k, XGroupCreateArgs xGroupCreateArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xgroupCreate");
  }

  @Override
  public Mono<Boolean> xgroupCreateconsumer(K k, Consumer<K> consumer) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException(
        "xgroupCreateconsumer");
  }

  @Override
  public Mono<Long> xgroupDelconsumer(K k, Consumer<K> consumer) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xgroupDelconsumer");
  }

  @Override
  public Mono<Boolean> xgroupDestroy(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xgroupDestroy");
  }

  @Override
  public Mono<String> xgroupSetid(XReadArgs.StreamOffset<K> streamOffset, K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xgroupSetid");
  }

  @Override
  public Flux<Object> xinfoStream(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xinfoStream");
  }

  @Override
  public Flux<Object> xinfoGroups(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xinfoGroups");
  }

  @Override
  public Flux<Object> xinfoConsumers(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xinfoConsumers");
  }

  @Override
  public Mono<Long> xlen(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xlen");
  }

  @Override
  public Mono<PendingMessages> xpending(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xpending");
  }

  @Override
  public Flux<PendingMessage> xpending(K k, K k1, Range<String> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xpending");
  }

  @Override
  public Flux<PendingMessage> xpending(
      K k, Consumer<K> consumer, Range<String> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xpending");
  }

  @Override
  public Flux<PendingMessage> xpending(K k, XPendingArgs<K> xPendingArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xpending");
  }

  @Override
  public Flux<StreamMessage<K, V>> xrange(K k, Range<String> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xrange");
  }

  @Override
  public Flux<StreamMessage<K, V>> xrange(K k, Range<String> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xrange");
  }

  @Override
  public Flux<StreamMessage<K, V>> xread(XReadArgs.StreamOffset<K>... streamOffsets) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xread");
  }

  @Override
  public Flux<StreamMessage<K, V>> xread(
      XReadArgs xReadArgs, XReadArgs.StreamOffset<K>... streamOffsets) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xread");
  }

  @Override
  public Flux<StreamMessage<K, V>> xreadgroup(
      Consumer<K> consumer, XReadArgs.StreamOffset<K>... streamOffsets) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xreadgroup");
  }

  @Override
  public Flux<StreamMessage<K, V>> xreadgroup(
      Consumer<K> consumer, XReadArgs xReadArgs, XReadArgs.StreamOffset<K>... streamOffsets) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xreadgroup");
  }

  @Override
  public Flux<StreamMessage<K, V>> xrevrange(K k, Range<String> range) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xrevrange");
  }

  @Override
  public Flux<StreamMessage<K, V>> xrevrange(K k, Range<String> range, Limit limit) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xrevrange");
  }

  @Override
  public Mono<Long> xtrim(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xtrim");
  }

  @Override
  public Mono<Long> xtrim(K k, boolean b, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xtrim");
  }

  @Override
  public Mono<Long> xtrim(K k, XTrimArgs xTrimArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("xtrim");
  }

  @Override
  public Mono<Long> append(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("append");
  }

  @Override
  public Mono<Long> bitcount(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitcount");
  }

  @Override
  public Mono<Long> bitcount(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitcount");
  }

  @Override
  public Flux<Value<Long>> bitfield(K k, BitFieldArgs bitFieldArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitfield");
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitpos");
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitpos");
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitpos");
  }

  @Override
  public Mono<Long> bitopAnd(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitopAnd");
  }

  @Override
  public Mono<Long> bitopNot(K k, K k1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitopNot");
  }

  @Override
  public Mono<Long> bitopOr(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitopOr");
  }

  @Override
  public Mono<Long> bitopXor(K k, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("bitopXor");
  }

  @Override
  public Mono<Long> decr(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("decr");
  }

  @Override
  public Mono<Long> decrby(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("decrby");
  }

  @Override
  public Mono<V> get(K k) {
    CompletableFuture<GetResponse> getResponseFuture =
        client.get(cacheName, codec.encodeKeyToBytes(k));
    return Mono.fromFuture(getResponseFuture)
        .flatMap(
            getResponse -> {
              if (getResponse instanceof GetResponse.Hit getResponseHit) {
                return Mono.just(codec.decodeValueFromBytes(getResponseHit.valueByteArray()));
              } else if (getResponse instanceof GetResponse.Miss) {
                return Mono.empty();
              } else if (getResponse instanceof GetResponse.Error error) {
                return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                        getResponse.toString()));
              }
            });
  }

  @Override
  public Mono<Long> getbit(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("getbit");
  }

  @Override
  public Mono<V> getdel(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("getdel");
  }

  @Override
  public Mono<V> getex(K k, GetExArgs getExArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("getex");
  }

  @Override
  public Mono<V> getrange(K k, long l, long l1) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("getrange");
  }

  @Override
  public Mono<V> getset(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("getset");
  }

  @Override
  public Mono<Long> incr(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("incr");
  }

  @Override
  public Mono<Long> incrby(K k, long l) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("incrby");
  }

  @Override
  public Mono<Double> incrbyfloat(K k, double v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("incrbyfloat");
  }

  @Override
  public Flux<KeyValue<K, V>> mget(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("mget");
  }

  @Override
  public Mono<Long> mget(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("mget");
  }

  @Override
  public Mono<String> mset(Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("mset");
  }

  @Override
  public Mono<Boolean> msetnx(Map<K, V> map) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("msetnx");
  }

  @Override
  public Mono<String> set(K k, V v) {
    CompletableFuture<SetResponse> setResponseFuture =
        client.set(cacheName, codec.encodeKeyToBytes(k), codec.encodeValueToBytes(v));
    return Mono.fromFuture(setResponseFuture)
        .flatMap(
            setResponse -> {
              if (setResponse instanceof SetResponse.Success) {
                return Mono.just(RedisResponse.OK);
              } else if (setResponse instanceof SetResponse.Error error) {
                return Mono.error(MomentoToLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoToLettuceExceptionMapper.createUnexpectedResponseException(
                        setResponse.toString()));
              }
            });
  }

  @Override
  public Mono<String> set(K k, V v, SetArgs setArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("set");
  }

  @Override
  public Mono<V> setGet(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setGet");
  }

  @Override
  public Mono<V> setGet(K k, V v, SetArgs setArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setGet");
  }

  @Override
  public Mono<Long> setbit(K k, long l, int i) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setbit");
  }

  @Override
  public Mono<String> setex(K k, long l, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setex");
  }

  @Override
  public Mono<String> psetex(K k, long l, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("psetex");
  }

  @Override
  public Mono<Boolean> setnx(K k, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setnx");
  }

  @Override
  public Mono<Long> setrange(K k, long l, V v) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("setrange");
  }

  @Override
  public Mono<StringMatchResult> stralgoLcs(StrAlgoArgs strAlgoArgs) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("stralgoLcs");
  }

  @Override
  public Mono<Long> strlen(K k) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("strlen");
  }

  @Override
  public Mono<String> discard() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("discard");
  }

  @Override
  public Mono<TransactionResult> exec() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("exec");
  }

  @Override
  public Mono<String> multi() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("multi");
  }

  @Override
  public Mono<String> watch(K... ks) {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("watch");
  }

  @Override
  public Mono<String> unwatch() {
    throw MomentoToLettuceExceptionMapper.createCommandNotImplementedException("unwatch");
  }
}
