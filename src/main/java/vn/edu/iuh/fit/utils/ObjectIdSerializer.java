/**
 * @ (#) ObjectIdSerializer.java      4/9/2025
 * <p>
 * Copyright (c) 2025 IUH. All rights reserved
 */

package vn.edu.iuh.fit.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

/*
 * @description:
 * @author: Sinh Phan Tien
 * @date: 4/9/2025
 */

/**
 * Chuyen doi ObjectId sang String
 */
public class ObjectIdSerializer extends JsonSerializer<ObjectId> {
    @Override
    public void serialize(ObjectId objectId, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (objectId != null) {
            gen.writeString(objectId.toHexString());
        } else {
            gen.writeNull();
        }
    }
}
