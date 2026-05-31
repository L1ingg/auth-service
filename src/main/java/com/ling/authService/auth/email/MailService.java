package com.ling.authService.auth.email;

import com.ling.authService.user.MyCustomUserDetails;
import com.ling.authService.user.MyCustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class MailService {

    private final JavaMailSender sender;
    private final RedisTemplate<String, String> redis;
    private final MyCustomUserDetailsService userDetailsService;

    public MailService(JavaMailSender sender, RedisTemplate<String, String> redis, MyCustomUserDetailsService userDetailsService) {
        this.sender = sender;
        this.redis = redis;
        this.userDetailsService = userDetailsService;
    }

    public void request(String email, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("Verify your email");
        msg.setText("Code: " + code);

        redis.opsForValue().set(code, email, Duration.ofMinutes(15));
        sender.send(msg);
    }

    public void verify(String code) {
        String email = redis.opsForValue().get(code);
        MyCustomUserDetails details = userDetailsService.getUserDetailsByEmail(email);
        if (details.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException(email + " already verified");
        }
        details.setEmailVerified(true);
        userDetailsService.save(details);
    }
}
