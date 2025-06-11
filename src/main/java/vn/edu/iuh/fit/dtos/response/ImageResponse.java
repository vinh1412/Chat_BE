package vn.edu.iuh.fit.dtos.response;/*
 * @description:
 * @author: TienMinhTran
 * @date: 18/4/2025
 * @time: 12:59 AM
 * @nameProject: Project_Architectural_Software
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageResponse implements Serializable{

    private ObjectId id;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Instant uploadedAt;
}
