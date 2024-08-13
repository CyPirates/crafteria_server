package com.example.crafteria_server.domain.test.controller;

import com.example.crafteria_server.global.response.JsonBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j(topic = "TestController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/test")
public class TestController {
    @GetMapping("/good")
    public JsonBody<String> test() {
        return JsonBody.of(200, "성공", "테스트 성공");
    }

    @GetMapping("/bad")
    public JsonBody<String> test2() {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "테스트 실패");
    }
}
