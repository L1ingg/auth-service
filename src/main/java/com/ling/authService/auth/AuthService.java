package com.ling.authService.auth;

import com.ling.authService.auth.dto.login.LoginResponse;
import com.ling.authService.auth.dto.register.RegisteredEvent;
import com.ling.authService.auth.email.EmailNotVerifiedException;
import com.ling.authService.auth.email.MailService;
import com.ling.authService.security.jwt.JwtService;
import com.ling.authService.security.jwt.TokenType;
import com.ling.authService.user.MyCustomUserDetails;
import com.ling.authService.user.MyCustomUserDetailsService;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Random;

@Service
public class AuthService {
    private final MyCustomUserDetailsService myCustomUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final String TOPIC_NAME = "user.registered";
    private final MailService mailService;
    private final Random random = new Random();

    public AuthService(MyCustomUserDetailsService myCustomUserDetailsService, PasswordEncoder passwordEncoder, JwtService jwtService, KafkaTemplate<String, String> kafkaTemplate, MailService mailService) {
        this.myCustomUserDetailsService = myCustomUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaTemplate = kafkaTemplate;
        this.mailService = mailService;
    }

    @Transactional
    public void register(String username, String email, String password) {
        MyCustomUserDetails myCustomUserDetails = myCustomUserDetailsService.create(new MyCustomUserDetails(username, email, passwordEncoder.encode(password), "ROLE_USER", false));
        kafkaTemplate.send(TOPIC_NAME, mapper.writeValueAsString(new RegisteredEvent(myCustomUserDetails.getUuid().toString(), username, email)));
        mailService.request(email, String.valueOf(100000 + random.nextInt(900000)));
    }

    public LoginResponse login(String email, String password) {
        MyCustomUserDetails myCustomUserDetails = myCustomUserDetailsService.getUserDetailsByEmail(email);

        if (!passwordEncoder.matches(password, myCustomUserDetails.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!myCustomUserDetails.isEmailVerified()) {
            throw new EmailNotVerifiedException(email + " not verified");
        }

        String accessToken = jwtService.generateToken(myCustomUserDetails, Duration.ofMinutes(15), TokenType.ACCESS);
        String refreshToken = jwtService.generateToken(myCustomUserDetails, Duration.ofDays(30), TokenType.REFRESH);

        return new LoginResponse(accessToken, refreshToken);
    }
}
