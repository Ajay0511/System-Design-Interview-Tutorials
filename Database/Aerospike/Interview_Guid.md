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
| amcat | Namespace | Database / Tablespace |
| token | Set | Table |
| id_name | Key | Primary Key |
| JSON Data | Bins / Record | Columns / Row |

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

# 9. What is the Primary Index?

The primary index maps the key digest to the storage location of the record.


Digest → Storage location


Each index entry contains:

- Key digest
- Partition ID
- Storage pointer
- Metadata

---
