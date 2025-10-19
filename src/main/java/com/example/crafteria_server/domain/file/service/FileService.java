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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
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
        String uuid = java.util.UUID.randomUUID().toString();
        String filePath = IMAGE_DIR + "/" + uuid;

        String asciiName = originalFilename == null ? "image.webp" : originalFilename.replace("\"", "");
        String utf8Encoded = URLEncoder.encode(asciiName, StandardCharsets.UTF_8);
        String contentDisposition = "inline; filename=\"" + asciiName + "\"; filename*=UTF-8''" + utf8Encoded;
        // 이미지의 경우 보통 미리보기 원하면 inline, 무조건 다운로드는 attachment로 바꿔도 됨

        BlobInfo blobInfo = BlobInfo.newBuilder(envBean.getBucketName(), filePath)
                .setContentType(extension)
                .setContentDisposition(contentDisposition) // ✅ 여기!
                .build();

        try {
            Blob blob = storage.create(blobInfo, convertToWebP(multipartFile));
            String imageUrl = String.format("https://storage.googleapis.com/%s/%s",
                    envBean.getBucketName(), filePath);
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
        String uuid = java.util.UUID.randomUUID().toString();
        String filePath = MODEL_DIR + "/" + uuid;

        // RFC 5987 형식: filename(ASCII) + filename* (UTF-8)
        String asciiName = originalFilename == null ? "file.stl" : originalFilename.replace("\"", "");
        String utf8Encoded = URLEncoder.encode(asciiName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + asciiName + "\"; filename*=UTF-8''" + utf8Encoded;

        BlobInfo blobInfo = BlobInfo.newBuilder(envBean.getBucketName(), filePath)
                .setContentType(extension)
                .setContentDisposition(contentDisposition) // ✅ 여기!
                .build();

        try {
            Blob blob = storage.create(blobInfo, multipartFile.getBytes());
            String modelUrl = String.format("https://storage.googleapis.com/%s/%s",
                    envBean.getBucketName(), filePath);
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
            log.warn("FileService.deleteFile: 파일을 찾을 수 없습니다. 이미 삭제된 상태일 수 있습니다.");
            // 파일이 없을 경우 예외를 발생시키지 않고 무시합니다.
            return;
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

    public void deleteFiles(List<File> files) {
        files.forEach(this::deleteFile);
    }

    /** RFC 5987 규격으로 Content-Disposition 값 생성 (한글/공백 안전) */
    private String buildContentDisposition(com.example.crafteria_server.domain.file.entity.File fileEntity) {
        String original = Optional.ofNullable(fileEntity.getOriginalName()).orElse("file");
        // 따옴표 제거(헤더 안전)
        String asciiName = original.replace("\"", "");
        String encoded = URLEncoder.encode(asciiName, StandardCharsets.UTF_8);
        // 이미지: inline(미리보기), 그 외: attachment(다운로드)
        boolean isImage = fileEntity.getType() == com.example.crafteria_server.domain.file.entity.Type.IMAGE;
        String dispType = isImage ? "inline" : "attachment";
        return dispType + "; filename=\"" + asciiName + "\"; filename*=UTF-8''" + encoded;
    }

    /** 단일 파일(레코드) 메타데이터 백필 */
    public boolean backfillContentDisposition(com.example.crafteria_server.domain.file.entity.File fileEntity) {
        try {
            String bucket = envBean.getBucketName();
            String objectName = fileEntity.getFileName(); // ex) models/<uuid> or images/<uuid>
            com.google.cloud.storage.Blob blob = storage.get(bucket, objectName);
            if (blob == null) {
                log.warn("[CD 백필] Blob not found: fileId={}, object={}", fileEntity.getId(), objectName);
                return false;
            }
            String newCd = buildContentDisposition(fileEntity);

            // contentType이 DB/객체에 없으면 안전하게 채움
            String contentType = Optional.ofNullable(fileEntity.getExtension())
                    .orElse(Optional.ofNullable(blob.getContentType()).orElse("application/octet-stream"));

            com.google.cloud.storage.BlobInfo updated = blob.toBuilder()
                    .setContentDisposition(newCd)
                    .setContentType(contentType)
                    .build();
            storage.update(updated);

            log.info("[CD 백필] OK fileId={}, object={}, contentDisposition={}", fileEntity.getId(), objectName, newCd);
            return true;
        } catch (Exception e) {
            log.error("[CD 백필] FAIL fileId={}, msg={}", fileEntity.getId(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 전체 일괄 백필 (페이지네이션)
     * @param pageSize 페이지당 처리 개수 (예: 500~2000 권장)
     * @param dryRun true면 실행하지 않고 로그만 남김
     * @return 실제 업데이트 성공 건수
     */
    @Transactional(readOnly = true)
    public int backfillAllContentDisposition(int pageSize, boolean dryRun) {
        int updated = 0;
        long page = 0;
        while (true) {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of((int) page, pageSize);
            org.springframework.data.domain.Page<com.example.crafteria_server.domain.file.entity.File> slice =
                    fileRepository.findAll(pageable);
            if (slice.isEmpty()) break;

            for (com.example.crafteria_server.domain.file.entity.File f : slice.getContent()) {
                // 이미 세팅되어 있으면 스킵하고 싶다면:
                // Blob b = storage.get(envBean.getBucketName(), f.getFileName());
                // if (b != null && b.getContentDisposition() != null) continue;

                if (dryRun) {
                    log.info("[CD 백필 DRY-RUN] fileId={}, name={}, type={}, url={}",
                            f.getId(), f.getOriginalName(), f.getType(), f.getUrl());
                } else {
                    if (backfillContentDisposition(f)) updated++;
                }
            }
            if (!slice.hasNext()) break;
            page++;
        }
        log.info("[CD 백필 완료] dryRun={}, updatedCount={}", dryRun, updated);
        return updated;
    }
}
