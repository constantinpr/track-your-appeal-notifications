package uk.gov.hmcts.sscs.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.sscs.exception.AuthorisationException;

public class AuthorisationServiceTest {

    @Mock
    private ServiceAuthorisationApi serviceAuthorisationApi;

    private AuthorisationService service;

    private static final String SERVICE_NAME = "SSCS";

    @Before
    public void setup() {
        initMocks(this);
        service = new AuthorisationService(serviceAuthorisationApi);
    }

    @Test
    public void authoriseClientRequest() {
        when(serviceAuthorisationApi.getServiceName(any())).thenReturn(SERVICE_NAME);

        assertTrue(service.authorise(SERVICE_NAME));
    }

    @Test(expected = AuthorisationException.class)
    public void shouldHandleAnAuthorisationException() {
        when(serviceAuthorisationApi.getServiceName(any())).thenThrow(new CustomFeignException(400, ""));
        service.authorise(SERVICE_NAME);
    }

    @Test(expected = FeignException.class)
    public void shouldHandleAnUnknownFeignException() {
        when(serviceAuthorisationApi.getServiceName(any())).thenThrow(new CustomFeignException(501, ""));
        service.authorise(SERVICE_NAME);
    }

    @Test(expected = FeignException.class)
    public void shouldHandleAnUnknownFeignException2() {
        when(serviceAuthorisationApi.getServiceName(any())).thenThrow(new CustomFeignException(399, ""));
        service.authorise(SERVICE_NAME);
    }

    private class CustomFeignException extends FeignException {
        public CustomFeignException(int status, String message) {
            super(status, message);
        }
    }
}