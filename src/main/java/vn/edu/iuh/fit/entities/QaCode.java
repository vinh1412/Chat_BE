package vn.edu.iuh.fit.entities;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "qacode")
public class QaCode {
    @Id
    private ObjectId id;
    private Boolean status;
    private String sessionId;

    private ObjectId userId;
    private String token;

}
