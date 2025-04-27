/**
 * @ (#) CloudinaryServiceImpl.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.iuh.fit.config.CloudinaryConfig;
import vn.edu.iuh.fit.services.CloudinaryService;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public String uploadImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/") && !contentType.equals("application/octet-stream"))) {
            throw new IllegalArgumentException("File is not an image!");
        }
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("resource_type", "image");
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            String url = result.get("secure_url").toString();
            System.out.println("Uploaded image URL: " + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty!");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit!");
        }

        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("resource_type", "raw");
            uploadOptions.put("access_mode", "public"); // Đảm bảo file công khai

            // Thêm phần mở rộng file vào public_id
            String contentType = file.getContentType();
            String extension = "";
            String publicId = UUID.randomUUID().toString();
            if (contentType != null) {
                switch (contentType) {
                    case "application/pdf":
                        extension = ".pdf";
                        break;
                    case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                        extension = ".docx";
                        break;
                    case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                        extension = ".xlsx";
                        break;
                    case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                        extension = ".pptx";
                        break;
                }
                publicId += extension;
            }
            uploadOptions.put("public_id", publicId);

            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            String secureUrl = result.get("secure_url").toString();
            System.out.println("Cloudinary upload result: " + result); // Debug

            // Trả về URL xem trước
            if (contentType != null) {
                if (contentType.equals("application/pdf")) {
                    // Xem PDF trực tiếp hoặc dùng Google Docs Viewer
                    return secureUrl.replace("/upload/", "/upload/fl_attachment=false/"); // Đảm bảo Content-Disposition: inline
                    // Hoặc: return "https://docs.google.com/viewer?url=" + URLEncoder.encode(secureUrl, "UTF-8") + "&embedded=true";
                } else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                        contentType.equals("application/vnd.openxmlformats-officedocument.presentationml.presentation")) {
                    return "https://view.officeapps.live.com/op/view.aspx?src=" + URLEncoder.encode(secureUrl, "UTF-8");
                }
            }
            return secureUrl; // Trả về link tải cho các file khác
        } catch (IOException e) {
            throw new RuntimeException("Error reading file bytes: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Cloudinary: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String publicId) {
        // Delete the image from Cloudinary
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    @Override
    public String uploadVideo(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            throw new IllegalArgumentException("File is not a video!");
        }
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new IllegalArgumentException("Video size exceeds 50MB limit!");
        }
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("resource_type", "video");
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), uploadOptions);
            String url = result.get("secure_url").toString();
            System.out.println("Uploaded video URL: " + url);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload video: " + e.getMessage(), e);
        }
    }
}
