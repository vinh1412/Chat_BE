/*
 * @ {#} Friend.java   1.0     18/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
@Document(collection = "conversations")
public class Conversation {
    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    private String name;
    private String avatar;
    @Field("is_group")
    private boolean isGroup;

    private ObjectId lastMessageId; // Lưu messageId cuối cùng
    private Instant createdAt;

    @JsonSerialize(contentUsing = ObjectIdSerializer.class)
    private Set<ObjectId> memberId;

    @JsonSerialize(contentUsing = ObjectIdSerializer.class)
    private Set<ObjectId> messageIds = new HashSet<>();

    private List<ObjectId> pinnedMessages = new ArrayList<>();

    private boolean dissolved;
    private ObjectId dissolvedBy;
    private Instant dissolvedAt;
    private Set<ObjectId> removedByUserIds = new HashSet<>();
    public Set<ObjectId> getMemberId() {
        return memberId;
    }

    private String linkGroup;

    private boolean restrictMessagingToAdmin = false;


}
