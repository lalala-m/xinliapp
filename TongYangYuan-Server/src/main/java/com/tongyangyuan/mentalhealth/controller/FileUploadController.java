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
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        try {
            // 获取当前工作目录
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + "/uploads/videos/";
            
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 生成唯一文件名
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
            // 注意：这里假设服务器端口是8080，且配置了资源映射
            // 实际生产环境可能需要更复杂的URL生成逻辑
            String fileUrl = "/uploads/videos/" + newFilename;
            
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

    /**
     * 上传用户头像
     * POST /upload/avatar
     */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ApiResponse.error("请选择要上传的文件");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ApiResponse.error("请上传图片文件");
        }

        // 限制文件大小为 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return ApiResponse.error("图片大小不能超过5MB");
        }

        try {
            String projectDir = System.getProperty("user.dir");
            String uploadDir = projectDir + "/uploads/avatars/";

            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 保留原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            // 默认使用 .jpg
            if (extension.isEmpty() || !extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
                extension = ".jpg";
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            File dest = new File(uploadDir + newFilename);
            file.transferTo(dest);

            // 返回访问URL
            String fileUrl = "/uploads/avatars/" + newFilename;

            return ApiResponse.success("头像上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        }
    }
}
