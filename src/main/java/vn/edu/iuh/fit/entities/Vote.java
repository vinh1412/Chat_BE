/**
 * @ (#) Vote.java      5/27/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.type.DateTime;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import vn.edu.iuh.fit.utils.ObjectIdDeserializer;
import vn.edu.iuh.fit.utils.ObjectIdSerializer;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/*
 * @description:
 * @author: Tien Minh Tran
 * @date: 5/27/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document(collection = "votes")
public class Vote {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId voteId;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId groupId;

    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId userIdCreator;

    @JsonSerialize(contentUsing = ObjectIdSerializer.class)
    @JsonDeserialize(contentUsing = ObjectIdDeserializer.class)
    private Set<ObjectId> userIdMembers;

    private String title;

    private List<Question> questions;

    private boolean setTime;

    private Instant createdAt;

    private Instant endAt;

    private boolean isResult;

    private boolean isMultipleChoice;

    private boolean isAddOption;
}
