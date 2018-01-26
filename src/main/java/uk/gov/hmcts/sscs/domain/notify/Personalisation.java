package uk.gov.hmcts.sscs.domain.notify;

import static uk.gov.hmcts.sscs.config.AppConstants.*;

import java.util.HashMap;
import java.util.Map;
import uk.gov.hmcts.sscs.config.NotificationConfig;
import uk.gov.hmcts.sscs.domain.CcdResponse;

public abstract class Personalisation {

    protected NotificationConfig config;

    public Personalisation(NotificationConfig config) {
        this.config = config;
    }

    public Map<String, String> create(CcdResponse ccdResponse) {

        Map<String, String> personalisation = new HashMap<>();
        personalisation.put(BENEFIT_NAME_ACRONYM_LITERAL, BENEFIT_NAME_ACRONYM);
        personalisation.put(BENEFIT_FULL_NAME_LITERAL, BENEFIT_FULL_NAME);
        personalisation.put(APPEAL_REF, ccdResponse.getCaseReference());
        personalisation.put(APPEAL_ID, ccdResponse.getAppealNumber());
        personalisation.put(APPELLANT_NAME, String.format("%s %s", ccdResponse.getAppellantFirstName(), ccdResponse.getAppellantSurname()));
        personalisation.put(PHONE_NUMBER, config.getHmctsPhoneNumber());
        personalisation.put(TRACK_APPEAL_LINK_LITERAL, config.getTrackAppealLink() != null ? config.getTrackAppealLink().replace(APPEAL_ID_LITERAL, ccdResponse.getAppealNumber()) : null);

        personalisation = customise(ccdResponse, personalisation);
        return personalisation;
    }

    protected abstract Map<String, String> customise(CcdResponse event, Map<String, String> personalisation);

    public abstract Template getTemplate();
}