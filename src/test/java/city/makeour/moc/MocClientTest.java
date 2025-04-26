package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import city.makeour.ngsi.v2.api.ApiEntryPointApi;
import city.makeour.ngsi.v2.invoker.ApiException;
import city.makeour.ngsi.v2.model.RetrieveApiResourcesResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("MocClient Tests")
class MocClientTest {
    private static final String BASE_URL = "http://example.com";

    @Mock
    private ApiEntryPointApi mockApiEntryPoint;

    @Mock
    private RetrieveApiResourcesResponse mockResponse;

    private MocClient client;

    @BeforeEach
    void setUp() throws ApiException {
        when(mockApiEntryPoint.retrieveAPIResources()).thenReturn(mockResponse);
        client = new MocClient(BASE_URL, mockApiEntryPoint);
    }

    @Nested
    @DisplayName("基本的な初期化テスト")
    class InitializationTests {
        @Test
        @DisplayName("baseURLが正しく設定されていること")
        void testGetBaseUrl() {
            assertEquals(BASE_URL, client.getBaseUrl(), "baseURLが正しく設定されていない");
        }

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
                    () -> new MocClient(BASE_URL, mockApiEntryPoint),
                    "APIリソース取得失敗時にApiExceptionがスローされるべき");

            assertEquals("Failed to retrieve API resources", exception.getMessage());
        }

        @Test
        @DisplayName("API呼び出しエラー時に例外が発生すること")
        void testApiCallError() throws ApiException {
            ApiException expectedException = new ApiException("API call failed");
            when(mockApiEntryPoint.retrieveAPIResources()).thenThrow(expectedException);

            ApiException exception = assertThrows(ApiException.class,
                    () -> new MocClient(BASE_URL, mockApiEntryPoint),
                    "API呼び出しエラー時にApiExceptionがスローされるべき");

            assertEquals(expectedException, exception);
        }
    }
}