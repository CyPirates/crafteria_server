package com.example.crafteria_server.domain.file.controller;

import com.example.crafteria_server.domain.file.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j(topic = "FileController")
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileController {
    /**private final FileService fileService;
    @PostMapping("/backfill-content-disposition")
    public Map<String, Object> backfill(@RequestParam(defaultValue = "1000") int pageSize,
                                        @RequestParam(defaultValue = "true") boolean dryRun) {
        int updated = fileService.backfillAllContentDisposition(pageSize, dryRun);
        return Map.of("updated", updated, "dryRun", dryRun, "pageSize", pageSize);
    }**/
}
