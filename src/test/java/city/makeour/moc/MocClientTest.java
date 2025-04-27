package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.model.ListEntitiesResponse;

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

    @Test
    @DisplayName("entities apiで、entityの一覧を取得するテスト")
    void testEntitiesApi() {
        MocClient client = new MocClient();
        EntitiesApi entitiesApi = client.entities();

        List<ListEntitiesResponse> list = entitiesApi.listEntities(null, null, null, null, null, null, null, null, null,
                null, null, null,
                null,
                null,
                null);
        assertNotNull(list);
        System.out.println("Entities: " + list);
    }
}