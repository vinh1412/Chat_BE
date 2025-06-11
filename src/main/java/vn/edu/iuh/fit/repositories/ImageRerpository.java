package vn.edu.iuh.fit.repositories;/*
 * @description:
 * @author: TienMinhTran
 * @date: 18/4/2025
 * @time: 1:03 AM
 */

import org.springframework.stereotype.Repository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import vn.edu.iuh.fit.entities.File;
@Repository
public interface ImageRerpository extends MongoRepository<File, ObjectId> {



}
