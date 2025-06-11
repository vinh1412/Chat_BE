/**
 * @ (#) FriendRequestReq.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class FriendRequestReq {
//    @JsonSerialize(using = ObjectIdSerializer.class)
//    @JsonDeserialize(using = ObjectIdDeserializer.class)
//    private ObjectId senderId;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId receiverId;
}
