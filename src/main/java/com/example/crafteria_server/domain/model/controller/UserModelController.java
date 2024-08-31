package com.example.crafteria_server.domain.model.controller;

import com.example.crafteria_server.domain.model.dto.UserModelDto;
import com.example.crafteria_server.domain.model.service.ModelService;
import com.example.crafteria_server.global.response.JsonBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j(topic = "UserModelController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/model")
public class UserModelController {
    private final ModelService modelService;

    @GetMapping("/list/popular")
    public JsonBody<List<UserModelDto.Response>> getPopularModelList(@RequestParam(defaultValue = "0") int page) {
        return JsonBody.of(200, "성공", modelService.getPopularList(page));
    }

    @GetMapping("/model/{modelId}")
    public JsonBody<UserModelDto.Response> getModelDetail(@PathVariable Long modelId) {
        return JsonBody.of(200, "성공", modelService.getModelDetail(modelId));
    }

//    @PostMapping("/model/upload")
//    public JsonBody<UserModelDto.Response> uploadModel(@RequestBody UserModelDto.UploadRequest request) {
//        return JsonBody.of(200, "성공", modelService.uploadModel(request));
//    }
}
