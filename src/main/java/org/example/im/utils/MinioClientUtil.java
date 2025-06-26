/* Copyright (C) 2025 */
package org.example.im.utils;

import io.minio.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/** MinIO客户端工具类 */
@Component
public class MinioClientUtil {
  private static final Logger logger = LoggerFactory.getLogger(MinioClientUtil.class);

  private final MinioClient minioClient;
  private final String bucketName;
  private final String endpoint;

  @Autowired
  public MinioClientUtil(
      MinioClient minioClient,
      @org.springframework.beans.factory.annotation.Value("${minio.bucket-name}") String bucketName,
      @org.springframework.beans.factory.annotation.Value("${minio.endpoint}") String endpoint) {
    this.minioClient = minioClient;
    this.bucketName = bucketName;
    this.endpoint = endpoint;

    try {
      // 确保bucket存在
      boolean bucketExists =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!bucketExists) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        logger.info("Created bucket: {}", bucketName);
      }
      logger.info("MinIO client initialized successfully");
    } catch (Exception e) {
      logger.error("Failed to initialize MinIO client: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * 上传文件到MinIO
   *
   * @param fileData 文件数据
   * @param fileName 文件名
   * @param contentType 文件类型
   * @return 文件URL
   */
  public String uploadFile(byte[] fileData, String fileName, String contentType) {
    try {
      // 将bytes转换为InputStream
      InputStream inputStream = new ByteArrayInputStream(fileData);

      // 上传文件
      minioClient.putObject(
          PutObjectArgs.builder().bucket(bucketName).object(fileName).stream(
                  inputStream, fileData.length, -1)
              .contentType(contentType)
              .build());

      // 返回文件URL
      return "http://" + endpoint + "/" + bucketName + "/" + fileName;

    } catch (Exception e) {
      logger.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /**
   * 从MinIO下载文件
   *
   * @param fileName 文件名
   * @return 文件数据
   */
  public byte[] downloadFile(String fileName) {
    try {
      // 获取对象
      InputStream stream =
          minioClient.getObject(
              GetObjectArgs.builder().bucket(bucketName).object(fileName).build());

      // 读取数据
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      byte[] buffer = new byte[4096];
      int bytesRead;
      while ((bytesRead = stream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
      }

      return outputStream.toByteArray();
    } catch (Exception e) {
      logger.error("Failed to download file from MinIO: {}", e.getMessage(), e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }


  /**
   * 列出目录下的所有文件
   *
   * @param directoryPrefix 目录前缀
   * @return 文件名列表
   */
  public List<String> listFiles(String directoryPrefix) {
    try {
      List<String> fileNames = new ArrayList<>();

      // 使用MinIO的listObjects方法列出文件
      Iterable<io.minio.Result<io.minio.messages.Item>> results =
              minioClient.listObjects(
                      io.minio.ListObjectsArgs.builder()
                              .bucket(bucketName)
                              .prefix(directoryPrefix)
                              .build());

      for (io.minio.Result<io.minio.messages.Item> result : results) {
        io.minio.messages.Item item = result.get();
        String objectName = item.objectName();

        // 只添加文件，不添加目录
        if (!objectName.endsWith("/")) {
          // 检查是否包含URL编码字符，如果有则解码
          if (objectName.contains("%")) {
            try {
              // 对文件名进行URL解码，解决中文路径问题
              String decodedObjectName = java.net.URLDecoder.decode(objectName, "UTF-8");
              fileNames.add(decodedObjectName);
            } catch (java.io.UnsupportedEncodingException e) {
              // 如果解码失败，使用原始文件名
              logger.warn("Failed to decode object name: {}, using original name", objectName);
              fileNames.add(objectName);
            }
          } else {
            // 没有URL编码，直接使用原始文件名
            fileNames.add(objectName);
          }
        }
      }

      logger.info("Found {} files in directory: {}", fileNames.size(), directoryPrefix);
      return fileNames;

    } catch (Exception e) {
      String errorMsg = "Failed to list files from MinIO: " + e.getMessage();
      logger.error(errorMsg, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, errorMsg);
    }
  }
}
