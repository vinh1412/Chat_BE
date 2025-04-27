/**
 * @ (#) CloudinaryService.java      4/10/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.services;

import org.springframework.web.multipart.MultipartFile;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/10/2025
 */
public interface CloudinaryService {
    public String uploadImage(MultipartFile file);
    public String uploadFile(MultipartFile file);
    public String uploadVideo(MultipartFile file);
    public void deleteImage(String publicId);
}
