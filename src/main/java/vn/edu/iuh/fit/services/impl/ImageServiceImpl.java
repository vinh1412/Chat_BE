package vn.edu.iuh.fit.services.impl;/*
 * @description:
 * @author: TienMinhTran
 * @date: 18/4/2025
 * @time: 1:05 AM
 * @nameProject: Project_Architectural_Software
 */

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.dtos.request.FileRequest;
import vn.edu.iuh.fit.entities.File;
import vn.edu.iuh.fit.repositories.ImageRerpository;
import vn.edu.iuh.fit.services.ImageService;

@Service
public class ImageServiceImpl implements ImageService {

    @Autowired
    private ImageRerpository imageRrpository;
    @Override
    public String uploadImage(String token, String imagePath, String imageName) {
        return null;
    }

    @Override
    public String getImage(String token, String imageId) {
        return null;
    }

    @Override
    public boolean deleteImage(String token, String imageId) {
        return false;
    }

    @Override
    public boolean updateImage(String token, String imageId, String newImagePath) {
        return false;
    }


    @Override
    public void saveImage(FileRequest FIleRequest) {
        try {
            if (FIleRequest.getFileUrl() == null || FIleRequest.getFileName() == null) {
                throw new IllegalArgumentException("File URL or File Name cannot be null");
            }

//            ObjectId senderId = new ObjectId(FileRequest.getSender());
//            ObjectId receiverId = new ObjectId(FileRequest.getReceiver());

            File.FileBuilder fileBuilder = File.builder()
                    .id(new ObjectId())
//                    .sender(senderId)
//                    .receiver(receiverId)
                    .fileName(FIleRequest.getFileName())
                    .fileType(FIleRequest.getFileType())
                    .fileUrl(FIleRequest.getFileUrl())
                    .uploadedAt(FIleRequest.getUploadedAt());
            if (FIleRequest.getMessageId() != null && !FIleRequest.getMessageId().isEmpty()) {
                fileBuilder.messageId(new ObjectId(FIleRequest.getMessageId()));
            }

            imageRrpository.save(fileBuilder.build());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save image: " + e.getMessage(), e);
        }
    }

}

