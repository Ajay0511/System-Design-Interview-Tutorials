#Elastic Search
# Elasticsearch Interview Guide (Beginner → Advanced → Architect)

This guide contains **layered questions (Easy, Medium, Advanced)** with
**detailed answers**, focusing on **internals, performance, and
real-world architecture decisions**.

------------------------------------------------------------------------

# (Fundamentals)

## Q1. What is Elasticsearch?

Elasticsearch is a **distributed, RESTful search and analytics engine**
built on Apache Lucene. It is designed for **full-text search, log
analytics, and near real-time data querying**.

Unlike traditional databases, Elasticsearch: - Stores data as JSON
documents - Uses an **inverted index** for fast lookups - Is optimized
for read-heavy workloads

------------------------------------------------------------------------

## Q2. What is an Index?

An index is a **logical grouping of documents**.

It is similar to: - A database in RDBMS - A collection in MongoDB

Internally: - An index is divided into **primary shards** - Each shard
is an independent Lucene index

Design consideration: - Too many indices → cluster state overhead - Too
few → scaling limitations

------------------------------------------------------------------------

## Q3. What is a Document?

A document is the **smallest unit of data** stored in Elasticsearch.

Example:

``` json
{
  "user": "Ajay",
  "age": 25
}
```

Internally: - Documents are transformed into **inverted index
structures** - Each field is indexed separately for search

------------------------------------------------------------------------

## Q4. What is a Shard?

A shard is a **horizontal partition of an index**.

Purpose: - Distribute data across nodes - Enable parallel processing of
queries

Each shard: - Is a complete Lucene index - Stores a subset of documents

------------------------------------------------------------------------

## Q5. What is a Replica?

A replica is a **copy of a primary shard**.

It helps in: - Fault tolerance (node failure recovery) - Improved search
performance (read scaling)

------------------------------------------------------------------------

In **Elasticsearch**, the **In-Memory Buffer, Translog, and Disk** work together to balance two conflicting goals: making data searchable quickly and ensuring it is never lost in a crash. 
# 1. In-Memory Buffer (Searchability)
When a document is indexed, it is first written to an internal in-memory buffer. [3, 4] 

* Function: It holds documents before they are converted into a searchable format.
* Refresh: By default, every 1 second, the buffer is "refreshed." This process converts the buffer's contents into a new Lucene Segment.
* Outcome: Once a segment is created, the data becomes searchable. However, these segments initially live in the operating system's file system cache (RAM) and are not yet safely on physical disk. [3, 5, 6, 7, 8] 

# 2. Translog (Durability)
Simultaneously with the buffer write, the document is appended to the Translog (Transaction Log) on disk. [2, 7] 

* Purpose: Because Lucene commits (writing to disk) are expensive and slow, Elasticsearch uses the Translog as a "safety net". If the node crashes, Elasticsearch replays the Translog upon restart to recover data that was only in memory.
* Acknowledgement: By default, Elasticsearch only sends a 200 OK success response to the client after the Translog has been successfully fsync’ed to disk.
* Real-time CRUD: When you request a document by ID (GET), Elasticsearch checks the Translog first to ensure you see the most recent version, even if a "refresh" hasn't happened yet. [1, 2, 7, 9, 10, 11] 

# 3. Disk (Permanent Storage)
Data only becomes "permanent" on disk during a Flush operation (also known as a Lucene Commit). [12, 13] 

* What happens: All in-memory segments are permanently written (fsync'ed) to the physical disk, and a commit point is created.
* Triggers: A flush occurs automatically every 30 minutes or when the Translog reaches a certain size (default 512 MB).
* Cleanup: Once the data is safely on disk, the Translog is cleared (truncated) because its "recovery" records are no longer needed. [6, 7, 9, 11, 14] 

| Process [6, 8, 15, 16, 17] | Moving From | Moving To | Frequency | Result |
|---|---|---|---|---|
| Indexing | Client Request | Buffer & Translog | Per Request | Data is durable (safely logged) |
| Refresh | Buffer | In-Memory Segment | ~1 Second | Data becomes searchable |
| Flush | Memory Segments | Physical Disk | 30min / 512MB | Data is permanently stored |

--------------------------------------------------

# (Internal Working)

## Q6. How does Elasticsearch distribute data?

Elasticsearch uses a hashing function:

    shard = hash(_id) % number_of_primary_shards

This ensures: - Even data distribution - Predictable routing

However, bad routing strategies can lead to **hot shards**.

------------------------------------------------------------------------

## Q7. Explain indexing flow in detail

When a document is indexed:

1.  Request hits a coordinating node
2.  Node determines target shard using routing
3.  Request goes to primary shard
4.  Document is written to:
    -   In-memory buffer
    -   Translog (for durability)
5.  Replicated to replica shards
6.  Data becomes searchable after a refresh

Important: Elasticsearch provides **near real-time** search due to
refresh intervals.

------------------------------------------------------------------------

## Q8. What is Refresh vs Flush?

### Refresh:

-   Makes newly indexed data searchable
-   Creates new Lucene segments
-   Happens every 1 second (default)

### Flush:

-   Writes data permanently to disk
-   Clears translog

Difference: - Refresh → search visibility - Flush → durability and
recovery

------------------------------------------------------------------------

Good catch — you’re right. For **senior/architect interviews**, answers after Q8 should be much deeper (internals + trade-offs + real-world thinking).

Here’s an **improved `.md` section (Q9 onward)** with **detailed, interview-ready answers** 👇

---

```md
## Q9. How does search work internally?

Elasticsearch follows a **distributed scatter-gather model**, executed in two main phases:

### 1. Query Phase (Scatter)
- The coordinating node sends the query to all relevant shards (primary + replicas).
- Each shard executes the query locally using its Lucene index.
- Each shard returns:
  - Top N matching document IDs
  - Relevance scores

Important:
- This phase is CPU-intensive (scoring happens here).
- Each shard works independently → parallel execution.

### 2. Fetch Phase (Gather)
- Coordinating node merges results from all shards.
- Determines final top N results globally.
- Fetches actual documents from shards.

Optimization insights:
- Fetch phase can become slow if `_source` is large.
- Use `_source filtering` to reduce payload.
- Avoid deep pagination → costly across shards.

---

## Q10. What is an inverted index?

An inverted index is the **core data structure** behind Elasticsearch search.

Instead of storing:
```
doc → words
```

It stores:
```
word → list of documents
```

Example:
```
"quick" → doc1, doc4
"fox"   → doc2, doc4
```

Internally:
- Text is tokenized using analyzers
- Stored in **posting lists**
- Includes metadata like term frequency and positions

Why it matters:
- Enables O(1)-like lookup for terms
- Supports fast full-text search

Trade-offs:
- More storage overhead
- Slower indexing compared to simple DB inserts

---

## Q11. What is Mapping?

Mapping defines how fields are:
- Stored
- Indexed
- Queried

Example:
- `text` → analyzed (used in full-text search)
- `keyword` → exact match (used in filters/aggregations)

Deep insights:
- Wrong mapping = biggest production issue
- Text fields cannot be used efficiently for aggregations
- Numeric fields should not be stored as text

Common pitfalls:
- Mapping explosion (too many dynamic fields)
- Incorrect data types (string instead of date)

Best practices:
- Use explicit mapping in production
- Disable dynamic mapping for uncontrolled data

---

## Q12. What are Analyzers?

Analyzers convert raw text into searchable tokens.

Pipeline:
1. Character Filters → clean text (HTML removal, etc.)
2. Tokenizer → splits text into tokens
3. Token Filters → modify tokens (lowercase, stemming)

Example:
```

"Running Fast" → ["run", "fast"]

```

Why important:
- Determines how search behaves
- Affects both indexing and querying

Advanced concepts:
- Custom analyzers for domain-specific use cases
- Different analyzers for index vs search

---

## Q13. How do you decide shard count?

Shard sizing is a **critical architectural decision**.

Factors:
- Total data size
- Query throughput
- Node count
- Hardware (heap, CPU, disk)

Guidelines:
- Ideal shard size: **10–50 GB**
- Avoid too many shards (cluster state overhead)

Trade-offs:
- More shards → better parallelism, more overhead
- Fewer shards → less overhead, limited scaling

Real-world mistake:
- Oversharding → leads to GC issues, slow cluster state updates

---

## Q14. What are hot shards?

Hot shards occur when:
- A few shards receive disproportionate traffic

Causes:
- Poor routing key
- Time-based skew (recent data gets all traffic)
- Uneven data distribution

Problems:
- High CPU on specific nodes
- Latency spikes
- Uneven cluster utilization

Solutions:
- Use better routing keys
- Increase shard count
- Use index rollover (time-based indices)
- Add more nodes

---

## Q15. How to optimize indexing performance?

Key techniques:

### 1. Bulk API
- Batch multiple documents
- Reduces network overhead

### 2. Increase Refresh Interval
- Default is 1s → costly
- Set to 30s during heavy ingestion

### 3. Disable Replicas Temporarily
- Speeds up indexing
- Re-enable after ingestion

### 4. Optimize Shard Count
- Avoid too many small shards

### 5. Avoid Frequent Updates
- Elasticsearch is optimized for **append-heavy workloads**

Advanced tip:
- Use **indexing pipelines** for preprocessing

---

## Q16. How to optimize search performance?

### Techniques:

- Use **filters instead of queries**
  - Filters are cached
  - No scoring required

- Avoid deep pagination
  - Use `search_after` instead of `from + size`

- Reduce `_source`
  - Fetch only required fields

- Use `doc_values`
  - Efficient for sorting/aggregations

- Cache frequent queries
  - Query cache + request cache

---

## Q17. What is doc_values?

doc_values is a **columnar storage format** used for:
- Sorting
- Aggregations

Key properties:
- Stored on disk
- Memory efficient
- Default for most field types

Why needed:
- Avoids loading entire fielddata into heap

---

## Q18. What is fielddata and why is it dangerous?

Fielddata:
- Used for text fields in aggregations
- Loaded into heap memory

Problem:
- Can consume huge memory
- Causes OutOfMemory errors

Best practice:
- Avoid using fielddata
- Use `keyword` fields instead

---

## Q19. What is ILM (Index Lifecycle Management)?

ILM automates index lifecycle:

Phases:
- Hot → actively written
- Warm → less frequently queried
- Cold → rarely accessed
- Delete → removed

Benefits:
- Cost optimization
- Automatic data management

Used heavily in:
- Logging systems
- Time-series data

---

## Q20. Design a large-scale logging system

Architecture:

App → Kafka → Logstash → Elasticsearch → Kibana

Explanation:

- Kafka:
  - Handles buffering
  - Prevents data loss during spikes

- Logstash:
  - Transforms logs
  - Enriches data

- Elasticsearch:
  - Stores and indexes logs

- Kibana:
  - Visualization layer

Key design considerations:
- Backpressure handling
- Index rotation
- Storage cost optimization

---

## Q21. How to handle billions of documents?

Strategies:

- Time-based indices (daily/weekly)
- Use ILM policies
- Move old data to cold storage
- Optimize shard sizes
- Use rollover APIs

Advanced:
- Separate hot and cold nodes
- Use index templates

---

## Q22. Common production issues

1. Mapping explosion
2. High heap usage
3. Slow queries
4. Cluster state bloat
5. Uneven shard distribution

Debug approach:
- Check `_cluster/health`
- Analyze slow logs
- Monitor heap usage

---

## Q23. When NOT to use Elasticsearch?

Avoid ES when:
- Strong consistency required
- Heavy transactional workloads
- Frequent updates/deletes

Reason:
- ES is optimized for search, not transactions

---

## Q24. Elasticsearch vs RDBMS

| Feature | Elasticsearch | RDBMS |
|--------|-------------|------|
| Use case | Search & analytics | Transactions |
| Schema | Flexible | Strict |
| Consistency | Eventual | Strong |
| Query type | Full-text | Structured |

---
## Q25. What is a Coordinating Node?

A coordinating node is any node that receives a client request and is responsible for routing it to the correct shards.

Every node in Elasticsearch can act as a coordinating node by default.

Responsibilities:
- Scatter the query to relevant shards
- Gather and merge results
- Return final response to the client

In large clusters:
- Dedicated coordinating nodes are used to offload this work from data nodes
- They hold no data themselves
- Reduces CPU/memory pressure on data nodes

---

## Q26. What are the different node types in Elasticsearch?

| Node Type | Role |
|-----------|------|
| Master | Manages cluster state (index creation, shard allocation) |
| Data | Stores shards and handles CRUD, search |
| Ingest | Pre-processes documents via pipelines before indexing |
| Coordinating | Routes requests, merges results — no data stored |
| ML | Runs machine learning jobs |
| Remote Cluster Client | Handles cross-cluster search |

Best practice:
- In production, always dedicate master-eligible nodes separately
- Avoid combining master + data roles on the same node in large clusters

---

## Q27. What is Split Brain and how does Elasticsearch prevent it?

Split brain occurs when a cluster gets partitioned into two halves, and both elect their own master — leading to data inconsistency.

Prevention — Quorum-based voting:
- Elasticsearch uses a minimum master nodes (quorum) setting
- Formula: `(number of master-eligible nodes / 2) + 1`
- Example: 3 master nodes → quorum = 2

In Elasticsearch 7+:
- This is handled automatically via the **cluster.initial_master_nodes** bootstrap setting
- Manual `discovery.zen.minimum_master_nodes` is no longer needed

---

## Q28. What is the difference between `term` and `match` query?

| | `term` | `match` |
|--|--------|---------|
| Analysis | No | Yes |
| Use case | Exact match (keyword, IDs) | Full-text search |
| Field type | keyword | text |
| Example | status = "active" | search in body text |

Key insight:
- Running a `term` query on a `text` field will likely return no results because the stored tokens are lowercased/stemmed
- Always use `term` on `keyword` fields

---

## Q29. What is the difference between `filter` and `query` context?

### Query context:
- Calculates relevance score (`_score`)
- Used when you care about *how well* a document matches
- Example: full-text search

### Filter context:
- No scoring — just yes/no match
- Results are **cached**
- Faster for repeated use
- Example: `status = active`, `date range`

Best practice:
- Always push non-scoring conditions into `filter` context
- This improves performance significantly for high-traffic queries

---

## Q30. What is `_score` and how is it calculated?

`_score` is a relevance score assigned to each document during a query.

Elasticsearch uses the **BM25 algorithm** (since v5) to compute scores.

BM25 considers:
- **Term Frequency (TF)** — how often the term appears in the document
- **Inverse Document Frequency (IDF)** — how rare the term is across all documents
- **Field length** — shorter fields with the same term score higher

Why it matters:
- Determines ranking order of search results
- Custom scoring can be applied via `function_score` or `script_score` queries

---

## Q31. What is a Nested Object vs a Flat Object?

### Flat Object (default):
- Inner object fields are flattened into the parent document
- Loses the relationship between sub-fields
- Example: `address.city` and `address.zip` are treated independently

### Nested Object:
- Stores inner objects as hidden separate Lucene documents
- Preserves relationships between sub-fields
- Required when querying combinations of inner fields accurately

Trade-off:
- Nested queries are slower and more memory-intensive
- Use only when you need to query inner field combinations

---

## Q32. What is Cross-Cluster Search (CCS)?

Cross-Cluster Search allows a single query to search across multiple Elasticsearch clusters simultaneously.

Use cases:
- Multi-region deployments
- Separate clusters per team/environment with unified search

How it works:
- A local cluster acts as the coordinating cluster
- It fans out queries to remote clusters
- Results are merged and returned

Configuration:
```
cluster.remote.<alias>.seeds: ["remote-host:9300"]
```

---

## Q33. What is an Ingest Pipeline?

An ingest pipeline is a series of processors that transform or enrich documents **before** they are indexed.

Common processors:
- `grok` — parse unstructured text using regex patterns
- `date` — parse and reformat date fields
- `rename` / `remove` — restructure fields
- `set` — add computed fields
- `enrich` — look up and append external data

Example use case:
- Parse raw log lines into structured fields using `grok`
- Normalize timestamps using `date` processor

Alternative: Logstash (more powerful but external dependency)

---

## Q34. What is Segment Merging?

Lucene segments are immutable. When many small segments accumulate, Elasticsearch periodically merges them into larger segments in the background.

Why it matters:
- Too many small segments → slow search (must scan each segment)
- Merging reduces segment count → faster queries
- Deleted documents are physically removed only during a merge

Trade-off:
- Merging is I/O intensive
- Can cause indexing slowdowns if not throttled

Tuning:
- `index.merge.scheduler.max_thread_count` controls merge concurrency
- During bulk indexing, reduce merge aggressiveness to improve throughput

---

## Q35. How does Elasticsearch handle updates and deletes internally?

### Updates:
- Elasticsearch documents are **immutable**
- An update actually marks the old document as deleted and indexes a new version
- The old document is not physically removed until a segment merge

### Deletes:
- Deleted documents are marked with a **tombstone** in a `.del` file
- They still consume disk space until a merge removes them
- High delete rates → significant storage and performance overhead

Implication:
- Heavy update/delete workloads are anti-patterns in Elasticsearch
- Use append-only strategies where possible

---

## Q36. What is `index.refresh_interval` and when should you change it?

`index.refresh_interval` controls how often the in-memory buffer is flushed into a searchable Lucene segment.

Default: `1s`

When to change:
- **During bulk indexing**: Set to `-1` (disables refresh) for maximum throughput, then re-enable after ingestion
- **Near real-time requirements**: Keep default or reduce if sub-second search visibility is critical
- **Cost optimization**: Increase to `30s` or `60s` for logging systems where slight delays are acceptable

```json
PUT /my-index/_settings
{
  "index.refresh_interval": "30s"
}
```

---

## Q37. What is the Scroll API and when should you use it?

The Scroll API is used for retrieving large result sets from Elasticsearch efficiently.

How it works:
- Creates a point-in-time snapshot of the index
- Returns a `scroll_id` which is used to paginate through results
- Maintains a consistent view even if data changes

Use case:
- Exporting large datasets
- Batch processing of search results

Limitation:
- Not designed for real-time user-facing pagination
- Keeping scroll contexts open consumes heap memory

Modern alternative:
- Use `search_after` with a PIT (Point in Time) for efficient, stateless pagination

---

## Q38. What is `search_after` and how does it differ from `from + size`?

### `from + size` (offset pagination):
- Simple but scales poorly
- Elasticsearch must retrieve and discard `from` documents on every shard
- Deep pages (e.g., from=10000) are very expensive

### `search_after`:
- Stateless cursor-based pagination
- Uses the sort value of the last document as the starting point for the next page
- No memory overhead, scales to arbitrary depth

Best practice:
- Use `search_after` + `sort` for any pagination beyond the first few pages

---

## Q39. What is a Point In Time (PIT)?

A PIT is a lightweight snapshot of an index's current state that persists across multiple search requests.

Why it exists:
- Prevents results from shifting between pages if documents are indexed or deleted mid-pagination
- Stateless alternative to Scroll API

Usage:
```json
POST /my-index/_pit?keep_alive=1m
```

Combine with `search_after` for consistent, efficient deep pagination.

---

## Q40. What is the difference between `keyword` and `text` field types?

| | `text` | `keyword` |
|--|--------|-----------|
| Analyzed | Yes (tokenized) | No |
| Full-text search | Yes | No |
| Aggregations | Not directly | Yes |
| Sorting | Not directly | Yes |
| Use case | Blog body, descriptions | Tags, status, IDs |

Common pattern — multi-field mapping:
```json
"title": {
  "type": "text",
  "fields": {
    "raw": { "type": "keyword" }
  }
}
```
This allows full-text search on `title` and exact match/aggregation on `title.raw`.

---

## Q41. What is the Circuit Breaker in Elasticsearch?

Circuit breakers are safety mechanisms that prevent operations from consuming too much memory and causing an OutOfMemory error.

Types:
- **Parent circuit breaker** — overall memory limit
- **Fielddata circuit breaker** — limits fielddata heap usage
- **Request circuit breaker** — limits memory used by a single request
- **In-flight requests circuit breaker** — limits memory used by in-flight HTTP requests

Behavior:
- When a limit is hit, Elasticsearch throws a `CircuitBreakingException`
- The request fails but the node stays healthy

Tuning:
- Raise limits carefully — they exist to protect the JVM heap

---

## Q42. What is Heap sizing best practice for Elasticsearch?

JVM heap is critical for Elasticsearch performance.

Rules:
- Set heap to **50% of available RAM** (but no more than **30–32 GB**)
- Above 32 GB, JVM cannot use compressed ordinary object pointers (OOPs), making memory usage less efficient
- Remaining RAM is used by the OS file system cache — which Lucene heavily relies on

Setting heap:
```
ES_JAVA_OPTS="-Xms16g -Xmx16g"
```

Always set Xms = Xmx to prevent heap resizing pauses.

---

## Q43. What is an Alias in Elasticsearch?

An alias is a virtual index name that points to one or more real indices.

Use cases:
- **Zero-downtime reindexing**: Point alias to new index, then switch atomically
- **Index abstraction**: Application always queries the alias, not the index directly
- **Filtered aliases**: An alias with a built-in filter (e.g., alias showing only `status: active` documents)

```json
POST /_aliases
{
  "actions": [
    { "add": { "index": "logs-2025-01", "alias": "logs-current" } }
  ]
}
```

---

## Q44. What is Reindexing and when is it needed?

Reindexing copies documents from one index to another.

When needed:
- Changing mapping (shard count, field types cannot be changed in place)
- Upgrading index settings
- Restructuring data model

Using the Reindex API:
```json
POST /_reindex
{
  "source": { "index": "old-index" },
  "dest":   { "index": "new-index" }
}
```

Best practice:
- Reindex into a new index, validate, then switch the alias atomically
- Use `slices` parameter for parallel reindexing on large datasets

---

## Q45. What is the difference between `index` and `store` in field mapping?

| | `index` | `store` |
|--|---------|---------|
| Purpose | Makes field searchable | Stores original value separately |
| Default | `true` | `false` |
| Effect when disabled | Field cannot be searched | Field value retrieved from `_source` |

Key insight:
- `store: true` is rarely needed because `_source` already contains the full document
- Only useful when `_source` is disabled or you want to fetch a specific field without loading the full `_source`

---
