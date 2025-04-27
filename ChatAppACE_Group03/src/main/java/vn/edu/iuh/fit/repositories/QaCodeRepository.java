package vn.edu.iuh.fit.repositories;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.fit.entities.QaCode;

@Repository
public interface QaCodeRepository extends MongoRepository<QaCode, ObjectId> {
    QaCode findBySessionId(String sessionId); // tim kiem qa code theo session id
}
