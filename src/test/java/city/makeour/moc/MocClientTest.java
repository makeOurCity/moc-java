package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import city.makeour.ngsi.v2.api.ApiEntryPointApi;
import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;
import city.makeour.ngsi.v2.invoker.ApiException;
import city.makeour.ngsi.v2.model.ListEntitiesResponse;
import city.makeour.ngsi.v2.model.RetrieveApiResourcesResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("MocClient Tests")
class MocClientTest {
    private static final String BASE_URL = "http://example.com";

    @Mock
    private ApiClient mockApiClient;

    @Mock
    private ApiEntryPointApi mockApiEntryPoint;

    @Mock
    private EntitiesApi mockEntitiesApi;

    @Mock
    private RetrieveApiResourcesResponse mockResponse;

    private MocClient client;

    @BeforeEach
    void setUp() throws ApiException {
        when(mockApiEntryPoint.retrieveAPIResources()).thenReturn(mockResponse);
        client = new MocClient(BASE_URL, mockApiClient, mockApiEntryPoint, mockEntitiesApi);
    }

    @Nested
    @DisplayName("基本的な初期化テスト")
    class InitializationTests {
        @Test
        @DisplayName("初期状態ではトークンがnullであること")
        void testInitialTokensAreNull() {
            assertNull(client.getAccessToken(), "初期状態でアクセストークンはnullのはず");
            assertNull(client.getRefreshToken(), "初期状態でリフレッシュトークンはnullのはず");
        }

        @Test
        @DisplayName("APIリソース取得の検証が行われること")
        void testApiResourceValidation() throws ApiException {
            verify(mockApiEntryPoint, times(1)).retrieveAPIResources();
        }
    }

    @Nested
    @DisplayName("トークン操作テスト")
    class TokenOperationTests {
        @Test
        @DisplayName("アクセストークンの設定と取得")
        void testSetAndGetAccessToken() {
            String accessToken = "test-access-token";
            client.setAccessToken(accessToken);
            assertEquals(accessToken, client.getAccessToken(), "設定したアクセストークンと取得したトークンが一致しない");
        }

        @Test
        @DisplayName("リフレッシュトークンの設定と取得")
        void testSetAndGetRefreshToken() {
            String refreshToken = "test-refresh-token";
            client.setRefreshToken(refreshToken);
            assertEquals(refreshToken, client.getRefreshToken(), "設定したリフレッシュトークンと取得したトークンが一致しない");
        }
    }

    @Nested
    @DisplayName("エラーケーステスト")
    class ErrorTests {
        @Test
        @DisplayName("APIリソース取得失敗時に例外が発生すること")
        void testApiResourceFailure() throws ApiException {
            when(mockApiEntryPoint.retrieveAPIResources()).thenReturn(null);

            ApiException exception = assertThrows(ApiException.class,
                    () -> new MocClient(BASE_URL, mockApiClient, mockApiEntryPoint, mockEntitiesApi),
                    "APIリソース取得失敗時にApiExceptionがスローされるべき");

            assertEquals("Failed to retrieve API resources", exception.getMessage());
        }

        @Test
        @DisplayName("API呼び出しエラー時に例外が発生すること")
        void testApiCallError() throws ApiException {
            ApiException expectedException = new ApiException("API call failed");
            when(mockApiEntryPoint.retrieveAPIResources()).thenThrow(expectedException);

            ApiException exception = assertThrows(ApiException.class,
                    () -> new MocClient(BASE_URL, mockApiClient, mockApiEntryPoint, mockEntitiesApi),
                    "API呼び出しエラー時にApiExceptionがスローされるべき");

            assertEquals(expectedException, exception);
        }
    }

    @Nested
    @DisplayName("エンティティ操作テスト")
    class EntityOperationTests {
        @Test
        @DisplayName("エンティティ一覧の取得")
        void testListEntities() throws ApiException {
            // テストデータの準備
            ListEntitiesResponse entity1 = new ListEntitiesResponse().id("entity1").type("testType");
            ListEntitiesResponse entity2 = new ListEntitiesResponse().id("entity2").type("testType");
            List<ListEntitiesResponse> expectedEntities = Arrays.asList(entity1, entity2);

            // モックの設定
            when(mockEntitiesApi.listEntities(null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null))
                    .thenReturn(expectedEntities);

            // テスト実行
            List<ListEntitiesResponse> actualEntities = client.listEntities();

            // 検証
            assertEquals(expectedEntities, actualEntities, "取得したエンティティリストが期待値と一致しない");
            verify(mockEntitiesApi, times(1))
                    .listEntities(null, null, null, null, null, null, null, null,
                            null, null, null, null, null, null, null);
        }

        @Test
        @DisplayName("エンティティ一覧取得時のエラー処理")
        void testListEntitiesError() throws ApiException {
            // モックの設定
            ApiException expectedException = new ApiException("Failed to list entities");
            when(mockEntitiesApi.listEntities(null, null, null, null, null, null, null, null,
                    null, null, null, null, null, null, null))
                    .thenThrow(expectedException);

            // テスト実行と検証
            ApiException exception = assertThrows(ApiException.class,
                    () -> client.listEntities(),
                    "エンティティ一覧取得失敗時にApiExceptionがスローされるべき");

            assertEquals(expectedException, exception);
        }
    }
}