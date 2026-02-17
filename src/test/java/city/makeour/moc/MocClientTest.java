package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.model.CreateEntityRequest;
import city.makeour.ngsi.v2.model.ListEntitiesResponse;
import city.makeour.ngsi.v2.model.RetrieveEntityResponse;

class MocClientTest {

    @Test
    @DisplayName("デフォルトコンストラクタで正しいベースパスが設定されることを確認")
    void defaultConstructorShouldSetCorrectBasePath() {
        MocClient client = new MocClient();
        assertEquals("https://orion.sandbox.makeour.city", client.client.getApiClient().getBasePath());
    }

    @Test
    @DisplayName("カスタムベースパスが正しく設定されることを確認")
    void constructorWithBasePathShouldSetCustomBasePath() {
        String customBasePath = "https://custom.orion.example.com";
        MocClient client = new MocClient(customBasePath);
        assertEquals(customBasePath, client.client.getApiClient().getBasePath());
    }

    @Test
    @DisplayName("entities()メソッドが正しいEntitiesApiインスタンスを返すことを確認")
    void entitiesMethodShouldReturnEntitiesApiInstance() {
        MocClient client = new MocClient();
        EntitiesApi entitiesApi = client.entities();

        assertNotNull(entitiesApi);
        assertEquals(client.client.getEntitiesApi(), entitiesApi);
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

    @Test
    @DisplayName("トークンリフレッシュなど行えるかのテスト")
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USER_POOL_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_CLIENT_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USERNAME", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_PASSWORD", matches = ".*")
    })

    void testAuth() throws GeneralSecurityException, NoSuchAlgorithmException {
        String cognitoUserPoolId = System.getenv("TEST_COGNITO_USER_POOL_ID");
        String cognitoClientId = System.getenv("TEST_COGNITO_CLIENT_ID");
        String username = System.getenv("TEST_COGNITO_USERNAME");
        String password = System.getenv("TEST_COGNITO_PASSWORD");

        MocClient client = new MocClient();
        client.setMocAuthInfo(cognitoUserPoolId, cognitoClientId);
        client.auth(username, password);
        client.auth(username, password); // トークンリフレッシュを行う

        String uuid = UUID.randomUUID().toString();

        CreateEntityRequest entity = new CreateEntityRequest();
        entity.setType("TestEntity");
        entity.setId("urn:ngsi-ld:TestEntity:" + uuid);

        client.entities().createEntity("application/json", entity, "keyValues");
    }

    @Test
    @DisplayName("エンティティを作成・取得できるかのテスト（最小版）")
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USER_POOL_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_CLIENT_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USERNAME", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_PASSWORD", matches = ".*")
    })
    void testCreateAndGetEntity_Minimal() throws GeneralSecurityException, NoSuchAlgorithmException {
        MocClient client = new MocClient();
        client.setMocAuthInfo(System.getenv("TEST_COGNITO_USER_POOL_ID"), System.getenv("TEST_COGNITO_CLIENT_ID"));
        client.login(System.getenv("TEST_COGNITO_USERNAME"), System.getenv("TEST_COGNITO_PASSWORD"));

        // 作成&取得
        String entityId = "urn:ngsi-ld:TestEntity:" + UUID.randomUUID().toString();
        CreateEntityRequest entity = new CreateEntityRequest();
        entity.setType("TestEntity");
        entity.setId(entityId);

        // 作成を実行
        client.entities().createEntity("application/json", entity, "keyValues");

        // getEntityの呼び出し、レスポンスの変換
        RetrieveEntityResponse retrievedEntity = client
                .getEntity(entityId, "TestEntity", null, null, null)
                .body(RetrieveEntityResponse.class);

        assertNotNull(retrievedEntity);
        assertEquals(entityId, retrievedEntity.getId());
    }

    @Test
    @DisplayName("updateEntityのUpsert（作成・更新）ロジックをテストする")
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USER_POOL_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_CLIENT_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USERNAME", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_PASSWORD", matches = ".*")
    })
    void testUpdateEntity_UpsertLogic() throws GeneralSecurityException, NoSuchAlgorithmException {
        MocClient client = new MocClient();
        client.setMocAuthInfo(System.getenv("TEST_COGNITO_USER_POOL_ID"), System.getenv("TEST_COGNITO_CLIENT_ID"));
        client.login(System.getenv("TEST_COGNITO_USERNAME"), System.getenv("TEST_COGNITO_PASSWORD"));

        String entityId = "urn:ngsi-ld:TestUpsert:" + UUID.randomUUID().toString();
        String entityType = "TestUpsertType";

        // 1. "作成" (Insert) pathのテスト
        // エンティティが存在しない --> catchブロックの this.createEntity
        Map<String, Object> initialAttrs = new HashMap<>();
        initialAttrs.put("temperature", 25);
        initialAttrs.put("humidity", 50); // "temperature" 以外の属性も指定

        client.updateEntity(entityId, entityType, initialAttrs);

        // 検証 (作成)
        ParameterizedTypeReference<Map<String, Object>> mapType = new ParameterizedTypeReference<>() {
        };
        Map<String, Object> createdEntity = client.getEntity(entityId, entityType).body(mapType);

        assertNotNull(createdEntity);
        assertEquals(entityId, createdEntity.get("id"));
        assertEquals(25, createdEntity.get("temperature"));
        assertEquals(50, createdEntity.get("humidity"));

        // 2. "更新" (Update/PATCH) pathのテスト
        // エンティティが既に存在する --> tryブロックの updateExistingEntityAttributesWithResponseSpec
        Map<String, Object> updateAttrs = new HashMap<>();
        updateAttrs.put("temperature", 30); // 更新
        updateAttrs.put("seatNumber", 10); // 追加
        updateAttrs.put("status", "active");

        client.updateEntity(entityId, entityType, updateAttrs);

        // 検証 (更新)
        Map<String, Object> updatedEntity = client.getEntity(entityId, entityType).body(mapType);

        assertNotNull(updatedEntity);
        // 更新・追加されている
        assertEquals(30, updatedEntity.get("temperature"));
        assertEquals(10, updatedEntity.get("seatNumber"));
        // 最初の作成時から変更されず残っている
        assertEquals(50, updatedEntity.get("humidity"));

        assertEquals("active", updatedEntity.get("status"));
    }
}
