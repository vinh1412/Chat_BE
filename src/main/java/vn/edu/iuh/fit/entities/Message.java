/*
 * @ {#} Friend.java   1.0     18/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;
import vn.edu.iuh.fit.utils.ObjectIdSetDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSetSerializer;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   18/03/2025
 * @version:    1.0
 */
@Data
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")
public class Message {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId senderId;// Người gửi
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId conversationId;// ID cuộc trò chuyện
    @Field("receiverId")
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId receiverId;
    // ID người nhận

    private String content;                     // Nội dung tin nhắn (text)
    private MessageType messageType;

    private String fileUrl;               // Link file nếu là ảnh/video/file

    private Instant timestamp;                  // Thời gian gửi
    private boolean isSeen;

    private boolean recalled;

    private ObjectId replyToMessageId;          // Phản hồi tin nhắn nào (nếu có)

    private Map<String, List<ObjectId>> reactions; // Reaction voi tin nhan

    private List<ObjectId> deletedBy;

    // Quan hệ với FIle
    private List<ObjectId> fileIds; // Danh sách fileId nếu là ảnh/video/file

    @JsonSerialize(using = ObjectIdSetSerializer.class)
    @JsonDeserialize(using = ObjectIdSetDeserializer.class)
    private Set<ObjectId> deletedByUserIds; // Danh sách người dùng đã xóa tin nhắn này

    private boolean pinned;

    private List<PollOption> pollOptions;

    private String senderName;
}

