/* Copyright (C) 2025 */
package org.example.im.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/** 文件处理工具类 */
@Component
public class FileUtil {

  private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

  @Autowired private MinioClientUtil minioClientUtil;

  /**
   * 存储文件到MinIO并生成文件URL
   */
  public String storageFileURL(String roomId, String fileName, byte[] fileContent) {
    try {
      // 生成MinIO中的文件路径，格式: im/{roomId}/{datasetId}/{fileName}
      String objectName = String.format("im/%s/%s", roomId, fileName);

      logger.info("Uploading file to MinIO: {}", objectName);

      // 根据文件扩展名确定Content-Type
      String contentType = getContentType(fileName);

      // 上传文件到MinIO
      return minioClientUtil.uploadFile(fileContent, objectName, contentType);

    } catch (Exception e) {
      logger.error("Failed to upload file to MinIO: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to upload file to MinIO: " + e.getMessage(), e);
    }
  }

  /**
   * 从MinIO下载文件
   */
  public byte[] downloadFile(String roomId, String fileName) {
    try {
      // 生成MinIO中的文件路径
      String objectName = String.format("im/%s/%s", roomId, fileName);

      logger.info("Downloading file from MinIO: {}", objectName);

      // 从MinIO下载文件
      byte[] fileContent = minioClientUtil.downloadFile(objectName);

      logger.info("Successfully downloaded file from MinIO: {}", objectName);

      return fileContent;

    } catch (Exception e) {
      logger.error("Failed to download file from MinIO: {}", e.getMessage(), e);
      throw new RuntimeException("Failed to download file from MinIO: " + e.getMessage(), e);
    }
  }

  /**
   * 搜索数据集目录下的所有文件
   */
  public List<String> searchFilesInDataset(String roomId) {
    try {
      // 生成MinIO中的目录路径
      String directoryPrefix = String.format("im/%s/", roomId);

      logger.info("Searching files in directory: {}", directoryPrefix);

      // 从MinIO搜索目录下的所有文件
      List<String> fileNamesSource = minioClientUtil.listFiles(directoryPrefix);


      List<String> fileNames = new ArrayList<>();

      for (String fileName : fileNamesSource) {
        // 找到最后一个路径分隔符的位置（兼容 Windows 的 '\' 和 Unix 的 '/'）
        int lastIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
        // 如果存在分隔符，截取后面的部分；否则保留全名
        String simpleName = (lastIndex >= 0) ? fileName.substring(lastIndex + 1) : fileName;
        fileNames.add(simpleName);
      }

      logger.info("Found {} files in directory: {}", fileNames.size(), directoryPrefix);

      return fileNames;

    } catch (Exception e) {
      logger.error("Failed to search files in directory: {}", e.getMessage(), e);
      return new ArrayList<>();
    }
  }

  /**
   * 根据文件名获取Content-Type
   *
   * @param fileName 文件名
   * @return Content-Type
   */
  private String getContentType(String fileName) {
    if (fileName == null || fileName.trim().isEmpty()) {
      return "application/octet-stream";
    }

    String lowerCaseFileName = fileName.toLowerCase();

    if (lowerCaseFileName.endsWith(".xlsx")) {
      return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    } else if (lowerCaseFileName.endsWith(".xls")) {
      return "application/vnd.ms-excel";
    } else if (lowerCaseFileName.endsWith(".pdf")) {
      return "application/pdf";
    } else if (lowerCaseFileName.endsWith(".txt")) {
      return "text/plain";
    } else if (lowerCaseFileName.endsWith(".doc")) {
      return "application/msword";
    } else if (lowerCaseFileName.endsWith(".docx")) {
      return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    } else {
      return "application/octet-stream";
    }
  }
}
