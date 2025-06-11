package vn.edu.iuh.fit.services;/*
 * @description:
 * @author: TienMinhTran
 * @date: 18/4/2025
 * @time: 1:05 AM
 * @nameProject: Project_Architectural_Software
 */

import vn.edu.iuh.fit.dtos.request.FileRequest;

public interface ImageService {

    String uploadImage(String token, String imagePath, String imageName);

    String getImage(String token, String imageId);

    boolean deleteImage(String token, String imageId);

    boolean updateImage(String token, String imageId, String newImagePath);


     void saveImage(FileRequest FIleRequest);
}
