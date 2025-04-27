package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import city.makeour.ngsi.v2.api.EntitiesApi;

class MocClientTest {

    @Test
    @DisplayName("デフォルトコンストラクタで正しいベースパスが設定されることを確認")
    void defaultConstructorShouldSetCorrectBasePath() {
        MocClient client = new MocClient();
        assertEquals("https://orion.sandbox.makeour.city", client.apiClient.getBasePath());
    }

    @Test
    @DisplayName("カスタムベースパスが正しく設定されることを確認")
    void constructorWithBasePathShouldSetCustomBasePath() {
        String customBasePath = "https://custom.orion.example.com";
        MocClient client = new MocClient(customBasePath);
        assertEquals(customBasePath, client.apiClient.getBasePath());
    }

    @Test
    @DisplayName("entities()メソッドが正しいEntitiesApiインスタンスを返すことを確認")
    void entitiesMethodShouldReturnEntitiesApiInstance() {
        MocClient client = new MocClient();
        EntitiesApi entitiesApi = client.entities();

        assertNotNull(entitiesApi);
        assertEquals(client.entitiesApi, entitiesApi);
    }
}