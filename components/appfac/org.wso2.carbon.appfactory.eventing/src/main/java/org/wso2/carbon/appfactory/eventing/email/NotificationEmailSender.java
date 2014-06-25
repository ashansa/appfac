package org.wso2.carbon.appfactory.eventing.email;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.appfactory.common.AppFactoryException;
import org.wso2.carbon.appfactory.eventing.AppFactoryEventException;
import org.wso2.carbon.appfactory.eventing.Event;
import org.wso2.carbon.appfactory.eventing.EventDispatcher;
import org.wso2.carbon.appfactory.utilities.services.EmailSenderService;
import org.wso2.carbon.email.verification.util.EmailVerifierConfig;

public class NotificationEmailSender implements EventDispatcher {
    Log log = LogFactory.getLog(NotificationEmailSender.class);

    @Override
    public void dispatchEvent(Event event) throws AppFactoryEventException {

        EmailSenderService emailSenderService = new EmailSenderService();
        try {
            String mailSubject = event.getMessageTitle();
            String mailBody = event.getMessageBody();
            String userName = event.getSender();
            String emailTemplate = "notification-email-config.xml";
            emailSenderService.sendMail(mailSubject, mailBody, emailTemplate, userName);
        } catch (AppFactoryException e) {
            log.error("Failed to send the email due to " + e.getMessage(), e);
            throw new AppFactoryEventException("Failed to send the email due to " + e.getMessage(), e);
        }
    }
}
