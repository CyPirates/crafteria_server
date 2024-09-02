package com.example.crafteria_server.domain.file.service;

import com.example.crafteria_server.config.EnvBean;
import com.example.crafteria_server.domain.file.entity.File;
import com.example.crafteria_server.domain.file.entity.Type;
import com.example.crafteria_server.domain.file.repository.FileRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
@Transactional
@Slf4j(topic = "ImageService")
@RequiredArgsConstructor
public class FileService {
    private final String IMAGE_DIR = "images";
    private final String MODEL_DIR = "models";

    private final FileRepository fileRepository;
    private final EnvBean envBean;
    private final Storage storage;

    private byte[] convertToWebP(MultipartFile file) throws IOException {
        // MultipartFile을 BufferedImage로 변환
        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 변환에 실패했습니다. 잘못된 이미지 파일입니다.");
        }

        // 비표준 색공간 이미지를 표준 RGB로 변환
        BufferedImage convertedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = convertedImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        // WebP 포맷으로 이미지 저장을 위한 설정
        ImageWriter writer = ImageIO.getImageWritersByMIMEType(file.getContentType()).next();
        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        if (writeParam.canWriteCompressed()) {
            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(0.8f); // 압축 품질 설정 (0 ~ 1)
        }

        // BufferedImage를 WebP 형식으로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(convertedImage, null, null), writeParam);
        } catch (IOException e) {
            log.error("ImageService.convertToWebP: {}", e.getMessage());
            throw new RuntimeException(e);
        } finally {
            writer.dispose();
        }

        return baos.toByteArray();
    }

    public File saveImage(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "image/webp";
        String uuid = UUID.randomUUID().toString();
        String filePath = IMAGE_DIR + "/" + uuid;

        BlobInfo blobInfo = BlobInfo.newBuilder(envBean.getBucketName(), filePath)
                .setContentType(extension)
                .build();

        try {
            Blob blob = storage.create(blobInfo, convertToWebP(multipartFile));
            String imageUrl = String.format("https://storage.googleapis.com/%s/%s", envBean.getBucketName(), filePath);
            String fileName = blob.getName();

            File file = File.builder()
                    .originalName(originalFilename)
                    .fileName(fileName)
                    .extension(extension)
                    .uuid(uuid)
                    .url(imageUrl)
                    .type(Type.IMAGE)
                    .build();

            return fileRepository.save(file);
        } catch (IOException e) {
            log.error("FileService.saveImage: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public File saveModel(MultipartFile multipartFile) {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "model/stl";
        String uuid = UUID.randomUUID().toString();
        String filePath = MODEL_DIR + "/" + uuid;

        BlobInfo blobInfo = BlobInfo.newBuilder(envBean.getBucketName(), filePath)
                .setContentType(extension)
                .build();

        try {
            Blob blob = storage.create(blobInfo, multipartFile.getBytes());
            String modelUrl = String.format("https://storage.googleapis.com/%s/%s", envBean.getBucketName(), filePath);
            String fileName = blob.getName();

            File file = File.builder()
                    .originalName(originalFilename)
                    .fileName(fileName)
                    .extension(extension)
                    .uuid(uuid)
                    .url(modelUrl)
                    .type(Type.MODEL)
                    .build();

            return fileRepository.save(file);
        } catch (IOException e) {
            log.error("FileService.saveModel: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void deleteFile(File file) {
        Blob blob = storage.get(envBean.getBucketName(), file.getFileName());
        if (blob == null) {
            log.error("FileService.deleteFile: 파일을 찾을 수 없습니다.");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        }

        try {
            Storage.BlobSourceOption preconditions = Storage.BlobSourceOption.generationMatch(blob.getGeneration());
            storage.delete(blob.getBlobId(), preconditions);
            fileRepository.delete(file);
        } catch (Exception e) {
            log.error("FileService.deleteFile: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
