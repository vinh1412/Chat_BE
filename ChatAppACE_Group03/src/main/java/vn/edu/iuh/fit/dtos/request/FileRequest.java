package vn.edu.iuh.fit.dtos.request;/*
 * @description:
 * @author: TienMinhTran
 * @date: 18/4/2025
 * @time: 12:58 AM
 * @nameProject: Project_Architectural_Software
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class FileRequest {
    private String sender;
    private String receiver;
    private String messageId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Instant uploadedAt;
}
