package com.alexaws.s3.controller;

import com.alexaws.s3.service.FileStoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/buckets")
public class BucketController {

    private final FileStoreService fileStoreService;

    @Autowired
    public BucketController(FileStoreService fileStoreService) {
        this.fileStoreService = fileStoreService;
    }

    @GetMapping
    public List<String> getBuckets() {
        return fileStoreService.getBuckets();
    }

    @PostMapping
    public String createBuckets(@RequestBody Map<String,String> bucketName) {
        return fileStoreService.createBucket(bucketName.get("bucketName"));
    }

    @DeleteMapping
    public boolean deleteBucket(@RequestBody Map<String,String> bucketName) {
        return fileStoreService.deleteBucket(bucketName.get("bucketName"));
    }

    @GetMapping("/{bucketName}/files")
    public List<String> getBucketFiles(@PathVariable String bucketName) {
        return fileStoreService.getAllFiles(bucketName);
    }

    @GetMapping("/{bucketName}/files/{filePath}/versions")
    public List<String> getFileVersions(@PathVariable String bucketName,
                                        @PathVariable String filePath) {
        return fileStoreService.getFileVersions(bucketName, filePath);
    }

    @GetMapping("/{bucketName}/files/{filePath}")
    public String downloadFile(@PathVariable String bucketName,
                               @PathVariable String filePath,
                               @RequestParam(required = false) String versionId) {
        fileStoreService.downloadFile(bucketName, filePath, versionId);
        File file = new File("savedFiles/" + filePath);
        if (file.exists()) {
            return "File " + filePath + " saved successfully!";
        } else {
            return "Error during saving file " + filePath + " .";
        }
    }

    @PostMapping("{bucketName}/files")
    public void uploadFile(@PathVariable("bucketName") String bucketName,
                           @RequestPart("file") MultipartFile file) {
        fileStoreService.uploadFile(bucketName, file);
    }

    @DeleteMapping("/{bucketName}/files")
    public boolean deleteFile(@PathVariable("bucketName") String bucketName,
                              @RequestBody Map<String,String> fileDescription) {
        return fileStoreService.delete(bucketName, fileDescription.get("filePath"));
    }

    @DeleteMapping("/{bucketName}/files/versions")
    public boolean deleteFileVersion(@PathVariable("bucketName") String bucketName,
                              @RequestBody Map<String,String> fileDescription) {
        return fileStoreService.deleteVersion(bucketName, fileDescription.get("filePath"), fileDescription.get("versionId"));
    }

}
