package net.whydah.crmservice.util;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;

import java.net.URISyntaxException;

public class MailModule extends AbstractModule {
    @Override
    protected void configure() {

    }


    @Provides
    MailClient mailClient(@Named("smtp.host") String smtpHost,
                          @Named("smtp.port") String smtpPort,
                          @Named("smtp.username") String username,
                          @Named("smtp.password") String password,
                          @Named("email.verification.subject") String subject,
                          @Named("email.verification.body") String bodyTemplate) {

        return new MailClient(smtpHost, smtpPort, username, password, subject, bodyTemplate);
    }
}