package com.example.aerospikeapp.controller;

import com.aerospike.client.Record;
import org.springframework.web.bind.annotation.*;
import com.example.aerospikeapp.service.AeropsikeService;


@RestController
@RequestMapping("/users")
public class AerospikeController {

    private final AeropsikeService service;

    public AerospikeController(AeropsikeService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public String saveUser(
            @RequestParam String id,
            @RequestParam String name,
            @RequestParam int age) {

        service.saveUser(id, name, age);

        return "User saved successfully";
    }

    @GetMapping("/{id}")
    public Record getUser(@PathVariable String id) {
        return service.getUser(id);
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable String id) {
        service.deleteUser(id);
        return "User deleted";
    }
}