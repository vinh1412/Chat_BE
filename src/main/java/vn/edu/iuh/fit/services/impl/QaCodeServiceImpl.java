package vn.edu.iuh.fit.services.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.entities.QaCode;
import vn.edu.iuh.fit.entities.User;
import vn.edu.iuh.fit.repositories.QaCodeRepository;
import vn.edu.iuh.fit.repositories.UserRepository;
import vn.edu.iuh.fit.services.QaCodeService;

@Service
public class QaCodeServiceImpl implements QaCodeService {

    @Autowired
    private QaCodeRepository qaCodeRepository;

    @Autowired
    private UserRepository userRepository;


    @Override
    public QaCode saveQaCode(String sessionId, Boolean status, ObjectId userId, String token) {
        QaCode qaCode = new QaCode();
        qaCode.setSessionId(sessionId);
        qaCode.setStatus(status);
        qaCode.setUserId(userId);
        qaCode.setToken(token);

        return qaCodeRepository.save(qaCode);
    }

    @Override
    public Boolean checkStatus(String sessionId) {
        QaCode qaCode = qaCodeRepository.findBySessionId(sessionId);
        if (qaCode != null) {
            return qaCode.getStatus();
        }
        return null; // mã QR không tồn tại
    }
    @Override
    public void updateStatus(String sessionId, Boolean status) {

    }

    @Override
    public QaCode findUserIdBySessionId(String sessionId) {
        QaCode qaCode = qaCodeRepository.findBySessionId(sessionId);
        System.out.println("qaCode: " + qaCode);
       if(qaCode != null) {
           return qaCode;
       }
       return null;
    }

}
