package com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.User;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userReposioryInterface) {
            this.userRepository = userReposioryInterface;
    }
    public User findByEmail(String email){
        return userRepository.findUserByEmail(email);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public boolean isEmailExist(String email){
        return userRepository.existsByEmail(email);
    }


}
