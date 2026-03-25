# Kafka

## Basics

### Q1. What is Apache Kafka?
**Answer:**
Apache Kafka is a distributed event streaming platform used for building real-time data pipelines and streaming applications. It is designed for high throughput, fault tolerance, and scalability.

---


### Q2. Explain Kafka architecture.
**Answer:**
Kafka consists of:
- **Producer** → Sends messages
- **Consumer** → Reads messages
- **Broker** → Kafka server
- **Topic** → Logical grouping of messages
- **Partition** → Subdivision of topic
- **Controller (KRaft/ZooKeeper)** → Manages metadata

---

### Q3. What is a partition?
**Answer:**
A partition is a unit of parallelism. Each topic is divided into partitions, allowing multiple consumers to read data in parallel. Ordering is guaranteed only within a partition.

---

### Q4. What is an offset?
**Answer:**
Offset is a unique identifier for each message within a partition. It helps consumers track which messages have been processed.

---


## Intermediate

### Q5. What is ISR (In-Sync Replica)?
**Answer:**
ISR is the set of replicas that are fully caught up with the leader. Only ISR members are eligible for leader election.

---

### Q6. Explain producer acknowledgments (acks).
**Answer:**
- `acks=0`: No acknowledgment
- `acks=1`: Leader acknowledgment
- `acks=all`: Leader + all ISR acknowledgment  
Higher acks ensure durability but increase latency.

---

### Q7. What are consumer groups?
**Answer:**
Consumers are grouped to enable parallel processing. Each partition is assigned to only one consumer in a group.

---

### Q8. What is consumer lag?
**Answer:**
Consumer lag is the difference between the latest offset and the consumer’s committed offset. It indicates how far behind a consumer is.

---

### Q9. What is log compaction?
**Answer:**
Log compaction retains only the latest value for each key, useful for maintaining state.

---

## Advanced

### Q10. How does Kafka ensure fault tolerance?
**Answer:**
- Replication across brokers
- Leader-follower model
- ISR mechanism
- Automatic leader election

---

### Q11. What is an idempotent producer?
**Answer:**
An idempotent producer ensures no duplicate messages are written even in retries by assigning sequence numbers.

---

### Q12. What are Kafka transactions?
**Answer:**
Transactions allow atomic writes across multiple partitions/topics and enable exactly-once semantics.

---

### Q13. How does Kafka handle ordering?
**Answer:**
Ordering is guaranteed within a partition. To maintain order, use the same key so messages go to the same partition.

---

### Q14. What is retention policy?
**Answer:**
Defines how long Kafka retains data:
- Time-based (`log.retention.hours`)
- Size-based (`log.retention.bytes`)

---

## Architect / System Design

### Q15. How do you design a high-throughput Kafka system?
**Answer:**
- Increase partitions
- Use batching (`batch.size`, `linger.ms`)
- Compression (snappy/lz4)
- Scale brokers horizontally
- Optimize disk and network

---

### Q16. How do you achieve exactly-once semantics?
**Answer:**
- Idempotent producers
- Transactions
- Proper offset commit handling

---

### Q17. How would you design Kafka for millions of messages/sec?
**Answer:**
- 100+ partitions
- Multiple brokers
- SSD storage
- High network bandwidth
- Tuned producer/consumer configs

---

### Q18. What happens during rebalance?
**Answer:**
- Consumers stop processing
- Partitions are reassigned
- Processing resumes  
This may cause temporary delays or duplicate processing.

---

### Q19. How do you scale Kafka?
**Answer:**
- Add brokers
- Increase partitions
- Reassign partitions

---

### Q20. What are common bottlenecks?
**Answer:**
- Disk I/O
- Network throughput
- GC pauses
- Under-partitioning
- Slow consumers

---

## Real-World Scenarios

### Q21. How do you debug consumer lag?
**Answer:**
- Monitor lag metrics
- Increase consumers
- Optimize processing logic
- Check downstream dependencies

---

### Q22. What happens if a broker crashes?
**Answer:**
- Leader election occurs
- New leader selected from ISR
- Producers/consumers reconnect automatically

---

### Q23. How do you prevent data loss?
**Answer:**
- replication.factor ≥ 3
- acks=all
- min.insync.replicas ≥ 2
- Disable unclean leader election

---

### Q24. How do you handle duplicate messages?
**Answer:**
- Use idempotent producers
- Deduplicate using keys
- Maintain external state (DB/Redis)

---

### Q25. How do you design Kafka + Elasticsearch pipeline?
**Answer:**
- Producer → Kafka
- Kafka Connect → Elasticsearch
- Bulk indexing
- Retry and DLQ handling

---

### Q26. How do you design multi-region Kafka?
**Answer:**
- Use MirrorMaker 2
- Active-passive or active-active setup
- Handle latency and consistency trade-offs

---

### Q27. How do you handle schema evolution?
**Answer:**
- Use Schema Registry
- Avro/Protobuf
- Maintain backward/forward compatibility

---

### Q28. What is backpressure and how do you handle it?
**Answer:**
Backpressure occurs when consumers cannot keep up.

Handling:
- Throttle producers
- Scale consumers
- Buffering and rate limiting

---

### Q29. How do you monitor Kafka?
**Answer:**
- Consumer lag
- ISR status
- Broker health
- Disk usage
- Throughput metrics

---

### Q30. Kafka vs traditional messaging systems?

| Feature | Kafka | Traditional MQ |
|--------|------|---------------|
| Model | Log-based | Queue-based |
| Scalability | High | Limited |
| Retention | Configurable | Usually delete on consume |
| Throughput | Very high | Moderate |

---

