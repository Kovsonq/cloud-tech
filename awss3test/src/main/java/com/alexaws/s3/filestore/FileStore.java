package com.alexaws.s3.filestore;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FileStore {

    private final AmazonS3 s3Client;

    @Autowired
    public FileStore(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }

    public List<String> getBuckets() {
        return s3Client.listBuckets().stream()
                .map(Bucket::getName)
                .collect(Collectors.toList());
    }

    public List<String> getAllFiles(String bucketName) {
        return s3Client.listObjects(bucketName).getObjectSummaries().stream()
                .map(s3obj -> String.format("%s-%s-%s", s3obj.getKey(), s3obj.getSize(), s3obj.getOwner().getDisplayName()))
                .collect(Collectors.toList());
    }

    public List<String> getFileVersions(String bucketName, String filePath) {
        return s3Client.listVersions(bucketName, filePath).getVersionSummaries().stream()
                .map(summary -> String.format("%s-%s", summary.getVersionId(), summary.getLastModified()))
                .collect(Collectors.toList());
    }

    public void downloadFile(String bucketName, String fileName, String versionId) {
        S3Object s3object = getS3Object(bucketName, fileName, versionId);
        S3ObjectInputStream inputStream = s3object.getObjectContent();

        try {
            FileUtils.copyInputStreamToFile(inputStream, new File("savedFiles/" + fileName));
        } catch (IOException e) {
            throw new IllegalStateException("Error during downloading file [ " + fileName + " ]", e);
        }
    }

    private S3Object getS3Object(String bucketName, String fileName, String versionId) {
        S3Object s3object;
        if (versionId == null) {
            s3object = s3Client.getObject(bucketName, fileName);
        } else {
            s3object = s3Client.getObject(new GetObjectRequest(bucketName, fileName, versionId));
        }
        return s3object;
    }

    public void uploadFile(String path, String fileName, Optional<Map<String, String>> optionalMetadata,
                           InputStream inputStream) {
        ObjectMetadata objectMetadata = getObjectMetadata(optionalMetadata);

        try {
            s3Client.putObject(path, fileName, inputStream, objectMetadata);
        } catch (AmazonS3Exception e) {
            throw new IllegalStateException("Failed to store content to S3", e);
        }
    }

    private ObjectMetadata getObjectMetadata(Optional<Map<String, String>> optionalMetadata) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        optionalMetadata.ifPresent(map -> {
            if (!map.isEmpty()) {
                map.forEach(objectMetadata::addUserMetadata);
            }
        });
        return objectMetadata;
    }

    public boolean deleteFile(String bucketName, String fileName) {
        s3Client.deleteObject(bucketName, fileName);

        try {
            getS3Object(bucketName, fileName, null);
            return false;
        } catch (AmazonS3Exception e) {
            return true;
        }
    }

    public boolean deleteVersion(String bucketName, String fileName , String versionId) {
        s3Client.deleteVersion(new DeleteVersionRequest(bucketName, fileName, versionId));

        try {
            getS3Object(bucketName, versionId, versionId);
            return false;
        } catch (AmazonS3Exception e) {
            return true;
        }
    }

    public boolean deleteBucket(String bucketName) {
        s3Client.deleteBucket(bucketName);
        return true;
    }

    public String createBucket(String bucketName) {
        if (s3Client.doesBucketExistV2(bucketName)) {
            throw new IllegalStateException(String.format("Bucket with name %s already exist.", bucketName));
        }
        s3Client.createBucket(bucketName);
        return String.format("Bucket with name %s successfully created.", bucketName);
    }
}
