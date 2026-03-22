# Aerospike Interview Questions and Answers  
## Architecture + Internals

---

# 1. What is Aerospike?

**Aerospike** is a **distributed NoSQL key-value database** designed for **real-time applications** that require **extremely low latency** and **high throughput**.

### Key Characteristics

- **Sub-millisecond latency**
- **Horizontally scalable**
- **Hybrid memory architecture**
- **Optimized for SSD storage**
- **Supports strong consistency**

### Typical Use Cases

- AdTech platforms
- Fraud detection systems
- Session storage
- Real-time analytics
- User profile storage

---

# 2. What type of database is Aerospike?

Aerospike is primarily a **Key-Value Database**, but it also supports **document-style records using bins**.

### Data Hierarchy

```
Namespace
   ↓
Set
   ↓
Record
   ↓
Bins
```

This hierarchy allows Aerospike to organize data efficiently across a distributed cluster.

---

# 3. What are the Core Components of Aerospike?

Major components include:

- **Cluster**
- **Node**
- **Namespace**
- **Set**
- **Record**
- **Bin**
- **Primary Index**
- **Secondary Index**
- **Storage Engine**

Each of these components plays a role in ensuring **high availability, scalability, and performance**.

---

# 4. What is a Namespace in Aerospike?

A **Namespace** is similar to a **database in relational systems**.

It defines important configuration properties such as:

- **Storage configuration**
- **Replication factor**
- **Memory limits**
- **TTL (Time To Live) configuration**
- **Eviction policy**

### Example

```
Namespace: users
Namespace: sessions
Namespace: logs
```

Each namespace can have **different storage and performance configurations**.

---

# 5. What is a Set?

A **Set** is similar to a **table inside a namespace**.

### Example

```
Namespace: users

Sets:
   profiles
   preferences
   login_history
```

Sets help logically group similar records.

---

# 6. What is a Record?

A **Record** is the **primary unit of storage** in Aerospike.

### Record Structure

```
Record
   ├── Key
   ├── Bins (data fields)
   └── Metadata
```

### Example

```
Key: user123

Bins:
   name = Ajay
   age = 28
   country = India
```

### Concept Mapping

| Your Concept | Aerospike Component | RDBMS Equivalent |
|---------------|--------------------|------------------|
| amcat         | Namespace          | Database / Tablespace |
| token         | Set                | Table |
| id_name       | Key                | Primary Key |
| JSON Data     | Bins / Record      | Columns / Row |

---

# 7. What are Bins?

**Bins** are equivalent to **columns/fields** in a record.

### Example

```
Record: user123

Bins:
   name
   age
   city
   email
```

A record can have **multiple bins**, each storing a **different piece of data**.

---

# 8. What Metadata is Stored With Each Record?

Aerospike stores several metadata attributes with each record:

- **Generation** → Version number used for concurrency control
- **TTL (Time To Live)** → Determines when the record expires
- **Last Update Time** → Timestamp of the last update
- **Record Size** → Size of the stored record

These metadata fields help Aerospike maintain **data consistency, expiration, and performance optimization**.

---

# Aerospike Internal Architecture & Workflow

Aerospike operates on a **three-layered architecture** designed to remove bottlenecks between your application and the physical hardware.

### 1. The Client Layer (Smart Client)
The "Smart Client" is more than a driver; it is a **cluster-aware routing engine**.
*   **Partition Map:** The client maintains a local copy of the cluster's partition map (4,096 logical partitions).
*   **Direct Routing:** When your app requests a key, the client hashes it to find the correct partition and identifies the specific node holding that data.
*   **Single-Hop Access:** It bypasses load balancers and proxies, sending the request directly to the correct server.

### 2. The Distribution Layer (Clustering)
This layer manages high availability and data integrity across the **Shared-Nothing** cluster.
*   **Peer-to-Peer:** There is no "master" node. Nodes use a **Paxos-based heartbeat** protocol to monitor each other's health.
*   **Automatic Rebalancing:** If a node fails, the cluster immediately redistributes its partitions. The Smart Client detects this and updates its map automatically.
*   **Data Consistency:** It handles replication (synchronous or asynchronous) to ensure data is mirrored across different physical nodes.

### 3. The Data Storage Layer (Hybrid Memory)
This is where the low-latency "magic" happens at the hardware level.

#### **The Read Path**
1.  **Index Lookup:** The node checks the **Primary Index** (stored entirely in DRAM).
2.  **64-Byte Entry:** Each index entry is a fixed 64 bytes containing the physical pointer (offset) of the data on the SSD.
3.  **Direct I/O:** The system performs a **single hardware read** to the exact disk location, bypassing the Linux filesystem and page cache to eliminate OS jitter.

#### **The Write Path**
1.  **Memory Buffering:** Writes are gathered into a large **Write Block** (typically 1MB) in RAM.
2.  **Sequential Writing:** Once full, the entire block is flushed to the SSD in one sequential operation. This mimics the speed of memory and extends SSD lifespan.
3.  **Defragmentation:** A background process (the "Garbage Collector") identifies blocks with expired or deleted data and reclaims that space to keep the SSD performing at peak speeds.

***

---

# 8. What is Hybrid Memory Architecture?
Aerospike uses hybrid memory architecture where the primary index is stored in RAM and the actual data is stored on SSD.

RAM
Primary Index

SSD
Record Data

Benefits:
- Fast lookups
- Low RAM usage
- Large storage capacity

---

# 9. What storage options does Aerospike support?
Aerospike supports multiple storage modes to fit different workloads:

- **Memory (in-memory)**: All data (index + records) resides in RAM. Best for ultra-low latency and ephemeral data.
- **SSD (storage)**: Index in RAM, records on SSD/NVMe (hybrid mode). Best balance of cost and capacity.
- **Hybrid memory**: Data in SSD, index in RAM (default mode). Offers large capacity with predictable performance.

---

# 10. What is the Primary Index?
The primary index maps the key digest to the storage location of the record.
Digest → Storage location

Each index entry contains:
- Key digest
- Partition ID
- Storage pointer
- Metadata
---

# 10. What is a Digest?
A digest is a hashed representation of the user key.
Aerospike uses RIPEMD-160 hashing.

User Key
↓
Hash (RIPEMD-160)
↓
Digest

Purpose:
- Uniform distribution
- Fast lookups
- Partition mapping

---


# 11. Why does Aerospike keep the primary index in RAM?
The primary index is stored in RAM to achieve extremely low latency lookups.
Lookup process:

Client request
↓
Hash key → Digest
↓
Lookup digest in RAM index
↓
Find storage pointer
↓
Read record from SSD

RAM lookup is extremely fast compared to disk access.
---
---

# 12. How does Aerospike distribute data across the cluster?
Aerospike divides each namespace into 4096 partitions.
These partitions are distributed across cluster nodes.

Example:

Cluster
Node A → 1024 partitions
Node B → 1024 partitions
Node C → 1024 partitions
Node D → 1024 partitions
---

# 13. Why does Aerospike use exactly 4096 partitions?
4096 partitions provide:
- Balanced data distribution
- Efficient migration
- Small metadata overhead
- Faster rebalancing when nodes join or leave

4096 = 2¹²

## Advantages:
- Fast bit operations for hashing
- Efficient partition calculation
- Perfect trade-off between
- too few partitions (hotspots)
- too many partitions (metadata overhead)
---

# 14. What is a Partition Map?
Partition map tells the client which node owns which partition.
Partition ID → Node

Client flow:
Client
↓
Hash key
↓
Determine partition
↓
Find node from partition map
↓
Send request directly
---

# 15. What happens when a new node joins the cluster?
When a node joins:
1. Cluster membership updates
2. Partitions are reassigned
3. Data migration begins
4. Partition map updates

Migration happens online without downtime.

---

# 16. What is Data Migration in Aerospike?
Data migration occurs when partitions move between nodes due to cluster changes.

Migration ensures:
- Balanced partition distribution
- Correct replication

---

# 17. How does replication work in Aerospike?
Each partition has a master and replica nodes.

Example:

Replication Factor = 2
Partition 100
Master → Node1
Replica → Node2

---

# 18. What is Replication Factor?
Replication factor defines how many copies of a record exist.

RF = 2 → 1 master + 1 replica
RF = 3 → 1 master + 2 replicas
---

# 19. What is Rack Awareness?
Rack awareness ensures replicas are placed on different racks.

Example:
Rack1 → Master
Rack2 → Replica

Purpose:
- Survive rack failure
- Improve fault tolerance
---

# 20. Does Aerospike support strong consistency?
Yes.

Aerospike supports two modes:
- AP Mode (Availability + Partition tolerance)
- Strong Consistency Mode
---

# 21. What is AP Mode?
AP mode prioritizes availability.

During network partition:
- Both clusters may accept writes
- Eventually the data becomes consistent.

---

# 22. What is Strong Consistency Mode?
Strong consistency ensures:
- Linearizable reads
- No stale data
- Strict write ordering

Tradeoff:
- Slightly higher latency
- Reduced availability during partitions

---

# 23. How does Aerospike avoid split-brain?
Aerospike uses Paxos-based cluster membership.
Writes are allowed only when a majority of nodes are available.

Example:
RF = 3
Minimum nodes required = 2

If majority is not available, writes are blocked.
---

# 24. What happens during a rolling upgrade?
Rolling upgrade means upgrading nodes one by one without downtime.

Process:
- Upgrade Node A
- Node leaves cluster
- Partitions migrate
- Node rejoins cluster
- Repeat for next node
---

# 26. How does Aerospike guarantee durability?
Durability is achieved through:
- Replication
- SSD storage
- Write commit levels

Example:
- COMMIT_MASTER
- COMMIT_ALL

COMMIT_ALL ensures writes are replicated before acknowledgment.
---

# 27. What is the Aerospike write path?
Write flow:

Client
↓
Node receives request
↓
Update primary index (in RAM)
↓
Write record to storage (SSD/NVMe)
↓
Replicate to replica nodes

### Write commit levels
Aerospike supports different durability levels:
- **COMMIT_MASTER**: Acknowledge after master has written to storage.
- **COMMIT_ALL**: Acknowledge after all replicas have stored the record.

---

# 28. What is the Aerospike read path?
Read flow:

Client
↓
Client hashes key to find partition
↓
Client routes to correct node via partition map
↓
Node looks up digest in primary index (in RAM)
↓
Node reads record from storage (SSD/NVMe)

---

# 29. What is a secondary index?
A secondary index allows querying records by the value of a bin rather than the primary key.

Example:
- Query users where `age = 30`

---

# 30. Where are secondary indexes stored?
Secondary indexes are stored in RAM for fast lookup.

---

# 31. What are the limitations of secondary indexes?
- Higher RAM usage compared to primary key lookups
- Slower than direct key-based access
- Limited query complexity (no joins, limited predicates)

---

# 32. What is TTL (Time To Live)?
TTL defines how long a record remains valid in Aerospike.

Example:
TTL = 3600 seconds (1 hour)

After expiration, records are eligible for removal.

---

# 33. What is lazy expiration?
Expired records are removed lazily:
- During reads (when a client accesses an expired record)
- During background scans (defragmentation / read operations)

---

# 34. How does Aerospike detect node failure?
Nodes send heartbeats to each other.
Heartbeat failure indicates node failure.

Cluster then:
- Updates membership
- Reassigns partitions
- Promotes replicas to master
---

# 30. What happens when a node crashes?

Example:
Cluster with RF = 2
Node1 → Master
Node2 → Replica

If Node1 crashes:
Node2 becomes master
Cluster rebalances partitions
Migration begins
---

# 31. What happens when memory for primary index is exhausted?

Possible outcomes:
- Writes are rejected
- Eviction policy removes old records
- Cluster must be scaled by adding nodes

Example error:
AEROSPIKE_ERR_NO_SPACE
---

# 32. How does Aerospike optimize SSD writes?
Aerospike uses:
- Log structured storage
- Sequential writes
- Write batching
- Direct IO
- Background defragmentation

These techniques improve SSD performance.
---

# 33. How does Aerospike handle hot keys?
Hot keys are keys that receive a disproportionate amount of traffic.

Common mitigation strategies:
- Shard the key at the application layer (e.g., add a random prefix)
- Use client-side batching or caching (if possible)
- Increase cluster capacity and spread partitions
- Use strong consistency carefully to avoid additional load on a single master

---

# 34. What are common Aerospike production monitoring metrics?
Important metrics include:
- Latency
- Object count
- Memory usage
- Migration progress
- Replication lag
- Disk usage
---

# 34. Why is Aerospike faster than many databases?
Reasons:
- Primary index in RAM
- Direct client-to-node communication
- SSD optimized storage
- Lock-free architecture
- Fixed partition distribution
- Efficient threading model

Typical performance:
Latency: <1 ms
Throughput: millions of operations per second