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

**RDB (snapshot):**
- Periodically saves full dataset to disk
- Compact and efficient for backups

**AOF (append-only file):**
- Logs every write command
- Better durability and recovery precision

## 5) Replication

In **primary-replica** setup:
- Writes are processed on primary
- Same writes are propagated to replicas

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
- Most basic Redis type
- **Example:** `SET user:1 "Ajay"`
- Internally uses **SDS (Simple Dynamic String)**

**Why SDS:**
- **O(1)** length lookup
- Binary-safe storage
- Efficient append
- Safer than plain C strings

**SDS shape:**
`struct SDS { int len; int free; char buf[]; }`

### 10.2 Hash
- Used for object-like records
- **Example:** `HSET user:1 name Ajay age 25`
- **Internal encodings:**
   - Small hash: compact encoding
   - Large hash: hashtable
- Redis auto-converts as size grows

### 10.3 List
- Ordered sequence
- **Example:** `LPUSH queue job1`, `RPUSH queue job2`
- Implemented using **quicklist**
- Good for queue-like workflows

### 10.4 Set
- Unordered collection of unique values
- **Example:** `SADD users Ajay Rahul`
- Internally backed by hash-based representation
- Common operations are **O(1)**

### 10.5 Sorted Set (ZSET)
- Elements with score
- **Example:** `ZADD leaderboard 100 Ajay`
- Internally: hash map + skip list
- Supports fast range queries
- **Example:** `ZRANGE leaderboard 0 10`
- Typical complexity: **O(log N)**

### 10.6 Bitmap
- Bit-level operations on strings
- **Example:** `SETBIT online_users 10 1`
- Useful for login/activity flags and analytics
- Very memory efficient

### 10.7 HyperLogLog
- Approximate unique counting
- **Example:** `PFADD visitors user1 user2`, `PFCOUNT visitors`
- Fixed low memory footprint (about **12 KB**)
- Works well even for very large cardinalities

### 10.8 Streams
- Log-style append-only data structure
- **Example:** `XADD orders * product iphone`
- Used for event streaming and messaging patterns



