/*
 * @ {#} ObjectIdSetDeserializer.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */
public class ObjectIdSetDeserializer extends JsonDeserializer<Set<ObjectId>> {
    @Override
    public Set<ObjectId> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Set<ObjectId> objectIds = new HashSet<>();

        // Đọc JSON và chuyển thành một mảng hoặc danh sách
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Iterator<JsonNode> elements = node.elements();

        // Chuyển mỗi chuỗi thành ObjectId và thêm vào Set
        while (elements.hasNext()) {
            String objectIdString = elements.next().asText();
            objectIds.add(new ObjectId(objectIdString));
        }

        return objectIds;
    }
}
