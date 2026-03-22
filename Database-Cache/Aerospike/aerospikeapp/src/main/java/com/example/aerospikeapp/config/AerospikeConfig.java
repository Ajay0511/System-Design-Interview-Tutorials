package com.example.aerospikeapp.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Host;
import com.aerospike.client.policy.ClientPolicy;

@Configuration
public class AerospikeConfig {

    private final AerospikeProperties aerospikeProperties;

    public AerospikeConfig(AerospikeProperties aerospikeProperties) {
        this.aerospikeProperties = aerospikeProperties;
    }

    @Bean
    public AerospikeClient aerospikeClient() {
        ClientPolicy clientPolicy = new ClientPolicy();
        List<AerospikeProperties.HostConfig> hosts = aerospikeProperties.getHosts();
        Host[] aerospikeHosts = hosts.stream()
                .map(hostConfig -> new Host(hostConfig.getHost(), hostConfig.getPort()))
                .toArray(Host[]::new);  
        return new AerospikeClient(clientPolicy, aerospikeHosts);
    }


}
