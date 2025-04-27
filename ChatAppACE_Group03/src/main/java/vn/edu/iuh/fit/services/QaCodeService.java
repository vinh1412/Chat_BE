package vn.edu.iuh.fit.services;

import org.bson.types.ObjectId;
import vn.edu.iuh.fit.entities.QaCode;
import vn.edu.iuh.fit.entities.User;

public interface QaCodeService {
    QaCode saveQaCode(String sessionId, Boolean status, ObjectId userId, String token);

    Boolean checkStatus(String sessionId);

    void updateStatus(String sessionId, Boolean status);

    //    // TIM KIEM IDUSER THEO SESSIONID
    QaCode findUserIdBySessionId(String sessionId);
}
