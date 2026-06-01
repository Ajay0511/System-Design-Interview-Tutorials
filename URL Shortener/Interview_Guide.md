# Design a URL Shortener (TinyURL / Bitly)

---

## 1. Requirements

### Functional Requirements

- User can submit a long URL and get a short URL.
- User can access the short URL and be redirected to the original URL.
- Same long URL should return the same short URL (deduplication).
- Support custom aliases (optional).
- Basic click analytics.

### Non-Functional Requirements

- High availability.
- Low latency (<50ms redirect).
- Horizontally scalable.
- Read-heavy workload.
- Durable storage.

---

## 2. High-Level Architecture

```
Client
  |
Load Balancer
  |
+------------------+
| URL Service Pods |
+------------------+
  |
  +------ Redis --------+
  |                     |
  |                   MySQL
  |
  +------ Kafka --------+
               |
     Analytics Consumer
               |
         Analytics DB
```

---

## 3. Database Schema

```sql
CREATE TABLE url_mapping (
    id          BIGINT PRIMARY KEY,
    short_code  VARCHAR(10) UNIQUE,
    url_hash    CHAR(32) UNIQUE,
    long_url    TEXT,
    created_at  TIMESTAMP,
    expires_at  TIMESTAMP NULL
);
```

**Indexes:**

```sql
INDEX(short_code)
INDEX(url_hash)
```

---

## 4. Why Both `short_code` and `url_hash`?

### `short_code`

Used for redirects. Example:

```
tiny.ly/aBc12X  →  https://google.com
```

### `url_hash`

Used for deduplication. Example:

```
url_hash = MD5("https://google.com")
```

If another user submits `https://google.com`, we return the existing short URL instead of creating a new one.

### Why not index `long_url` directly?

URLs can be 300–1000 bytes, making large indexes expensive. A hash is a fixed 32 bytes, making lookups far more efficient.

---

## 5. Unique ID Generation

### Common Mistake

Using `hash(long_url)` as the short code causes:

- Collision handling complexity.
- Difficulty guaranteeing uniqueness.

### Better Approach: Snowflake ID Generator

Use a distributed ID generator similar to Twitter Snowflake.

**Structure:**

```
64-bit ID:  | timestamp | machine_id | sequence |
```

**Example:**

```
timestamp = 1717234567890
machine_id = 5
sequence = 123

→ generates: 7392847239847234
```

**Why is it unique?**
The combination `(timestamp, machine_id, sequence)` is always unique. Even with multiple servers generating IDs simultaneously, each has a distinct `machine_id`, so there are no collisions.

### Convert to Base62

```
7392847239847234  →  aBc12X
```

Base62 uses characters `0-9`, `a-z`, `A-Z`.

**Capacity:**

```
62^6 ≈ 56 Billion
62^7 ≈ 3.5 Trillion
```

This gives compact, URL-friendly identifiers.

---

## 6. URL Creation Flow

**User submits:** `https://google.com`

**Step 1** — Compute hash:
```
url_hash = MD5(long_url)
```

**Step 2** — Check Redis:
```
hash:url_hash → short_code
```
If found, return the existing short URL.

**Step 3** — Redis miss → Query MySQL:
```sql
SELECT short_code FROM url_mapping WHERE url_hash = ?
```
If found, populate Redis and return the existing short URL.

**Step 4** — Not found → Generate new entry:
```
Snowflake ID: 7392847239847234
Base62(ID):   aBc12X
```
Store `(id, short_code, url_hash, long_url)` in MySQL.

**Step 5** — Cache both mappings in Redis:
```
hash:url_hash  →  short_code
short:aBc12X   →  long_url
```
Return: `tiny.ly/aBc12X`

---

## 7. Handling Concurrent Requests

Two simultaneous requests for `https://google.com` may both miss the cache. To prevent duplicate entries, enforce a unique constraint:

```sql
UNIQUE(url_hash)
```

**Flow:**

```
Request 1 → INSERT → Success
Request 2 → INSERT → Unique Constraint Violation
           → SELECT short_code WHERE url_hash = ?
           → Return existing short URL
```

This prevents race-condition duplicates.

---

## 8. Redirect Flow

**User visits:** `tiny.ly/aBc12X`

**Step 1** — Check Redis:
```
short:aBc12X → long_url
```
Cache hit → Return `302 Redirect` immediately. Latency: **1–5 ms**.

**Step 2** — Cache miss → Query MySQL:
```sql
SELECT long_url FROM url_mapping WHERE short_code = ?
```
Populate Redis, then redirect.

---

## 9. Redis Strategy

Do not cache all URLs — with 100M+ URLs, memory consumption becomes prohibitive.

**Instead:**

- Cache only **hot URLs**.
- Use **LFU** or **LRU** eviction policy.
- TTL: **30 minutes** (reasonable default).

---

## 10. Analytics

**Avoid** updating a counter on every redirect:

```sql
UPDATE click_count = click_count + 1
```

A viral URL could generate 50,000 clicks/sec, overloading the database.

### Better Approach: Async via Kafka

After a successful redirect, publish an event:

```json
{
  "shortCode": "aBc12X",
  "timestamp": "..."
}
```

**Analytics Consumer:**

- Reads events from Kafka.
- Aggregates counts in memory.
- Batch-updates the DB every **30 sec – 1 min**.

This keeps redirect latency low and the write load off the primary DB.

---

## 11. Scaling the Database

### Initial Phase

```
Primary MySQL  +  Read Replicas
```

- Writes → Primary
- Reads → Replicas

### Future Growth (Billions of URLs)

Introduce **sharding** with shard key:

```
hash(short_code) % N
```

Example shards: `Shard0`, `Shard1`, `Shard2`, `Shard3` — provides even distribution.

---

## 12. Availability & Failure Handling

| Failure | Behaviour |
|---|---|
| Redis failure | Falls back to MySQL lookup; system continues. |
| Replica failure | Other replicas serve traffic. |
| App server failure | Load balancer routes to healthy instances. |

---

## 13. Additional Enhancements

- **Bloom Filter** — Avoid unnecessary DB lookups for non-existent short codes.
- **CDN** — Cache globally popular redirects at the edge.
- **URL expiration** — Support `expires_at` TTL per entry.
- **Rate limiting** — Prevent abuse on the creation endpoint.
- **Custom aliases** — Allow users to specify their own short code.

---

## 2-Minute Interview Summary

> I would use **MySQL** as the source of truth and **Redis** as a cache. Each record stores `short_code`, `url_hash`, and `long_url`. The `url_hash` is used for deduplication, while `short_code` is used for redirects. For generating unique short URLs, I would use a distributed **Snowflake ID** generator and encode the result using **Base62**. Redirects first hit Redis and fall back to MySQL on cache misses. Analytics are handled asynchronously through **Kafka** to keep redirect latency low. Initially, I would scale MySQL with read replicas and later shard using `hash(short_code) % N` when the dataset becomes very large. This design is highly available, scalable, and optimized for a read-heavy workload.
