/**
 * @ (#) PostUserActivity.java      5/25/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.entities;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/25/2025
 */
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "postactivity")
public class PostUserActivity {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId userIdActor;  // người thực hiện hành động (thích, bình luận)

    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId userIdPost;   // người đăng bài

    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId postId;

    private boolean tym;

    private String comment;

    private LocalDateTime activityTime;
}