/*
 * @ {#} MemberResponse.java   1.0     22/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.dtos.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.enums.MemberRoles;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   22/04/2025
 * @version:    1.0
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MemberResponse {
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;
    @JsonProperty("display_name")
    private String displayName;
    private String avatar;
    private MemberRoles role;
    private String phone;
}
