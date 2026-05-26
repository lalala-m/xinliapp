package com.tongyangyuan.mentalhealth.controller;

import com.tongyangyuan.mentalhealth.dto.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class FileUploadController {

    @PostMapping("/video")
    public ApiResponse<String> uploadVideo(@RequestParam("file") MultipartFile file) {
        return doUploadVideo(file, "/uploads/videos/");
    }

    /**
     * Web端视频存档上传接口
     * 用于 Web 端视频通话录制上传
     */
    @PostMapping("/chat-video")
    public ApiResponse<String> uploadChatVideo(@RequestParam("file") MultipartFile file) {
        return doUploadVideo(file, "/uploads/videos/");
    }

    /**
     * 通用视频上传处理
     */
    private ApiResponse<String> doUploadVideo(MultipartFile file, String relativePath) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType != null && !contentType.startsWith("video/")) {
            // 允许 webm 类型
            String filename = file.getOriginalFilename();
            if (filename != null && !filename.toLowerCase().endsWith(".webm")) {
                return ApiResponse.error("只支持视频文件上传");
            }
        }

        try {
            // 获取当前工作目录
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + relativePath;

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名，保持原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest);

            // 返回访问URL
            String fileUrl = relativePath + newFilename;

            return ApiResponse.success("上传成功", fileUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/image")
    public ApiResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        try {
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + "/uploads/images/";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest);

            String fileUrl = "/uploads/images/" + newFilename;

            return ApiResponse.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    @PostMapping("/audio")
    public ApiResponse<String> uploadAudio(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        try {
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + "/uploads/audios/";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // 确保使用 .m4a 扩展名（AAC编码的音频）
            if (extension.isEmpty() || !extension.equalsIgnoreCase(".m4a")) {
                extension = ".m4a";
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest);

            String fileUrl = "/uploads/audios/" + newFilename;

            return ApiResponse.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}
