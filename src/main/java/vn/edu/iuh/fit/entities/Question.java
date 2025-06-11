/**
 * @ (#) Question.java      5/28/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import org.bson.types.ObjectId;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.util.Set;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/28/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    private String content;

    @JsonSerialize(contentUsing = ObjectIdSerializer.class)
    @JsonDeserialize(contentUsing = ObjectIdDeserializer.class)
    private Set<ObjectId> userIds;
}
