package nodomain.freeyourgadget.gadgetbridge.service.devices.garmin.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class FakeOauthHandler {
    private static final Logger LOG = LoggerFactory.getLogger(FakeOauthHandler.class);

    public static GarminHttpResponse handleRequest(GarminHttpRequest request) {
        final String fakeOauth = "{\"access_token\":\"t\",\"token_type\":\"Bearer\",\"expires_in\":7776000,\"scope\":\"GCS_EPHEMERIS_SONY_READ\",\"refresh_token\":\"r\",\"refresh_token_expires_in\":\"31536000\",\"customerId\":\"c\"}";
        final GarminHttpResponse response = new GarminHttpResponse();

        LOG.info("Sending fake oauth");

        response.setStatus(200);
        response.setBody(fakeOauth.getBytes(StandardCharsets.UTF_8));

        return response;
    }
}
