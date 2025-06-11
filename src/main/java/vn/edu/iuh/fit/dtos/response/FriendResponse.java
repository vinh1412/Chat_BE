/**
 * @ (#) FriendResponse.java      4/14/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.dtos.response;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/14/2025
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FriendResponse {

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private Object userId;
    private String displayName;
    private String avatar;
}
