/*
 * @ {#} FriendRequestReq.java   1.0     18/03/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.entities;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.enums.RequestFriendStatus;

import java.time.Instant;

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
@Document(collection = "friend_requests")
public class FriendRequest {
    @Id
    private ObjectId id;
    private ObjectId sender;
    private ObjectId receiver;
    private RequestFriendStatus status;
    private Instant sendAt;

}
