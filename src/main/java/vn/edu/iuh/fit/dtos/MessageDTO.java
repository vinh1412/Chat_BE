/*
 * @ {#} MessageDTO.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.enums.MessageType;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId senderId;
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId conversationId;

    private String content; // dùng cho text hoặc emoji
    private MessageType messageType;

    private String fileUrl; // dùng cho sticker hoặc gif

    private Instant timestamp;
    private boolean isSeen;

    private boolean recalled = false;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId replyToMessageId;

    private Map<String, List<ObjectId>> reactions;

    private List<ObjectId> deletedBy;

    // Quan hệ với FIle
    private List<ObjectId> fileIds;

    private String senderName;

}
