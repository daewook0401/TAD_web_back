package com.tad.www.api.auth.service.impl;
import com.tad.www.api.auth.dto.mail.MailDTO;
import com.tad.www.api.auth.service.MailService;

public class MailServiceImpl implements MailService {

    @Override
    public String sendVerificationEmail(MailDTO mailDTO) {
        throw new UnsupportedOperationException("Unimplemented method 'sendVerificationEmail'");
    }

    @Override
    public String sendTemporaryPasswordEmail(String toEmail) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendTemporaryPasswordEmail'");
    }

    @Override
    public String sendWelcomeEmail(String toEmail, String userName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendWelcomeEmail'");
    }

}
