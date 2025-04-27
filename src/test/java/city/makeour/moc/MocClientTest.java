package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import city.makeour.moc.MocClient.HttpClient;

@ExtendWith(MockitoExtension.class)
class MocClientTest {

    @Test
    void defaultConstructorShouldSetDefaultBasePath() {
        MocClient client = new MocClient();
        assertEquals("https://orion.sandbox.makeour.city", client.getHttpClient().getBasePath());
    }

    @Test
    void constructorWithBasePathShouldSetSpecifiedPath() {
        String customPath = "https://custom.example.com";
        MocClient client = new MocClient(customPath);
        assertEquals(customPath, client.getHttpClient().getBasePath());
    }

    @Test
    void createHttpClientShouldCreateNewInstance() {
        MocClient client = new MocClient();
        HttpClient httpClient = client.getHttpClient();
        assertNotNull(httpClient);
    }
}