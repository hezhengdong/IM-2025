package org.example.im.file;

import org.example.im.common.dto.Result;
import org.example.im.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileUtil fileUtil;

    @GetMapping("/{roomId}/files")
    public Result getFileNames(@PathVariable String roomId) {
        try {
            List<String> names = fileUtil.searchFilesInDataset(roomId);
            return Result.success(names);
        } catch (Exception e) {
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/{roomId}/upload")
    public Result uploadFile(@PathVariable String roomId, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            String fileName = file.getOriginalFilename();
            byte[] fileContent = file.getBytes();
            String url = fileUtil.storageFileURL(roomId, fileName, fileContent);
            return Result.success(url);
        } catch (IOException e) {
            return Result.error("文件上传失败: " + e.getMessage());
        }
    }

    @GetMapping("/{roomId}/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String roomId, @PathVariable String fileName) {
        try {
            byte[] bytes = fileUtil.downloadFile(roomId, fileName);
            System.out.println("Downloaded file: " + fileName);
            System.out.println("File size: " + bytes.length);
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
