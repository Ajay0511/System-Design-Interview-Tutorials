# Redis Internal Working (Shareable Notes)

## 1) What is Redis
Redis (**Remote Dictionary Server**) is an in-memory data structure store used as:
- **Cache**
- **Database**
- **Message broker**
- **Streaming engine**

Because data is stored in **RAM**, Redis is very fast.

**Typical performance:**
- **Latency:** under 1 ms
- **Throughput:** 100k+ operations/second

## 2) High-Level Architecture

**Client**
↓
**TCP Connection**
↓
**Redis Server**
↓
**Event Loop** (single-threaded command execution)
↓
**In-Memory Data Store**
↓
**Persistence Layer** (RDB / AOF)

**Main components:**
- **Event loop:** handles client requests
- **Memory store:** stores all data
- **Persistence:** writes data to disk
- **Replication:** sync from primary to replica
- **Cluster:** distributes data across nodes

## 3) Request Flow

**Step 1: Client sends command**
- **Example:** `SET user:1 "Ajay"`
- Redis receives commands over TCP using **RESP** protocol.

**Step 2: I/O multiplexing**
Redis handles many connections using OS mechanisms like:
- `epoll` (Linux)
- `kqueue` (macOS)
- `select/poll`

**Step 3: Event loop**
Redis processes requests in an event-driven loop:
- read request
- parse command
- execute
- send response

**Step 4: Command parsing**
For `SET user:1 Ajay`, Redis identifies:
- **Command:** `SET`
- **Key:** `user:1`
- **Value:** `Ajay`

**Step 5: Command execution**
Redis executes using in-memory data structures (mainly hash tables).
Most common operations are **O(1)**.

## 4) Persistence Options

Redis keeps data in RAM but has mechanisms to persist data to disk for durability.

### 4.1 RDB (snapshot)
- Periodically saves the entire dataset to an RDB file based on `save` directives (e.g. `save 900 1` means save if at least 1 write occurs in 900 seconds).
- During a snapshot Redis **forks**: the child writes to disk while the parent continues serving clients.
- **Pros:** compact backups, fast restarts.
- **Cons:** potential data loss between snapshots; not crash‑proof.

### 4.2 AOF (append-only file)
- Appends every write command to a log that is replayed on restart.
- Example log:
  ```
  SET user:1 Ajay
  SET user:2 Rahul
  DEL user:1
  ```
- Append‑fsync modes:
  - `appendfsync always` – flush on every command (safest, slowest).
  - `appendfsync everysec` – flush every second (default, good trade‑off).
  - `appendfsync no` – rely on the OS (fastest but risky).
- Recommended mode: **everysec** (max ≈1 second data loss).
- A background rewrite (`BGREWRITEAOF`) compacts the file to prevent unbounded growth.
- **Pros:** fine‑grained durability; near‑zero data loss possible.
- **Cons:** larger disk / I/O overhead.

### 4.3 Using RDB and AOF Together
- Common production practice: enable both mechanisms.
- RDB provides fast restarts and compact backups; AOF ensures durability in between snapshots.
- During an AOF rewrite, Redis can use the latest RDB snapshot as the base to speed recovery.


## 5) Replication

Redis supports asynchronous primary–replica replication for read scaling and redundancy.

- The primary (master) accepts all writes; replicas copy the write stream.
- Replicas can be promoted to primary during failover (manually or via Sentinel).
- **Initial sync process:**
  1. Replica connects to primary.
  2. Primary sends an RDB snapshot.
  3. Replica loads the snapshot.
  4. Primary streams subsequent write commands.
- Replication is non‑blocking for clients and uses replication buffers.
- Partial resynchronization (PSYNC) allows faster reconnection without a full sync.

Architecture example:
```
        Primary
        /   \
   Replica1 Replica2
```

Since replication is asynchronous, replicas may lag behind the primary.


## 6) Response

After execution, Redis returns response to client.
- **Example for SET command:** `OK`

## 7) Why Redis Uses Single-Threaded Command Execution

- No lock contention for command execution
- No thread context-switch overhead in core execution path
- Predictable latency
- In-memory **O(1)** operations are already very fast

## 8) Redis 6+ Update

Redis introduced **multi-threading for network I/O** (read/write sockets), while command execution remains single-threaded for consistency and simplicity.

## 9) End-to-End Summary

**Client command**
→ TCP/RESP
→ I/O multiplexer
→ event loop
→ command parse
→ in-memory execution
→ persistence/replication (if enabled)
→ response to client

## 10) Redis Core Data Structures

Redis is not just a key-value store. It supports multiple optimized data structures.

### 10.1 String
- Most basic Redis type, can store strings, integers, or binary data up to 512 MB.
- **Example:** `SET user:1 "Ajay"`
- Internally uses **SDS (Simple Dynamic String)** for efficient memory management.

**Why SDS over C strings:**
- **O(1)** length lookup (avoids `strlen()` scans).
- Binary-safe: handles null bytes without truncation.
- Efficient appends: pre-allocates space to reduce reallocations.
- Prevents buffer overflows with bounds checking.

**SDS structure:**
```c
struct SDS {
    int len;      // Used length
    int free;     // Available space
    char buf[];   // Data buffer
};
```
- Space-efficient for small strings; automatically resizes as needed.

### 10.2 Hash
- Stores field-value pairs, ideal for representing objects like user profiles.
- **Example:** `HSET user:1 name Ajay age 25 city Mumbai`
- **Internal encodings:**
  - **ziplist**: Compact array for small hashes (< 512 fields, avg field/value < 64 bytes).
  - **hashtable**: Standard hash table for larger hashes.
- Redis automatically switches encodings based on size thresholds to optimize memory and performance.
- Operations like `HGET`, `HSET` are O(1); `HGETALL` is O(N).

### 10.3 List
- Doubly-linked list of strings, ordered by insertion.
- **Examples:** 
  - `LPUSH queue job1` (add to left)
  - `RPUSH queue job2` (add to right)
  - `LPOP queue` (remove from left)
- Implemented using **quicklist**: a hybrid of ziplist and linked list nodes.
  - Each node is a ziplist (compressed list) for memory efficiency.
  - Balances memory usage and performance for large lists.
- Ideal for queues, stacks, or capped collections (e.g., recent items).
- Operations: O(1) for ends, O(N) for middle access.

### 10.4 Set
- Unordered collection of unique strings.
- **Example:** `SADD users Ajay Rahul Priya` (adds unique members)
- Internally uses a hash table for O(1) membership tests.
- Supports set operations: `SINTER` (intersection), `SUNION` (union), `SDIFF` (difference).
- Useful for tags, unique visitors, or deduplication.
- Memory efficient; no duplicates allowed.

### 10.5 Sorted Set (ZSET)
- Ordered set with scores; elements sorted by score (ascending).
- **Example:** `ZADD leaderboard 100 Ajay 200 Rahul 150 Priya`
- Internally: dual structure for efficiency.
  - Hash map for O(1) score lookups by member.
  - Skip list for O(log N) ordered operations.
- Supports range queries: `ZRANGE leaderboard 0 10` (by rank), `ZRANGEBYSCORE leaderboard 100 200` (by score).
- Ideal for leaderboards, priority queues, or time-series data.
- Complexity: O(log N) for inserts/deletes, O(1) for score access.

### 10.6 Bitmap
- Space-efficient bit arrays for boolean flags.
- **Example:** `SETBIT online_users 12345 1` (mark user 12345 as online)
- Built on Redis strings; each bit represents a state.
- Operations: `GETBIT`, `BITCOUNT`, `BITOP` (AND/OR/XOR/NOT).
- Memory usage: ~1 MB for 8 million bits.
- Perfect for analytics like daily active users, feature flags, or bloom filters.

### 10.7 HyperLogLog
- Probabilistic data structure for estimating unique elements.
- **Example:** `PFADD visitors user1 user2 user3`, `PFCOUNT visitors` (returns ~3)
- Uses stochastic averaging; error rate ~0.81% with 12 KB fixed memory.
- Mergeable: `PFMERGE` combines multiple HLLs.
- Scalable for billions of elements; trades accuracy for memory.

### 10.8 Streams
- Append-only log of entries (key-value pairs) for event sourcing.
- **Example:** `XADD orders * product iphone qty 2 user ajay`
- Consumer groups: `XGROUP CREATE orders group1 0` for distributed processing.
- Commands: `XREAD` (blocking read), `XPENDING` (pending messages), `XACK` (acknowledge).
- Durable and ordered; supports backlogs and trimming.
- Replaces Pub/Sub for reliable messaging; integrates with Redis modules.










## Redis Sentinel
Sentinel provides high availability.

Sentinels run in a distributed quorum and use a gossip protocol to coordinate; a majority of sentinels must agree before a failover occurs.

Responsibilities:
monitoring
failover
configuration

Architecture:
   Sentinel1
   Sentinel2
   Sentinel3
        |
      Master
        |
     Replicas

Failover flow:
Master fails
   ↓
Sentinel detects
   ↓
Elect new master
   ↓
Update replicas


## Redis Cluster (Horizontal Scaling)
Redis cluster supports sharding.
Data distributed using hash slots.

Total slots:
16384

Example:
hash(key) % 16384

Clients typically compute the slot using CRC16(key) % 16384; the cluster gossip protocol shares slot ownership and allows slots to migrate during resharding.

Cluster example:
Node1 -> slots 0–5000
Node2 -> slots 5001–10000
Node3 -> slots 10001–16383

Client Routing
Client calculates slot.
Then sends request to correct node.


## Redis Memory Management
Redis memory components:
Redis
 ├─ Data
 ├─ Replication buffers
 ├─ AOF buffers
 └─ Client buffers

Configuration exercises control via `maxmemory` and `maxmemory-policy`; fragmentation is tracked with the `INFO` command.

Eviction Policies
When memory is full:
Policies:
noeviction
allkeys-lru
volatile-lru
allkeys-random
volatile-ttl

Most common:
allkeys-lru

## Redis Transactions

Commands:
MULTI
EXEC
DISCARD
WATCH

Example:
MULTI
SET a 10
SET b 20
EXEC

Redis transactions are:
Atomic
But NOT rollback capable


## Redis Pub/Sub

Message system.
Example:
Publisher:
PUBLISH news "hello"

Subscriber:
SUBSCRIBE news

Used for:
notifications
chat systems


## Redis Streams (Modern Messaging)

Example:
XADD orders * item iphone

Consumer groups:
XGROUP CREATE orders group1

Used for:
event driven systems
distributed processing

## Redis Production Use Cases
1. Caching Layer
Application
   ↓
Redis Cache
   ↓
Database
Example:
user profile cache

2. Rate Limiting
Example:
INCR api:user1
EXPIRE 60


3. Distributed Locks
Example:
SET lock_key value NX PX 30000
Used for:
microservice coordination

4. Session Store
Used by:
web applications
authentication systems

5. Leaderboards
Using:
Sorted Sets

6. Job Queues
Using:
Lists / Streams


## Redis vs Memcached
| Feature     | Redis | Memcached   |
| ----------- | ----- | ----------- |
| Data types  | Many  | String only |
| Persistence | Yes   | No          |
| Replication | Yes   | No          |
| Clustering  | Yes   | Limited     |


## Redis Limitations
Memory expensive
Single threaded CPU bound
Not ideal for complex queries
Large dataset expensive


## Redis Scaling Strategies
1️⃣ Vertical scaling
More RAM.

2️⃣ Redis cluster
Horizontal scaling.

3️⃣ Sharding
Manual key distribution.


## Redis Common Interview Questions
Q1 Why Redis is single threaded?

Answer:

avoid locking

reduce context switching

predictable performance

Follow up:

Redis 6 added multi-threaded IO
Q2 How Redis handles millions of connections?

Using:

epoll / kqueue

Event driven model.

Q3 How Redis ensures durability?

Using:

RDB
AOF
Q4 What happens if Redis crashes?

Recovery from:

AOF replay
or
RDB snapshot
Q5 How Redis cluster handles failures?

replicas

failover

slot migration

Q6 Difference between Redis Cluster and Sentinel
Feature	Sentinel	Cluster
Scaling	No	Yes
HA	Yes	Yes
Sharding	No	Yes
## Redis Production Architecture Example

Large scale system:

            Load Balancer
                 |
             Application
                 |
              Redis
        (Cluster + Sentinel)
           /      |      \
        Node1   Node2   Node3
           |
         Replica
## Redis Monitoring

Tools:

INFO
MONITOR
SLOWLOG

Metrics:

memory usage

latency

ops/sec

## Advanced Redis Topics (Architect Level)

Must know:

Redis Lua scripting

Redis pipelining

Redis cluster internals

distributed locks (RedLock)

Redis eviction algorithms

Redis memory fragmentation

Redis modules

## Redis Pipelining

Normal:

client -> request -> response

Pipelining:

client -> send many requests
server -> send many responses

Reduces network latency.

## Redis Lua Scripting

Atomic execution.

Example:

EVAL script.lua

Used for:

atomic operations

complex logic

## Real Production Example (System Design)

Example:

Instagram Feed Cache

Flow:

User requests feed
       ↓
Check Redis
       ↓
Cache hit → return
Cache miss
       ↓
DB query
       ↓
Update Redis
## Final Tip (Important for Interviews)

Interviewers expect 3 levels:

Level 1:

Redis is in-memory key value store

Level 2:

Event loop architecture
SDS
Data structures
Persistence

Level 3 (Architect):

Replication internals
Cluster slots
Eviction policies
Memory fragmentation
Failover mechanics

## Redis Security and Best Practices

### Security Considerations
- **Authentication:** Use `requirepass` for password protection; consider ACLs (Access Control Lists) in Redis 6+ for fine-grained permissions.
- **Encryption:** Redis does not encrypt data in transit or at rest by default. Use TLS (`redis-cli --tls`) for secure connections; encrypt disks for persistence files.
- **Network Security:** Bind to localhost or use firewalls; avoid exposing Redis to the internet.
- **Data Sanitization:** Treat Redis as trusted; validate inputs to prevent injection-like issues in Lua scripts.

### Performance Tuning
- **Memory Optimization:** Monitor fragmentation with `INFO memory`; use `maxmemory` and eviction policies; choose data structures wisely (e.g., integers as strings save space).
- **Connection Pooling:** Reuse connections to avoid overhead; use connection pools in clients like Jedis or redis-py.
- **Pipelining and Transactions:** Batch commands to reduce round-trips; use Lua scripts for atomic multi-step operations.
- **Monitoring:** Track slow logs (`SLOWLOG`), latency (`LATENCY`), and ops/sec; use Redis Insight or Prometheus exporters.

### Common Pitfalls
- **Key Expiration:** TTL is not precise; expired keys are lazy-deleted.
- **Big Keys:** Large values block the single-threaded event loop; split data or use compression.
- **Persistence Misconfiguration:** RDB + AOF can cause high I/O; test recovery scenarios.
- **Cluster Resharding:** Manual; plan for downtime or use Redis 7's cluster manager.
- **Lua Script Limits:** Scripts must be pure functions; avoid infinite loops.

### Recent Developments (as of 2026)
- **Redis 7.x:** Enhanced JSON support via RedisJSON module, improved cluster stability, and Active-Active replication for geo-distributed setups.
- **Modules Ecosystem:** Extend Redis with modules like RediSearch (full-text search), RedisGraph (graph queries), and RedisTimeSeries.
- **Cloud Integrations:** Managed Redis on Azure Cache, AWS ElastiCache, or GCP Memorystore for auto-scaling and backups.