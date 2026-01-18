package com.forensicintelligencethreatreport.forensicintelligencethreatreport.controller;

import com.forensicintelligencethreatreport.forensicintelligencethreatreport.auth.AuthenticationResponse;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.auth.JwtUtil;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.LoginRequest;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.dto.UserDTO;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.Role;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.model.User;
import com.forensicintelligencethreatreport.forensicintelligencethreatreport.service.impl.UserService;
import jakarta.validation.Valid;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping(value="/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody UserDTO userRegistrationDTO)
    {
        if(userService.isEmailExist(userRegistrationDTO.email)) {
            return new ResponseEntity<>("Email exits", HttpStatus.BAD_REQUEST);
        }

        User savedUser = new User();
        savedUser.setEmail(userRegistrationDTO.email);
        savedUser.setName(userRegistrationDTO.name);
        savedUser.setSurname(userRegistrationDTO.surname);
        savedUser.setRole(Role.USER);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode(userRegistrationDTO.password);
        savedUser.setPassword(hash);
        savedUser.setVerified(true);

        UserDTO userDTO = new UserDTO(savedUser);
        System.out.println(userDTO.name + userDTO.email);
        try{
            userService.save(savedUser);
            return new ResponseEntity<>("User successefully created", HttpStatus.CREATED);

        }catch (Exception e){
            return new ResponseEntity<>("Cannot send verification email",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value="/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest logInRequest){

        User user = userService.findByEmail(logInRequest.getEmail());
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if(user==null) {
            return new ResponseEntity<>("Unsuccessful login - user doesn't exist",HttpStatus.NOT_FOUND);
        }
        if (!encoder.matches(logInRequest.getPassword(), user.getPassword())) {
            return new ResponseEntity<>("Unsuccessful login - wrong password", HttpStatus.UNAUTHORIZED);
        }
        if (!user.isVerified()) {
            return new ResponseEntity<>("Unsuccessful login - mail not verified", HttpStatus.NOT_ACCEPTABLE);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        AuthenticationResponse response = new AuthenticationResponse(token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
