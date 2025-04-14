/*
 * @ {#} ConservationDTO.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;
import vn.edu.iuh.fit.utils.ObjectIdSetDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSetSerializer;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

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
public class ConversationDTO {
    @JsonSerialize(using = ObjectIdSerializer.class)
//    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;
    private String name;
    private String avatar;

    @JsonProperty("is_group")
    private boolean isGroup;

    @JsonProperty("last_message_id")
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId lastMessageId;
    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("member_id")
    @JsonSerialize(using = ObjectIdSetSerializer.class)
    @JsonDeserialize(using = ObjectIdSetDeserializer.class)
    private Set<ObjectId> memberId= new HashSet<>();


    @JsonProperty("message_ids")
    private Set<ObjectId> messageIds = new HashSet<>();
}
