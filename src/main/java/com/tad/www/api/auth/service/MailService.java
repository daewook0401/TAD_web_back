package com.tad.www.api.auth.service;

import com.tad.www.api.auth.dto.mail.MailDTO;

public interface MailService {
    String sendVerificationEmail(MailDTO mailDTO);
    String sendTemporaryPasswordEmail(String toEmail);
    String sendWelcomeEmail(String toEmail, String userName);
}
