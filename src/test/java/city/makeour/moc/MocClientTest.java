package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.model.CreateEntityRequest;
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

    // TODO: テストのスキップについてきちんと動作確認をする。
    @Test
    @DisplayName("ログインしてデータ作成できるかのテスト")
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USER_POOL_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_CLIENT_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USERNAME", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_PASSWORD", matches = ".*")
    })
    void testSetMocAuthInfo() throws GeneralSecurityException, NoSuchAlgorithmException {
        String cognitoUserPoolId = System.getenv("TEST_COGNITO_USER_POOL_ID");
        String cognitoClientId = System.getenv("TEST_COGNITO_CLIENT_ID");
        String username = System.getenv("TEST_COGNITO_USERNAME");
        String password = System.getenv("TEST_COGNITO_PASSWORD");

        MocClient client = new MocClient();
        client.setMocAuthInfo(cognitoUserPoolId, cognitoClientId);
        client.login(username, password);

        String uuid = UUID.randomUUID().toString();

        CreateEntityRequest entity = new CreateEntityRequest();
        entity.setType("TestEntity");
        entity.setId("urn:ngsi-ld:TestEntity:" + uuid);

        client.entities().createEntity("application/json", entity, "keyValues");

    }
}
