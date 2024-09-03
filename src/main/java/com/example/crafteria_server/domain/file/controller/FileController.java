package com.example.crafteria_server.domain.file.controller;

import com.example.crafteria_server.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j(topic = "FileController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileController {
    private final FileService fileService;
//    @PostMapping(value = "/upload_model_test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public JsonBody<String> uploadModel(@ModelAttribute FileDto file) {
//        fileService.saveModel(file.getImage());
//        return JsonBody.of(200, "성공", "파일 업로드 성공");
//    }
//
//    @PostMapping(value = "/upload_image_test", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public JsonBody<String> uploadImage(@ModelAttribute FileDto file) {
//        fileService.saveImage(file.getImage());
//        return JsonBody.of(200, "성공", "파일 업로드 성공");
//    }
}
