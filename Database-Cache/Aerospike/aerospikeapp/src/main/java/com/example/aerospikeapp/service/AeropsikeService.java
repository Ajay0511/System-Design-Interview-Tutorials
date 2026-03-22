package com.example.aerospikeapp.service;

import org.springframework.stereotype.Service;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import com.example.aerospikeapp.config.AerospikeProperties;
import com.aerospike.client.Record;

@Service
public class AeropsikeService {
    private final AerospikeClient aerospikeClient;

    private final AerospikeProperties aerospikeProperties;

    public AeropsikeService(AerospikeClient aerospikeClient, AerospikeProperties aerospikeProperties) {
        this.aerospikeClient = aerospikeClient;
        this.aerospikeProperties = aerospikeProperties;
    }

    public void saveUser(String userId, String name, int age) {
        // Implement logic to save user data to Aerospike
        Key key = new Key(aerospikeProperties.getNamespace(), aerospikeProperties.getSet(), userId);
        // Create bins and save data using aerospikeClient.put() method
        Bin nameBin = new Bin("name", name);
        Bin ageBin = new Bin("age", age);
        aerospikeClient.put(null, key, nameBin, ageBin);
    }

    public Record getUser(String userId) {
        // Implement logic to retrieve user data from Aerospike
        Key key = new Key(aerospikeProperties.getNamespace(), aerospikeProperties.getSet(), userId);
        return aerospikeClient.get(null, key);
    }

    public boolean existsUser(String userId) {
        // Implement logic to check if user exists in Aerospike
        Key key = new Key(aerospikeProperties.getNamespace(), aerospikeProperties.getSet(), userId);
        return aerospikeClient.exists(null, key);
    }

    public void deleteUser(String userId) {
        // Implement logic to delete user data from Aerospike
        Key key = new Key(aerospikeProperties.getNamespace(), aerospikeProperties.getSet(), userId);
        aerospikeClient.delete(null, key);
    }
    

}
