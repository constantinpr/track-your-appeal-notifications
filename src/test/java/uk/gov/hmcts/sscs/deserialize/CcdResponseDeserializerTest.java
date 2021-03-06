package uk.gov.hmcts.sscs.deserialize;

import static org.junit.Assert.*;
import static uk.gov.hmcts.sscs.domain.notify.EventType.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.sscs.domain.CcdResponse;
import uk.gov.hmcts.sscs.domain.CcdResponseWrapper;

public class CcdResponseDeserializerTest {

    private CcdResponseDeserializer ccdResponseDeserializer;
    private ObjectMapper mapper;

    @Before
    public void setup() {
        ccdResponseDeserializer = new CcdResponseDeserializer();
        mapper = new ObjectMapper();
    }

    @Test
    public void deserializeAppellantJson() throws IOException {

        String appealJson = "{\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Vasquez\",\"firstName\":\"Dexter\",\"middleName\":\"Ali Sosa\"}}}";
        String subscriptionJson = "{\"appellantSubscription\":{\"tya\":\"543212345\",\"email\":\"test@testing.com\",\"mobile\":\"01234556634\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"}}";

        CcdResponse ccdResponse = ccdResponseDeserializer.deserializeAppellantDetailsJson(mapper.readTree(appealJson), mapper.readTree(subscriptionJson), new CcdResponse());

        assertEquals("Dexter", ccdResponse.getAppellantSubscription().getFirstName());
        assertEquals("Vasquez", ccdResponse.getAppellantSubscription().getSurname());
        assertEquals("Mr", ccdResponse.getAppellantSubscription().getTitle());
        assertEquals("test@testing.com", ccdResponse.getAppellantSubscription().getEmail());
        assertEquals("01234556634", ccdResponse.getAppellantSubscription().getMobileNumber());
        assertFalse(ccdResponse.getAppellantSubscription().isSubscribeSms());
        assertTrue(ccdResponse.getAppellantSubscription().isSubscribeEmail());
    }

    @Test
    public void deserializeSupporterJson() throws IOException {

        String appealJson = "{\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Eaton\"}}}";
        String subscriptionJson = "{\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}}";

        CcdResponse ccdResponse = ccdResponseDeserializer.deserializeSupporterDetailsJson(mapper.readTree(appealJson), mapper.readTree(subscriptionJson), new CcdResponse());

        assertEquals("Amber", ccdResponse.getSupporterSubscription().getFirstName());
        assertEquals("Wilder", ccdResponse.getSupporterSubscription().getSurname());
        assertEquals("Mrs", ccdResponse.getSupporterSubscription().getTitle());
        assertEquals("supporter@live.co.uk", ccdResponse.getSupporterSubscription().getEmail());
        assertEquals("07925289702", ccdResponse.getSupporterSubscription().getMobileNumber());
        assertTrue(ccdResponse.getSupporterSubscription().isSubscribeSms());
        assertFalse(ccdResponse.getSupporterSubscription().isSubscribeEmail());
    }

    @Test
    public void deserializeEventJson() throws IOException {
        String eventJson = "{\"events\": [{\"id\": \"bad54ab0-5d09-47ab-b9fd-c3d55cbaf56f\",\"value\": {\"date\": \"2018-01-19\",\"description\": null,\"type\": \"appealReceived\"}}]}";

        CcdResponse ccdResponse = ccdResponseDeserializer.deserializeEventDetailsJson(mapper.readTree(eventJson), new CcdResponse());

        assertEquals(1, ccdResponse.getEvents().size());
        assertEquals(new DateTime(2018, 1, 19, 0, 0).toDate(), ccdResponse.getEvents().get(0).getDate());
        assertEquals(APPEAL_RECEIVED, ccdResponse.getEvents().get(0).getEventType());
    }

    @Test
    public void deserializeMultipleEventJsonInDescendingEventDateOrder() throws IOException {
        String eventJson = "{\"events\": [{\"id\": \"bad54ab0\",\"value\": {\"date\": \"2018-01-19\",\"description\": null,\"type\": \"appealReceived\"}},\n"
                + "{\"id\": \"12354ab0\",\"value\": {\"date\": \"2018-01-21\",\"description\": null,\"type\": \"appealWithdrawn\"}},\n"
                + "{\"id\": \"87564ab0\",\"value\": {\"date\": \"2018-01-20\",\"description\": null,\"type\": \"appealLapsed\"}}]}";

        CcdResponse ccdResponse = ccdResponseDeserializer.deserializeEventDetailsJson(mapper.readTree(eventJson), new CcdResponse());

        assertEquals(3, ccdResponse.getEvents().size());
        assertEquals(new DateTime(2018, 1, 21, 0, 0).toDate(), ccdResponse.getEvents().get(0).getDate());
        assertEquals(APPEAL_WITHDRAWN, ccdResponse.getEvents().get(0).getEventType());
        assertEquals(new DateTime(2018, 1, 20, 0, 0).toDate(), ccdResponse.getEvents().get(1).getDate());
        assertEquals(APPEAL_LAPSED, ccdResponse.getEvents().get(1).getEventType());
        assertEquals(new DateTime(2018, 1, 19, 0, 0).toDate(), ccdResponse.getEvents().get(2).getDate());
        assertEquals(APPEAL_RECEIVED, ccdResponse.getEvents().get(2).getEventType());
    }

    @Test(expected = IOException.class)
    public void throwsIoExceptionWhenDeserializeInvalidEventDateJson() throws IOException {
        String eventJson = "{\"events\": [{\"id\": \"bad54ab0\",\"value\": {\"date\": \"bla-01-19\",\"description\": null,\"type\": \"appealReceived\"}},\n"
                + "{\"id\": \"87564ab0\",\"value\": {\"date\": \"2018-01-20\",\"description\": null,\"type\": \"appealLapsed\"}}]}";

        ccdResponseDeserializer.deserializeEventDetailsJson(mapper.readTree(eventJson), new CcdResponse());
    }

    @Test
    public void deserializeAllCcdResponseJson() throws IOException {

        String json = "{\"case_details\":{\"case_data\":{\"subscriptions\":{"
                + "\"appellantSubscription\":{\"tya\":\"543212345\",\"email\":\"test@testing.com\",\"mobile\":\"01234556634\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"},"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"caseReference\":\"SC/1234/23\",\"appeal\":{"
                + "\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Vasquez\",\"firstName\":\"Dexter\",\"middleName\":\"Ali Sosa\"}},"
                + "\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Clark Eaton\"}}}}},\"event_id\": \"appealReceived\"\n}";

        CcdResponseWrapper wrapper = mapper.readValue(json, CcdResponseWrapper.class);
        CcdResponse ccdResponse = wrapper.getNewCcdResponse();

        assertEquals(APPEAL_RECEIVED, ccdResponse.getNotificationType());
        assertEquals("Dexter", ccdResponse.getAppellantSubscription().getFirstName());
        assertEquals("Vasquez", ccdResponse.getAppellantSubscription().getSurname());
        assertEquals("Mr", ccdResponse.getAppellantSubscription().getTitle());
        assertEquals("test@testing.com", ccdResponse.getAppellantSubscription().getEmail());
        assertEquals("01234556634", ccdResponse.getAppellantSubscription().getMobileNumber());
        assertFalse(ccdResponse.getAppellantSubscription().isSubscribeSms());
        assertTrue(ccdResponse.getAppellantSubscription().isSubscribeEmail());
        assertEquals("Amber", ccdResponse.getSupporterSubscription().getFirstName());
        assertEquals("Wilder", ccdResponse.getSupporterSubscription().getSurname());
        assertEquals("Mrs", ccdResponse.getSupporterSubscription().getTitle());
        assertEquals("supporter@live.co.uk", ccdResponse.getSupporterSubscription().getEmail());
        assertEquals("07925289702", ccdResponse.getSupporterSubscription().getMobileNumber());
        assertTrue(ccdResponse.getSupporterSubscription().isSubscribeSms());
        assertFalse(ccdResponse.getSupporterSubscription().isSubscribeEmail());
        assertEquals("SC/1234/23", ccdResponse.getCaseReference());
    }

    @Test
    public void deserializeAllCcdResponseJsonWithNewAndOldCcdData() throws IOException {

        String json = "{\"case_details\":{\"case_data\":{\"subscriptions\":{"
                + "\"appellantSubscription\":{\"tya\":\"543212345\",\"email\":\"test@testing.com\",\"mobile\":\"01234556634\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"},"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"caseReference\":\"SC/1234/23\",\"appeal\":{"
                + "\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Vasquez\",\"firstName\":\"Dexter\",\"middleName\":\"Ali Sosa\"}},"
                + "\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Clark Eaton\"}}}}},"
                + "\"case_details_before\":{\"case_data\":{\"subscriptions\":{"
                + "\"appellantSubscription\":{\"tya\":\"123456\",\"email\":\"old@email.com\",\"mobile\":\"07543534345\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"},"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@gmail.co.uk\",\"mobile\":\"07925267702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"caseReference\":\"SC/5432/89\",\"appeal\":{"
                + "\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Smith\",\"firstName\":\"Jeremy\",\"middleName\":\"Rupert\"}},"
                + "\"supporter\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Redknapp\",\"firstName\":\"Harry\",\"middleName\":\"Winston\"}}}}},"
                + "\"event_id\": \"appealReceived\"\n}";

        CcdResponseWrapper wrapper = mapper.readValue(json, CcdResponseWrapper.class);
        CcdResponse newCcdResponse = wrapper.getNewCcdResponse();

        assertEquals(APPEAL_RECEIVED, newCcdResponse.getNotificationType());
        assertEquals("Dexter", newCcdResponse.getAppellantSubscription().getFirstName());
        assertEquals("Vasquez", newCcdResponse.getAppellantSubscription().getSurname());
        assertEquals("Mr", newCcdResponse.getAppellantSubscription().getTitle());
        assertEquals("test@testing.com", newCcdResponse.getAppellantSubscription().getEmail());
        assertEquals("01234556634", newCcdResponse.getAppellantSubscription().getMobileNumber());
        assertFalse(newCcdResponse.getAppellantSubscription().isSubscribeSms());
        assertTrue(newCcdResponse.getAppellantSubscription().isSubscribeEmail());
        assertEquals("Amber", newCcdResponse.getSupporterSubscription().getFirstName());
        assertEquals("Wilder", newCcdResponse.getSupporterSubscription().getSurname());
        assertEquals("Mrs", newCcdResponse.getSupporterSubscription().getTitle());
        assertEquals("supporter@live.co.uk", newCcdResponse.getSupporterSubscription().getEmail());
        assertEquals("07925289702", newCcdResponse.getSupporterSubscription().getMobileNumber());
        assertTrue(newCcdResponse.getSupporterSubscription().isSubscribeSms());
        assertFalse(newCcdResponse.getSupporterSubscription().isSubscribeEmail());
        assertEquals("SC/1234/23", newCcdResponse.getCaseReference());

        CcdResponse oldCcdResponse = wrapper.getOldCcdResponse();

        assertEquals("Jeremy", oldCcdResponse.getAppellantSubscription().getFirstName());
        assertEquals("Smith", oldCcdResponse.getAppellantSubscription().getSurname());
        assertEquals("Mr", oldCcdResponse.getAppellantSubscription().getTitle());
        assertEquals("old@email.com", oldCcdResponse.getAppellantSubscription().getEmail());
        assertEquals("07543534345", oldCcdResponse.getAppellantSubscription().getMobileNumber());
        assertFalse(oldCcdResponse.getAppellantSubscription().isSubscribeSms());
        assertTrue(oldCcdResponse.getAppellantSubscription().isSubscribeEmail());
        assertEquals("Harry", oldCcdResponse.getSupporterSubscription().getFirstName());
        assertEquals("Redknapp", oldCcdResponse.getSupporterSubscription().getSurname());
        assertEquals("Mr", oldCcdResponse.getSupporterSubscription().getTitle());
        assertEquals("supporter@gmail.co.uk", oldCcdResponse.getSupporterSubscription().getEmail());
        assertEquals("07925267702", oldCcdResponse.getSupporterSubscription().getMobileNumber());
        assertTrue(oldCcdResponse.getSupporterSubscription().isSubscribeSms());
        assertFalse(oldCcdResponse.getSupporterSubscription().isSubscribeEmail());
        assertEquals("SC/5432/89", oldCcdResponse.getCaseReference());
    }

    @Test
    public void deserializeWithMissingAppellantName() throws IOException {
        String json = "{\"case_details\":{\"case_data\":{\"subscriptions\":{"
                + "\"appellantSubscription\":{\"tya\":\"543212345\",\"email\":\"test@testing.com\",\"mobile\":\"01234556634\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"},"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"caseReference\":\"SC/1234/23\",\"appeal\":{"
                + "\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Clark Eaton\"}}}}},\"event_id\": \"appealReceived\"\n}";

        CcdResponseWrapper wrapper = mapper.readValue(json, CcdResponseWrapper.class);

        assertNull(wrapper.getNewCcdResponse().getAppellantSubscription().getSurname());
        assertEquals("test@testing.com", wrapper.getNewCcdResponse().getAppellantSubscription().getEmail());
    }

    @Test
    public void deserializeWithMissingAppellantSubscription() throws IOException {
        String json = "{\"case_details\":{\"case_data\":{\"subscriptions\":{"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"caseReference\":\"SC/1234/23\",\"appeal\":{"
                + "\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Vasquez\",\"firstName\":\"Dexter\",\"middleName\":\"Ali Sosa\"}},"
                + "\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Clark Eaton\"}}}}},\"event_id\": \"appealReceived\"\n}";

        CcdResponseWrapper wrapper = mapper.readValue(json, CcdResponseWrapper.class);

        assertNull(wrapper.getNewCcdResponse().getAppellantSubscription().getEmail());
        assertEquals("Vasquez", wrapper.getNewCcdResponse().getAppellantSubscription().getSurname());
    }

    @Test
    public void deserializeWithMissingCaseReference() throws IOException {
        String json = "{\"case_details\":{\"case_data\":{\"subscriptions\":{"
                + "\"appellantSubscription\":{\"tya\":\"543212345\",\"email\":\"test@testing.com\",\"mobile\":\"01234556634\",\"reason\":null,\"subscribeSms\":\"No\",\"subscribeEmail\":\"Yes\"},"
                + "\"supporterSubscription\":{\"tya\":\"232929249492\",\"email\":\"supporter@live.co.uk\",\"mobile\":\"07925289702\",\"reason\":null,\"subscribeSms\":\"Yes\",\"subscribeEmail\":\"No\"}},"
                + "\"appeal\":{"
                + "\"appellant\":{\"name\":{\"title\":\"Mr\",\"lastName\":\"Vasquez\",\"firstName\":\"Dexter\",\"middleName\":\"Ali Sosa\"}},"
                + "\"supporter\":{\"name\":{\"title\":\"Mrs\",\"lastName\":\"Wilder\",\"firstName\":\"Amber\",\"middleName\":\"Clark Eaton\"}}}}},\"event_id\": \"appealReceived\"\n}";

        CcdResponseWrapper wrapper = mapper.readValue(json, CcdResponseWrapper.class);

        assertNull(wrapper.getNewCcdResponse().getCaseReference());
    }

    @Test
    public void returnNodeWhenNodeIsPresent() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode node = factory.objectNode();
        final ObjectNode child = factory.objectNode();

        node.put("message", "test");
        child.set("child", node);
        assertEquals(node, ccdResponseDeserializer.getNode(child, "child"));
    }

    @Test
    public void returnNullWhenNodeIsNotPresent() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode child = factory.objectNode();

        child.put("message", "test");
        assertEquals(null, ccdResponseDeserializer.getNode(child, "somethingelse"));
    }

    @Test
    public void returnNullWhenNodeIsNull() {
        assertEquals(null, ccdResponseDeserializer.getNode(null, "somethingelse"));
    }

    @Test
    public void returnTextWhenFieldIsPresent() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode node = factory.objectNode();

        node.put("message", "test");
        assertEquals("test", ccdResponseDeserializer.getField(node, "message"));
    }

    @Test
    public void returnNullWhenFieldIsNotPresent() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;
        final ObjectNode child = factory.objectNode();

        child.put("message", "test");
        assertEquals(null, ccdResponseDeserializer.getField(child, "somethingelse"));
    }

    @Test
    public void returnNullWhenFieldIsNull() {
        assertEquals(null, ccdResponseDeserializer.getField(null, "somethingelse"));
    }

    @Test
    public void returnTrueForYes() {
        assertTrue(ccdResponseDeserializer.convertYesNoToBoolean("Yes"));
    }

    @Test
    public void returnFalseForNo() {
        assertFalse(ccdResponseDeserializer.convertYesNoToBoolean("No"));
    }

    @Test
    public void returnFalseForNull() {
        assertFalse(ccdResponseDeserializer.convertYesNoToBoolean(null));
    }
}