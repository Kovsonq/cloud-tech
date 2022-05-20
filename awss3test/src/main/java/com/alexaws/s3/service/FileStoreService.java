package com.alexaws.s3.service;

import com.alexaws.s3.filestore.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

import static org.apache.http.entity.ContentType.*;

@Service
public class FileStoreService {

    private final FileStore fileStore;

    @Autowired
    public FileStoreService(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    public List<String> getBuckets() {
        return fileStore.getBuckets();
    }

    public List<String> getAllFiles(String bucketName) {
        return fileStore.getAllFiles(bucketName);
    }

    public void downloadFile(String bucketName, String fileName, String versionId) {
        fileStore.downloadFile(bucketName, fileName, versionId);
    }

    public void uploadFile(String bucketName, MultipartFile file) {
        isFileEmpty(file);
        isFileText(file);
        Map<String, String> metadata = fetchFileMetadata(file);

        try {
            fileStore.uploadFile(bucketName, file.getOriginalFilename(), Optional.of(metadata),
                    file.getInputStream());
        } catch (IOException e) {
            throw new IllegalStateException("Error during saving file to S3 bucket", e);
        }
    }

    private void isFileText(MultipartFile file) {
        if (!Arrays.asList(
                        TEXT_PLAIN.getMimeType(),
                        TEXT_HTML.getMimeType(),
                        TEXT_XML.getMimeType())
                .contains(file.getContentType())) {
            throw new IllegalStateException("File must be an image [ " + file.getContentType() + " ]");
        }
    }

    private void isFileEmpty(MultipartFile file) {
        if (file == null) {
            throw new IllegalStateException("Cannot upload file (it's null).");
        }
        if (file.isEmpty()) {
            throw new IllegalStateException("Cannot upload empty file [ " + file.getSize() + " ]");
        }
    }

    private Map<String, String> fetchFileMetadata(MultipartFile file) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("Content-Type", file.getContentType());
        metadata.put("Content-Length", String.valueOf(file.getSize()));
        return metadata;
    }

    public boolean delete(String bucketName, String fileName) {
        return fileStore.deleteFile(bucketName, fileName);
    }

    public boolean deleteBucket(String bucketName) {
        return fileStore.deleteBucket(bucketName);
    }

    public String createBucket(String bucketName) {
        return fileStore.createBucket(bucketName);
    }

    public List<String> getFileVersions(String bucketName, String filePath) {
        return fileStore.getFileVersions(bucketName, filePath);
    }

    public boolean deleteVersion(String bucketName, String filePath, String versionId) {
        return fileStore.deleteVersion(bucketName, filePath, versionId);
    }
}
