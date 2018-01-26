package uk.gov.hmcts.sscs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.sscs.domain.notify.Link;

@Component
public class NotificationConfig {

    @Value("${hmcts.phone.number}")
    private String hmctsPhoneNumber;
    @Value("${track.appeal.link}")
    private String trackAppealLink;

    public String getHmctsPhoneNumber() {
        return hmctsPhoneNumber;
    }

    public Link getTrackAppealLink() {
        return new Link(trackAppealLink);
    }
}