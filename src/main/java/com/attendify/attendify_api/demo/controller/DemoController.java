package com.attendify.attendify_api.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/attendify/v1/demo")
public class DemoController {
    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Hello, this a secured endpoint");
    }
}
