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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import momento.lettuce.utils.MomentoLettuceExceptionMapper;
import momento.lettuce.utils.RedisCodecByteArrayConverter;
import momento.lettuce.utils.RedisResponse;
import momento.sdk.CacheClient;
import momento.sdk.responses.cache.GetResponse;
import momento.sdk.responses.cache.SetResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MomentoRedisReactiveClient<K, V>
    implements RedisReactiveCommands<K, V>, MomentoRedisReactiveCommands<K, V> {
  private final CacheClient client;
  private final String cacheName;
  private final RedisCodecByteArrayConverter<K, V> codec;
  private volatile EventExecutorGroup scheduler;

  public MomentoRedisReactiveClient(CacheClient client, String cacheName, RedisCodec<K, V> codec) {
    this.client = client;
    this.cacheName = cacheName;
    this.codec = new RedisCodecByteArrayConverter<>(codec);
    this.scheduler = ImmediateEventExecutor.INSTANCE;
  }

  public static <K, V> MomentoRedisReactiveClient<K, V> create(
      CacheClient client, String cacheName, RedisCodec<K, V> codec) {
    return new MomentoRedisReactiveClient<>(client, cacheName, codec);
  }

  public static MomentoRedisReactiveClient<String, String> create(
      CacheClient client, String cacheName) {
    return new MomentoRedisReactiveClient<>(client, cacheName, StringCodec.UTF8);
  }

  @Override
  public void setTimeout(Duration duration) {}

  @Override
  public Mono<String> asking() {
    return null;
  }

  @Override
  public Mono<String> auth(CharSequence charSequence) {
    return null;
  }

  @Override
  public Mono<String> auth(String s, CharSequence charSequence) {
    return null;
  }

  @Override
  public Mono<String> clusterAddSlots(int... ints) {
    return null;
  }

  @Override
  public Mono<String> clusterBumpepoch() {
    return null;
  }

  @Override
  public Mono<Long> clusterCountFailureReports(String s) {
    return null;
  }

  @Override
  public Mono<Long> clusterCountKeysInSlot(int i) {
    return null;
  }

  @Override
  public Mono<String> clusterAddSlotsRange(Range<Integer>... ranges) {
    return null;
  }

  @Override
  public Mono<String> clusterDelSlots(int... ints) {
    return null;
  }

  @Override
  public Mono<String> clusterDelSlotsRange(Range<Integer>... ranges) {
    return null;
  }

  @Override
  public Mono<String> clusterFailover(boolean b) {
    return null;
  }

  @Override
  public Mono<String> clusterFailover(boolean b, boolean b1) {
    return null;
  }

  @Override
  public Mono<String> clusterFlushslots() {
    return null;
  }

  @Override
  public Mono<String> clusterForget(String s) {
    return null;
  }

  @Override
  public Flux<K> clusterGetKeysInSlot(int i, int i1) {
    return null;
  }

  @Override
  public Mono<String> clusterInfo() {
    return null;
  }

  @Override
  public Mono<Long> clusterKeyslot(K k) {
    return null;
  }

  @Override
  public Mono<String> clusterMeet(String s, int i) {
    return null;
  }

  @Override
  public Mono<String> clusterMyId() {
    return null;
  }

  @Override
  public Mono<String> clusterNodes() {
    return null;
  }

  @Override
  public Mono<String> clusterReplicate(String s) {
    return null;
  }

  @Override
  public Flux<String> clusterReplicas(String s) {
    return null;
  }

  @Override
  public Mono<String> clusterReset(boolean b) {
    return null;
  }

  @Override
  public Mono<String> clusterSaveconfig() {
    return null;
  }

  @Override
  public Mono<String> clusterSetConfigEpoch(long l) {
    return null;
  }

  @Override
  public Mono<String> clusterSetSlotImporting(int i, String s) {
    return null;
  }

  @Override
  public Mono<String> clusterSetSlotMigrating(int i, String s) {
    return null;
  }

  @Override
  public Mono<String> clusterSetSlotNode(int i, String s) {
    return null;
  }

  @Override
  public Mono<String> clusterSetSlotStable(int i) {
    return null;
  }

  @Override
  public Mono<List<Object>> clusterShards() {
    return null;
  }

  @Override
  public Flux<String> clusterSlaves(String s) {
    return null;
  }

  @Override
  public Flux<Object> clusterSlots() {
    return null;
  }

  @Override
  public Mono<String> select(int i) {
    return null;
  }

  @Override
  public Mono<String> swapdb(int i, int i1) {
    return null;
  }

  @Override
  public StatefulRedisConnection<K, V> getStatefulConnection() {
    return null;
  }

  @Override
  public Mono<Long> publish(K k, V v) {
    return null;
  }

  @Override
  public Flux<K> pubsubChannels() {
    return null;
  }

  @Override
  public Flux<K> pubsubChannels(K k) {
    return null;
  }

  @Override
  public Mono<Map<K, Long>> pubsubNumsub(K... ks) {
    return null;
  }

  @Override
  public Flux<K> pubsubShardChannels() {
    return null;
  }

  @Override
  public Flux<K> pubsubShardChannels(K k) {
    return null;
  }

  @Override
  public Mono<Map<K, Long>> pubsubShardNumsub(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> pubsubNumpat() {
    return null;
  }

  @Override
  public Mono<Long> spublish(K k, V v) {
    return null;
  }

  @Override
  public Mono<V> echo(V v) {
    return null;
  }

  @Override
  public Flux<Object> role() {
    return null;
  }

  @Override
  public Mono<String> ping() {
    return null;
  }

  @Override
  public Mono<String> readOnly() {
    return null;
  }

  @Override
  public Mono<String> readWrite() {
    return null;
  }

  @Override
  public Mono<String> quit() {
    return null;
  }

  @Override
  public Mono<Long> waitForReplication(int i, long l) {
    return null;
  }

  @Override
  public <T> Flux<T> dispatch(
      ProtocolKeyword protocolKeyword, CommandOutput<K, V, ?> commandOutput) {
    return null;
  }

  @Override
  public <T> Flux<T> dispatch(
      ProtocolKeyword protocolKeyword,
      CommandOutput<K, V, ?> commandOutput,
      CommandArgs<K, V> commandArgs) {
    return null;
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
    return null;
  }

  @Override
  public Mono<Set<CommandType>> aclCat(AclCategory aclCategory) {
    return null;
  }

  @Override
  public Mono<Long> aclDeluser(String... strings) {
    return null;
  }

  @Override
  public Mono<String> aclDryRun(String s, String s1, String... strings) {
    return null;
  }

  @Override
  public Mono<String> aclDryRun(String s, RedisCommand<K, V, ?> redisCommand) {
    return null;
  }

  @Override
  public Mono<String> aclGenpass() {
    return null;
  }

  @Override
  public Mono<String> aclGenpass(int i) {
    return null;
  }

  @Override
  public Mono<List<Object>> aclGetuser(String s) {
    return null;
  }

  @Override
  public Flux<String> aclList() {
    return null;
  }

  @Override
  public Mono<String> aclLoad() {
    return null;
  }

  @Override
  public Flux<Map<String, Object>> aclLog() {
    return null;
  }

  @Override
  public Flux<Map<String, Object>> aclLog(int i) {
    return null;
  }

  @Override
  public Mono<String> aclLogReset() {
    return null;
  }

  @Override
  public Mono<String> aclSave() {
    return null;
  }

  @Override
  public Mono<String> aclSetuser(String s, AclSetuserArgs aclSetuserArgs) {
    return null;
  }

  @Override
  public Flux<String> aclUsers() {
    return null;
  }

  @Override
  public Mono<String> aclWhoami() {
    return null;
  }

  @Override
  public <T> Flux<T> fcall(String s, ScriptOutputType scriptOutputType, K... ks) {
    return null;
  }

  @Override
  public <T> Flux<T> fcall(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> fcallReadOnly(String s, ScriptOutputType scriptOutputType, K... ks) {
    return null;
  }

  @Override
  public <T> Flux<T> fcallReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public Mono<String> functionLoad(String s) {
    return null;
  }

  @Override
  public Mono<String> functionLoad(String s, boolean b) {
    return null;
  }

  @Override
  public Mono<byte[]> functionDump() {
    return null;
  }

  @Override
  public Mono<String> functionRestore(byte[] bytes) {
    return null;
  }

  @Override
  public Mono<String> functionRestore(byte[] bytes, FunctionRestoreMode functionRestoreMode) {
    return null;
  }

  @Override
  public Mono<String> functionFlush(FlushMode flushMode) {
    return null;
  }

  @Override
  public Mono<String> functionKill() {
    return null;
  }

  @Override
  public Flux<Map<String, Object>> functionList() {
    return null;
  }

  @Override
  public Flux<Map<String, Object>> functionList(String s) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, double v, double v1, V v2) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, double v, double v1, V v2, GeoAddArgs geoAddArgs) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, Object... objects) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, GeoValue<V>... geoValues) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, GeoAddArgs geoAddArgs, Object... objects) {
    return null;
  }

  @Override
  public Mono<Long> geoadd(K k, GeoAddArgs geoAddArgs, GeoValue<V>... geoValues) {
    return null;
  }

  @Override
  public Mono<Double> geodist(K k, V v, V v1, GeoArgs.Unit unit) {
    return null;
  }

  @Override
  public Flux<Value<String>> geohash(K k, V... vs) {
    return null;
  }

  @Override
  public Flux<Value<GeoCoordinates>> geopos(K k, V... vs) {
    return null;
  }

  @Override
  public Flux<V> georadius(K k, double v, double v1, double v2, GeoArgs.Unit unit) {
    return null;
  }

  @Override
  public Flux<GeoWithin<V>> georadius(
      K k, double v, double v1, double v2, GeoArgs.Unit unit, GeoArgs geoArgs) {
    return null;
  }

  @Override
  public Mono<Long> georadius(
      K k,
      double v,
      double v1,
      double v2,
      GeoArgs.Unit unit,
      GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
    return null;
  }

  @Override
  public Flux<V> georadiusbymember(K k, V v, double v1, GeoArgs.Unit unit) {
    return null;
  }

  @Override
  public Flux<GeoWithin<V>> georadiusbymember(
      K k, V v, double v1, GeoArgs.Unit unit, GeoArgs geoArgs) {
    return null;
  }

  @Override
  public Mono<Long> georadiusbymember(
      K k, V v, double v1, GeoArgs.Unit unit, GeoRadiusStoreArgs<K> geoRadiusStoreArgs) {
    return null;
  }

  @Override
  public Flux<V> geosearch(K k, GeoSearch.GeoRef<K> geoRef, GeoSearch.GeoPredicate geoPredicate) {
    return null;
  }

  @Override
  public Flux<GeoWithin<V>> geosearch(
      K k, GeoSearch.GeoRef<K> geoRef, GeoSearch.GeoPredicate geoPredicate, GeoArgs geoArgs) {
    return null;
  }

  @Override
  public Mono<Long> geosearchstore(
      K k,
      K k1,
      GeoSearch.GeoRef<K> geoRef,
      GeoSearch.GeoPredicate geoPredicate,
      GeoArgs geoArgs,
      boolean b) {
    return null;
  }

  @Override
  public Mono<Long> pfadd(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<String> pfmerge(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> pfcount(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> hdel(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Boolean> hexists(K k, K k1) {
    return null;
  }

  @Override
  public Mono<V> hget(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Long> hincrby(K k, K k1, long l) {
    return null;
  }

  @Override
  public Mono<Double> hincrbyfloat(K k, K k1, double v) {
    return null;
  }

  @Override
  public Flux<KeyValue<K, V>> hgetall(K k) {
    return null;
  }

  @Override
  public Mono<Long> hgetall(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Flux<K> hkeys(K k) {
    return null;
  }

  @Override
  public Mono<Long> hkeys(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<Long> hlen(K k) {
    return null;
  }

  @Override
  public Flux<KeyValue<K, V>> hmget(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> hmget(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, K... ks) {
    return null;
  }

  @Override
  public Mono<String> hmset(K k, Map<K, V> map) {
    return null;
  }

  @Override
  public Mono<K> hrandfield(K k) {
    return null;
  }

  @Override
  public Flux<K> hrandfield(K k, long l) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, V>> hrandfieldWithvalues(K k) {
    return null;
  }

  @Override
  public Flux<KeyValue<K, V>> hrandfieldWithvalues(K k, long l) {
    return null;
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k) {
    return null;
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<MapScanCursor<K, V>> hscan(K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> hscanNovalues(K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscan(
      KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> hscanNovalues(
      KeyStreamingChannel<K> keyStreamingChannel, K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<Boolean> hset(K k, K k1, V v) {
    return null;
  }

  @Override
  public Mono<Long> hset(K k, Map<K, V> map) {
    return null;
  }

  @Override
  public Mono<Boolean> hsetnx(K k, K k1, V v) {
    return null;
  }

  @Override
  public Mono<Long> hstrlen(K k, K k1) {
    return null;
  }

  @Override
  public Flux<V> hvals(K k) {
    return null;
  }

  @Override
  public Mono<Long> hvals(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Flux<Long> hexpire(K k, long l, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpire(K k, long l, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpire(K k, Duration duration, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpire(K k, Duration duration, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, long l, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, long l, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, Date date, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, Date date, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, Instant instant, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpireat(K k, Instant instant, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hexpiretime(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpersist(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpire(K k, long l, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpire(K k, long l, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpire(K k, Duration duration, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpire(K k, Duration duration, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, long l, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, long l, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, Date date, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, Date date, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, Instant instant, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpireat(K k, Instant instant, ExpireArgs expireArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpexpiretime(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> httl(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<Long> hpttl(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Boolean> copy(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Boolean> copy(K k, K k1, CopyArgs copyArgs) {
    return null;
  }

  @Override
  public Mono<Long> del(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> unlink(K... ks) {
    return null;
  }

  @Override
  public Mono<byte[]> dump(K k) {
    return null;
  }

  @Override
  public Mono<Long> exists(K... ks) {
    return null;
  }

  @Override
  public Mono<Boolean> expire(K k, long l) {
    return null;
  }

  @Override
  public Mono<Boolean> expire(K k, long l, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> expire(K k, Duration duration) {
    return null;
  }

  @Override
  public Mono<Boolean> expire(K k, Duration duration, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, long l) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, long l, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, Date date) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, Date date, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, Instant instant) {
    return null;
  }

  @Override
  public Mono<Boolean> expireat(K k, Instant instant, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Long> expiretime(K k) {
    return null;
  }

  @Override
  public Flux<K> keys(K k) {
    return null;
  }

  @Override
  public Mono<Long> keys(KeyStreamingChannel<K> keyStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<String> migrate(String s, int i, K k, int i1, long l) {
    return null;
  }

  @Override
  public Mono<String> migrate(String s, int i, int i1, long l, MigrateArgs<K> migrateArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> move(K k, int i) {
    return null;
  }

  @Override
  public Mono<String> objectEncoding(K k) {
    return null;
  }

  @Override
  public Mono<Long> objectFreq(K k) {
    return null;
  }

  @Override
  public Mono<Long> objectIdletime(K k) {
    return null;
  }

  @Override
  public Mono<Long> objectRefcount(K k) {
    return null;
  }

  @Override
  public Mono<Boolean> persist(K k) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpire(K k, long l) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpire(K k, long l, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpire(K k, Duration duration) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpire(K k, Duration duration, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, long l) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, long l, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Date date) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Date date, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Instant instant) {
    return null;
  }

  @Override
  public Mono<Boolean> pexpireat(K k, Instant instant, ExpireArgs expireArgs) {
    return null;
  }

  @Override
  public Mono<Long> pexpiretime(K k) {
    return null;
  }

  @Override
  public Mono<Long> pttl(K k) {
    return null;
  }

  @Override
  public Mono<K> randomkey() {
    return null;
  }

  @Override
  public Mono<String> rename(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Boolean> renamenx(K k, K k1) {
    return null;
  }

  @Override
  public Mono<String> restore(K k, long l, byte[] bytes) {
    return null;
  }

  @Override
  public Mono<String> restore(K k, byte[] bytes, RestoreArgs restoreArgs) {
    return null;
  }

  @Override
  public Flux<V> sort(K k) {
    return null;
  }

  @Override
  public Mono<Long> sort(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Flux<V> sort(K k, SortArgs sortArgs) {
    return null;
  }

  @Override
  public Mono<Long> sort(ValueStreamingChannel<V> valueStreamingChannel, K k, SortArgs sortArgs) {
    return null;
  }

  @Override
  public Flux<V> sortReadOnly(K k) {
    return null;
  }

  @Override
  public Mono<Long> sortReadOnly(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Flux<V> sortReadOnly(K k, SortArgs sortArgs) {
    return null;
  }

  @Override
  public Mono<Long> sortReadOnly(
      ValueStreamingChannel<V> valueStreamingChannel, K k, SortArgs sortArgs) {
    return null;
  }

  @Override
  public Mono<Long> sortStore(K k, SortArgs sortArgs, K k1) {
    return null;
  }

  @Override
  public Mono<Long> touch(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> ttl(K k) {
    return null;
  }

  @Override
  public Mono<String> type(K k) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> scan() {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<KeyScanCursor<K>> scan(ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> scan(KeyStreamingChannel<K> keyStreamingChannel) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> scan(
      KeyStreamingChannel<K> keyStreamingChannel, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<V> blmove(K k, K k1, LMoveArgs lMoveArgs, long l) {
    return null;
  }

  @Override
  public Mono<V> blmove(K k, K k1, LMoveArgs lMoveArgs, double v) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<V>>> blmpop(long l, LMPopArgs lmPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<V>>> blmpop(double v, LMPopArgs lmPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, V>> blpop(long l, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, V>> blpop(double v, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, V>> brpop(long l, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, V>> brpop(double v, K... ks) {
    return null;
  }

  @Override
  public Mono<V> brpoplpush(long l, K k, K k1) {
    return null;
  }

  @Override
  public Mono<V> brpoplpush(double v, K k, K k1) {
    return null;
  }

  @Override
  public Mono<V> lindex(K k, long l) {
    return null;
  }

  @Override
  public Mono<Long> linsert(K k, boolean b, V v, V v1) {
    return null;
  }

  @Override
  public Mono<Long> llen(K k) {
    return null;
  }

  @Override
  public Mono<V> lmove(K k, K k1, LMoveArgs lMoveArgs) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<V>>> lmpop(LMPopArgs lmPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<V> lpop(K k) {
    return null;
  }

  @Override
  public Flux<V> lpop(K k, long l) {
    return null;
  }

  @Override
  public Mono<Long> lpos(K k, V v) {
    return null;
  }

  @Override
  public Mono<Long> lpos(K k, V v, LPosArgs lPosArgs) {
    return null;
  }

  @Override
  public Flux<Long> lpos(K k, V v, int i) {
    return null;
  }

  @Override
  public Flux<Long> lpos(K k, V v, int i, LPosArgs lPosArgs) {
    return null;
  }

  @Override
  public Mono<Long> lpush(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<Long> lpushx(K k, V... vs) {
    return null;
  }

  @Override
  public Flux<V> lrange(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> lrange(ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> lrem(K k, long l, V v) {
    return null;
  }

  @Override
  public Mono<String> lset(K k, long l, V v) {
    return null;
  }

  @Override
  public Mono<String> ltrim(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<V> rpop(K k) {
    return null;
  }

  @Override
  public Flux<V> rpop(K k, long l) {
    return null;
  }

  @Override
  public Mono<V> rpoplpush(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Long> rpush(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<Long> rpushx(K k, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> eval(String s, ScriptOutputType scriptOutputType, K... ks) {
    return null;
  }

  @Override
  public <T> Flux<T> eval(byte[] bytes, ScriptOutputType scriptOutputType, K... ks) {
    return null;
  }

  @Override
  public <T> Flux<T> eval(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> eval(byte[] bytes, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> evalReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> evalReadOnly(
      byte[] bytes, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> evalsha(String s, ScriptOutputType scriptOutputType, K... ks) {
    return null;
  }

  @Override
  public <T> Flux<T> evalsha(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public <T> Flux<T> evalshaReadOnly(String s, ScriptOutputType scriptOutputType, K[] ks, V... vs) {
    return null;
  }

  @Override
  public Flux<Boolean> scriptExists(String... strings) {
    return null;
  }

  @Override
  public Mono<String> scriptFlush() {
    return null;
  }

  @Override
  public Mono<String> scriptFlush(FlushMode flushMode) {
    return null;
  }

  @Override
  public Mono<String> scriptKill() {
    return null;
  }

  @Override
  public Mono<String> scriptLoad(String s) {
    return null;
  }

  @Override
  public Mono<String> scriptLoad(byte[] bytes) {
    return null;
  }

  @Override
  public String digest(String s) {
    return "";
  }

  @Override
  public String digest(byte[] bytes) {
    return "";
  }

  @Override
  public Mono<String> bgrewriteaof() {
    return null;
  }

  @Override
  public Mono<String> bgsave() {
    return null;
  }

  @Override
  public Mono<String> clientCaching(boolean b) {
    return null;
  }

  @Override
  public Mono<K> clientGetname() {
    return null;
  }

  @Override
  public Mono<Long> clientGetredir() {
    return null;
  }

  @Override
  public Mono<Long> clientId() {
    return null;
  }

  @Override
  public Mono<String> clientKill(String s) {
    return null;
  }

  @Override
  public Mono<Long> clientKill(KillArgs killArgs) {
    return null;
  }

  @Override
  public Mono<String> clientList() {
    return null;
  }

  @Override
  public Mono<String> clientList(ClientListArgs clientListArgs) {
    return null;
  }

  @Override
  public Mono<String> clientInfo() {
    return null;
  }

  @Override
  public Mono<String> clientNoEvict(boolean b) {
    return null;
  }

  @Override
  public Mono<String> clientPause(long l) {
    return null;
  }

  @Override
  public Mono<String> clientSetname(K k) {
    return null;
  }

  @Override
  public Mono<String> clientSetinfo(String s, String s1) {
    return null;
  }

  @Override
  public Mono<String> clientTracking(TrackingArgs trackingArgs) {
    return null;
  }

  @Override
  public Mono<Long> clientUnblock(long l, UnblockType unblockType) {
    return null;
  }

  @Override
  public Flux<Object> command() {
    return null;
  }

  @Override
  public Mono<Long> commandCount() {
    return null;
  }

  @Override
  public Flux<Object> commandInfo(String... strings) {
    return null;
  }

  @Override
  public Flux<Object> commandInfo(CommandType... commandTypes) {
    return null;
  }

  @Override
  public Mono<Map<String, String>> configGet(String s) {
    return null;
  }

  @Override
  public Mono<Map<String, String>> configGet(String... strings) {
    return null;
  }

  @Override
  public Mono<String> configResetstat() {
    return null;
  }

  @Override
  public Mono<String> configRewrite() {
    return null;
  }

  @Override
  public Mono<String> configSet(String s, String s1) {
    return null;
  }

  @Override
  public Mono<String> configSet(Map<String, String> map) {
    return null;
  }

  @Override
  public Mono<Long> dbsize() {
    return null;
  }

  @Override
  public Mono<String> debugCrashAndRecover(Long aLong) {
    return null;
  }

  @Override
  public Mono<String> debugHtstats(int i) {
    return null;
  }

  @Override
  public Mono<String> debugObject(K k) {
    return null;
  }

  @Override
  public Mono<Void> debugOom() {
    return null;
  }

  @Override
  public Mono<String> debugReload() {
    return null;
  }

  @Override
  public Mono<String> debugRestart(Long aLong) {
    return null;
  }

  @Override
  public Mono<String> debugSdslen(K k) {
    return null;
  }

  @Override
  public Mono<Void> debugSegfault() {
    return null;
  }

  @Override
  public Mono<String> flushall() {
    return null;
  }

  @Override
  public Mono<String> flushall(FlushMode flushMode) {
    return null;
  }

  @Override
  public Mono<String> flushallAsync() {
    return null;
  }

  @Override
  public Mono<String> flushdb() {
    return null;
  }

  @Override
  public Mono<String> flushdb(FlushMode flushMode) {
    return null;
  }

  @Override
  public Mono<String> flushdbAsync() {
    return null;
  }

  @Override
  public Mono<String> info() {
    return null;
  }

  @Override
  public Mono<String> info(String s) {
    return null;
  }

  @Override
  public Mono<Date> lastsave() {
    return null;
  }

  @Override
  public Mono<Long> memoryUsage(K k) {
    return null;
  }

  @Override
  public Mono<String> replicaof(String s, int i) {
    return null;
  }

  @Override
  public Mono<String> replicaofNoOne() {
    return null;
  }

  @Override
  public Mono<String> save() {
    return null;
  }

  @Override
  public Mono<Void> shutdown(boolean b) {
    return null;
  }

  @Override
  public Mono<Void> shutdown(ShutdownArgs shutdownArgs) {
    return null;
  }

  @Override
  public Mono<String> slaveof(String s, int i) {
    return null;
  }

  @Override
  public Mono<String> slaveofNoOne() {
    return null;
  }

  @Override
  public Flux<Object> slowlogGet() {
    return null;
  }

  @Override
  public Flux<Object> slowlogGet(int i) {
    return null;
  }

  @Override
  public Mono<Long> slowlogLen() {
    return null;
  }

  @Override
  public Mono<String> slowlogReset() {
    return null;
  }

  @Override
  public Flux<V> time() {
    return null;
  }

  @Override
  public Mono<Long> sadd(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<Long> scard(K k) {
    return null;
  }

  @Override
  public Flux<V> sdiff(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sdiff(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sdiffstore(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<V> sinter(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sinter(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sintercard(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sintercard(long l, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sinterstore(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Boolean> sismember(K k, V v) {
    return null;
  }

  @Override
  public Flux<V> smembers(K k) {
    return null;
  }

  @Override
  public Mono<Long> smembers(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Flux<Boolean> smismember(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<Boolean> smove(K k, K k1, V v) {
    return null;
  }

  @Override
  public Mono<V> spop(K k) {
    return null;
  }

  @Override
  public Flux<V> spop(K k, long l) {
    return null;
  }

  @Override
  public Mono<V> srandmember(K k) {
    return null;
  }

  @Override
  public Flux<V> srandmember(K k, long l) {
    return null;
  }

  @Override
  public Mono<Long> srandmember(ValueStreamingChannel<V> valueStreamingChannel, K k, long l) {
    return null;
  }

  @Override
  public Mono<Long> srem(K k, V... vs) {
    return null;
  }

  @Override
  public Flux<V> sunion(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sunion(ValueStreamingChannel<V> valueStreamingChannel, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> sunionstore(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k) {
    return null;
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<ValueScanCursor<V>> sscan(K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> sscan(ValueStreamingChannel<V> valueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel, K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> sscan(
      ValueStreamingChannel<V> valueStreamingChannel, K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzmpop(long l, ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> bzmpop(
      long l, long l1, ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzmpop(double v, ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> bzmpop(
      double v, int i, ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmin(long l, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmin(double v, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmax(long l, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> bzpopmax(double v, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, double v, V v1) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, Object... objects) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, ScoredValue<V>... scoredValues) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, double v, V v1) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, Object... objects) {
    return null;
  }

  @Override
  public Mono<Long> zadd(K k, ZAddArgs zAddArgs, ScoredValue<V>... scoredValues) {
    return null;
  }

  @Override
  public Mono<Double> zaddincr(K k, double v, V v1) {
    return null;
  }

  @Override
  public Mono<Double> zaddincr(K k, ZAddArgs zAddArgs, double v, V v1) {
    return null;
  }

  @Override
  public Mono<Long> zcard(K k) {
    return null;
  }

  @Override
  public Mono<Long> zcount(K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zcount(K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zcount(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<V> zdiff(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zdiffstore(K k, K... ks) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zdiffWithScores(K... ks) {
    return null;
  }

  @Override
  public Mono<Double> zincrby(K k, double v, V v1) {
    return null;
  }

  @Override
  public Flux<V> zinter(K... ks) {
    return null;
  }

  @Override
  public Flux<V> zinter(ZAggregateArgs zAggregateArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zintercard(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zintercard(long l, K... ks) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zinterWithScores(ZAggregateArgs zAggregateArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zinterWithScores(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zinterstore(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zinterstore(K k, ZStoreArgs zStoreArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zlexcount(K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zlexcount(K k, Range<? extends V> range) {
    return null;
  }

  @Override
  public Mono<List<Double>> zmscore(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, ScoredValue<V>>> zmpop(ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<KeyValue<K, List<ScoredValue<V>>>> zmpop(int i, ZPopArgs zPopArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<ScoredValue<V>> zpopmin(K k) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zpopmin(K k, long l) {
    return null;
  }

  @Override
  public Mono<ScoredValue<V>> zpopmax(K k) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zpopmax(K k, long l) {
    return null;
  }

  @Override
  public Mono<V> zrandmember(K k) {
    return null;
  }

  @Override
  public Flux<V> zrandmember(K k, long l) {
    return null;
  }

  @Override
  public Mono<ScoredValue<V>> zrandmemberWithScores(K k) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrandmemberWithScores(K k, long l) {
    return null;
  }

  @Override
  public Flux<V> zrange(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrange(ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangeWithScores(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrangeWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrangebylex(K k, String s, String s1) {
    return null;
  }

  @Override
  public Flux<V> zrangebylex(K k, Range<? extends V> range) {
    return null;
  }

  @Override
  public Flux<V> zrangebylex(K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrangebylex(K k, Range<? extends V> range, Limit limit) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, double v, double v1) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, String s, String s1) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrangebyscore(K k, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, double v, double v1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, String s, String s1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrangebyscoreWithScores(
      K k, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      double v,
      double v1,
      long l,
      long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      String s,
      String s1,
      long l,
      long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrangestore(K k, K k1, Range<Long> range) {
    return null;
  }

  @Override
  public Mono<Long> zrangestorebylex(K k, K k1, Range<? extends V> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrangestorebyscore(K k, K k1, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrank(K k, V v) {
    return null;
  }

  @Override
  public Mono<ScoredValue<Long>> zrankWithScore(K k, V v) {
    return null;
  }

  @Override
  public Mono<Long> zrem(K k, V... vs) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebylex(K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebylex(K k, Range<? extends V> range) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebyrank(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zremrangebyscore(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<V> zrevrange(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrange(
      ValueStreamingChannel<V> valueStreamingChannel, K k, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangeWithScores(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangeWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebylex(K k, Range<? extends V> range) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebylex(K k, Range<? extends V> range, Limit limit) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, double v, double v1) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, String s, String s1) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<V> zrevrangebyscore(K k, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel, K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscore(
      ValueStreamingChannel<V> valueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, double v, double v1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, String s, String s1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(K k, Range<? extends Number> range) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, double v, double v1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, String s, String s1, long l, long l1) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zrevrangebyscoreWithScores(
      K k, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, double v, double v1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, String s, String s1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      double v,
      double v1,
      long l,
      long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      String s,
      String s1,
      long l,
      long l1) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangebyscoreWithScores(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      Range<? extends Number> range,
      Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangestore(K k, K k1, Range<Long> range) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangestorebylex(K k, K k1, Range<? extends V> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrevrangestorebyscore(K k, K k1, Range<? extends Number> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> zrevrank(K k, V v) {
    return null;
  }

  @Override
  public Mono<ScoredValue<Long>> zrevrankWithScore(K k, V v) {
    return null;
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k) {
    return null;
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanCursor scanCursor, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<ScoredValueScanCursor<V>> zscan(K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel,
      K k,
      ScanCursor scanCursor,
      ScanArgs scanArgs) {
    return null;
  }

  @Override
  public Mono<StreamScanCursor> zscan(
      ScoredValueStreamingChannel<V> scoredValueStreamingChannel, K k, ScanCursor scanCursor) {
    return null;
  }

  @Override
  public Mono<Double> zscore(K k, V v) {
    return null;
  }

  @Override
  public Flux<V> zunion(K... ks) {
    return null;
  }

  @Override
  public Flux<V> zunion(ZAggregateArgs zAggregateArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zunionWithScores(ZAggregateArgs zAggregateArgs, K... ks) {
    return null;
  }

  @Override
  public Flux<ScoredValue<V>> zunionWithScores(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zunionstore(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> zunionstore(K k, ZStoreArgs zStoreArgs, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> xack(K k, K k1, String... strings) {
    return null;
  }

  @Override
  public Mono<String> xadd(K k, Map<K, V> map) {
    return null;
  }

  @Override
  public Mono<String> xadd(K k, XAddArgs xAddArgs, Map<K, V> map) {
    return null;
  }

  @Override
  public Mono<String> xadd(K k, Object... objects) {
    return null;
  }

  @Override
  public Mono<String> xadd(K k, XAddArgs xAddArgs, Object... objects) {
    return null;
  }

  @Override
  public Mono<ClaimedMessages<K, V>> xautoclaim(K k, XAutoClaimArgs<K> xAutoClaimArgs) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xclaim(K k, Consumer<K> consumer, long l, String... strings) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xclaim(
      K k, Consumer<K> consumer, XClaimArgs xClaimArgs, String... strings) {
    return null;
  }

  @Override
  public Mono<Long> xdel(K k, String... strings) {
    return null;
  }

  @Override
  public Mono<String> xgroupCreate(XReadArgs.StreamOffset<K> streamOffset, K k) {
    return null;
  }

  @Override
  public Mono<String> xgroupCreate(
      XReadArgs.StreamOffset<K> streamOffset, K k, XGroupCreateArgs xGroupCreateArgs) {
    return null;
  }

  @Override
  public Mono<Boolean> xgroupCreateconsumer(K k, Consumer<K> consumer) {
    return null;
  }

  @Override
  public Mono<Long> xgroupDelconsumer(K k, Consumer<K> consumer) {
    return null;
  }

  @Override
  public Mono<Boolean> xgroupDestroy(K k, K k1) {
    return null;
  }

  @Override
  public Mono<String> xgroupSetid(XReadArgs.StreamOffset<K> streamOffset, K k) {
    return null;
  }

  @Override
  public Flux<Object> xinfoStream(K k) {
    return null;
  }

  @Override
  public Flux<Object> xinfoGroups(K k) {
    return null;
  }

  @Override
  public Flux<Object> xinfoConsumers(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Long> xlen(K k) {
    return null;
  }

  @Override
  public Mono<PendingMessages> xpending(K k, K k1) {
    return null;
  }

  @Override
  public Flux<PendingMessage> xpending(K k, K k1, Range<String> range, Limit limit) {
    return null;
  }

  @Override
  public Flux<PendingMessage> xpending(
      K k, Consumer<K> consumer, Range<String> range, Limit limit) {
    return null;
  }

  @Override
  public Flux<PendingMessage> xpending(K k, XPendingArgs<K> xPendingArgs) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xrange(K k, Range<String> range) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xrange(K k, Range<String> range, Limit limit) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xread(XReadArgs.StreamOffset<K>... streamOffsets) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xread(
      XReadArgs xReadArgs, XReadArgs.StreamOffset<K>... streamOffsets) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xreadgroup(
      Consumer<K> consumer, XReadArgs.StreamOffset<K>... streamOffsets) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xreadgroup(
      Consumer<K> consumer, XReadArgs xReadArgs, XReadArgs.StreamOffset<K>... streamOffsets) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xrevrange(K k, Range<String> range) {
    return null;
  }

  @Override
  public Flux<StreamMessage<K, V>> xrevrange(K k, Range<String> range, Limit limit) {
    return null;
  }

  @Override
  public Mono<Long> xtrim(K k, long l) {
    return null;
  }

  @Override
  public Mono<Long> xtrim(K k, boolean b, long l) {
    return null;
  }

  @Override
  public Mono<Long> xtrim(K k, XTrimArgs xTrimArgs) {
    return null;
  }

  @Override
  public Mono<Long> append(K k, V v) {
    return null;
  }

  @Override
  public Mono<Long> bitcount(K k) {
    return null;
  }

  @Override
  public Mono<Long> bitcount(K k, long l, long l1) {
    return null;
  }

  @Override
  public Flux<Value<Long>> bitfield(K k, BitFieldArgs bitFieldArgs) {
    return null;
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b) {
    return null;
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b, long l) {
    return null;
  }

  @Override
  public Mono<Long> bitpos(K k, boolean b, long l, long l1) {
    return null;
  }

  @Override
  public Mono<Long> bitopAnd(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> bitopNot(K k, K k1) {
    return null;
  }

  @Override
  public Mono<Long> bitopOr(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> bitopXor(K k, K... ks) {
    return null;
  }

  @Override
  public Mono<Long> decr(K k) {
    return null;
  }

  @Override
  public Mono<Long> decrby(K k, long l) {
    return null;
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
                return Mono.error(MomentoLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoLettuceExceptionMapper.unexpectedResponseException(
                        getResponse.toString()));
              }
            });
  }

  @Override
  public Mono<Long> getbit(K k, long l) {
    return null;
  }

  @Override
  public Mono<V> getdel(K k) {
    return null;
  }

  @Override
  public Mono<V> getex(K k, GetExArgs getExArgs) {
    return null;
  }

  @Override
  public Mono<V> getrange(K k, long l, long l1) {
    return null;
  }

  @Override
  public Mono<V> getset(K k, V v) {
    return null;
  }

  @Override
  public Mono<Long> incr(K k) {
    return null;
  }

  @Override
  public Mono<Long> incrby(K k, long l) {
    return null;
  }

  @Override
  public Mono<Double> incrbyfloat(K k, double v) {
    return null;
  }

  @Override
  public Flux<KeyValue<K, V>> mget(K... ks) {
    return null;
  }

  @Override
  public Mono<Long> mget(KeyValueStreamingChannel<K, V> keyValueStreamingChannel, K... ks) {
    return null;
  }

  @Override
  public Mono<String> mset(Map<K, V> map) {
    return null;
  }

  @Override
  public Mono<Boolean> msetnx(Map<K, V> map) {
    return null;
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
                return Mono.error(MomentoLettuceExceptionMapper.mapException(error));
              } else {
                return Mono.error(
                    MomentoLettuceExceptionMapper.unexpectedResponseException(
                        setResponse.toString()));
              }
            });
  }

  @Override
  public Mono<String> set(K k, V v, SetArgs setArgs) {
    return null;
  }

  @Override
  public Mono<V> setGet(K k, V v) {
    return null;
  }

  @Override
  public Mono<V> setGet(K k, V v, SetArgs setArgs) {
    return null;
  }

  @Override
  public Mono<Long> setbit(K k, long l, int i) {
    return null;
  }

  @Override
  public Mono<String> setex(K k, long l, V v) {
    return null;
  }

  @Override
  public Mono<String> psetex(K k, long l, V v) {
    return null;
  }

  @Override
  public Mono<Boolean> setnx(K k, V v) {
    return null;
  }

  @Override
  public Mono<Long> setrange(K k, long l, V v) {
    return null;
  }

  @Override
  public Mono<StringMatchResult> stralgoLcs(StrAlgoArgs strAlgoArgs) {
    return null;
  }

  @Override
  public Mono<Long> strlen(K k) {
    return null;
  }

  @Override
  public Mono<String> discard() {
    return null;
  }

  @Override
  public Mono<TransactionResult> exec() {
    return null;
  }

  @Override
  public Mono<String> multi() {
    return null;
  }

  @Override
  public Mono<String> watch(K... ks) {
    return null;
  }

  @Override
  public Mono<String> unwatch() {
    return null;
  }
}
