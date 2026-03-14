package com.example.aerospikeapp.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "aerospike")
public class AerospikeProperties {
    private List<HostConfig> hosts;
    private String namespace;
    private String set;

    @Data
    public static class HostConfig {
        private String host;
        private int port;
    }
    
}
