package com.forensicintelligencethreatreport.forensicintelligencethreatreport.repository;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Integer> {

    public User findUserByEmail(String email);
    public Optional<User> findByEmail(String email);
    public boolean existsByEmail(String email);
}
