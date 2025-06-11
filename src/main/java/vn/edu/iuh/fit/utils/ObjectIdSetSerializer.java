/*
 * @ {#} ObjectIdSetSerializer.java   1.0     14/04/2025
 *
 * Copyright (c) 2025 IUH. All rights reserved.
 */

package vn.edu.iuh.fit.utils;

/*
 * @description:
 * @author: Tran Hien Vinh
 * @date:   14/04/2025
 * @version:    1.0
 */

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.Set;

public class ObjectIdSetSerializer extends JsonSerializer<Set<ObjectId>> {
    @Override
    public void serialize(Set<ObjectId> objectIds, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, IOException {
        jsonGenerator.writeStartArray();
        for (ObjectId objectId : objectIds) {
            jsonGenerator.writeString(objectId.toHexString()); // Ghi mỗi ObjectId dạng chuỗi hex
        }
        jsonGenerator.writeEndArray(); // Kết thúc mảng JSON
    }
}
