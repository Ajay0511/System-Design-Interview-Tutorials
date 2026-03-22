package com.example.elasticsearchapp.repository;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.example.elasticsearchapp.model.User;

public interface UserRepository extends ElasticsearchRepository<User, String> {

    List<User> findByName(String name);
}