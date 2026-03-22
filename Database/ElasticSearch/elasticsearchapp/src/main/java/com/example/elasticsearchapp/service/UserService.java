package com.example.elasticsearchapp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.elasticsearchapp.repository.UserRepository;
import com.example.elasticsearchapp.model.User;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public List<User> findByName(String name) {
        return userRepository.findByName(name);
    }

}
