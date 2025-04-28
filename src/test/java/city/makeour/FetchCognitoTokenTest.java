package city.makeour;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

class FetchCognitoTokenTest {

    private static final String TEST_USER_POOL_ID = "ap-northeast-1_testpool";
    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpassword";
    private static final String TEST_ID_TOKEN = "test-id-token";
    private static final String TEST_REFRESH_TOKEN = "test-refresh-token";

    @Mock
    private CognitoIdentityProviderClient mockCognitoClient;

    private FetchCognitoToken tokenFetcher;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        tokenFetcher = new FetchCognitoToken(TEST_USER_POOL_ID, TEST_CLIENT_ID);
        tokenFetcher.setAuthParameters(TEST_USERNAME, TEST_PASSWORD);

        // モックしたクライアントを注入
        Field clientField = FetchCognitoToken.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(tokenFetcher, mockCognitoClient);
    }

    @Test
    @DisplayName("コンストラクタで正しく初期化されることを確認")
    void constructorShouldInitializeCorrectly() {
        FetchCognitoToken fetcher = new FetchCognitoToken(TEST_USER_POOL_ID, TEST_CLIENT_ID);

        assertEquals(TEST_USER_POOL_ID, fetcher.cognitoUserPoolId);
        assertEquals(TEST_CLIENT_ID, fetcher.cognitoClientId);
        assertNotNull(fetcher.client);
    }

    @Test
    @DisplayName("認証パラメータが正しく設定されることを確認")
    void setAuthParametersShouldSetCredentials() {
        FetchCognitoToken fetcher = new FetchCognitoToken(TEST_USER_POOL_ID, TEST_CLIENT_ID);
        fetcher.setAuthParameters(TEST_USERNAME, TEST_PASSWORD);

        assertEquals(TEST_USERNAME, fetcher.username);
        assertEquals(TEST_PASSWORD, fetcher.password);
    }

    @Test
    @DisplayName("リフレッシュトークンを使用してトークンを取得できることを確認")
    void fetchTokenWithRefreshTokenShouldSucceed() {
        // リフレッシュトークンを設定
        tokenFetcher.refreshToken = TEST_REFRESH_TOKEN;

        // モックの応答を設定
        AuthenticationResultType authResult = AuthenticationResultType.builder()
                .idToken(TEST_ID_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .build();

        InitiateAuthResponse mockResponse = InitiateAuthResponse.builder()
                .authenticationResult(authResult)
                .build();

        when(mockCognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                .thenReturn(mockResponse);

        // トークン取得を実行
        String token = tokenFetcher.fetchToken();

        assertEquals(TEST_ID_TOKEN, token);
        assertEquals(TEST_REFRESH_TOKEN, tokenFetcher.refreshToken);
    }

    @Test
    @DisplayName("SRP認証でトークンを取得できることを確認")
    void fetchTokenWithSRPShouldSucceed() {
        // InitiateAuth のモック応答を設定
        InitiateAuthResponse initAuthResponse = InitiateAuthResponse.builder()
                .challengeName("PASSWORD_VERIFIER")
                .challengeParameters(new java.util.HashMap<String, String>() {
                    {
                        put("USER_ID_FOR_SRP", TEST_USERNAME);
                        put("SRP_B", "123456789abcdef");
                        put("SALT", "salt123");
                        put("SECRET_BLOCK", "c2VjcmV0"); // Base64エンコードされた "secret"
                    }
                })
                .build();

        when(mockCognitoClient.initiateAuth(any(InitiateAuthRequest.class)))
                .thenReturn(initAuthResponse);

        // RespondToAuthChallenge のモック応答を設定
        AuthenticationResultType authResult = AuthenticationResultType.builder()
                .idToken(TEST_ID_TOKEN)
                .refreshToken(TEST_REFRESH_TOKEN)
                .build();

        RespondToAuthChallengeResponse challengeResponse = RespondToAuthChallengeResponse.builder()
                .authenticationResult(authResult)
                .build();

        when(mockCognitoClient.respondToAuthChallenge(any(RespondToAuthChallengeRequest.class)))
                .thenReturn(challengeResponse);

        // トークン取得を実行
        String token = tokenFetcher.fetchToken();

        assertEquals(TEST_ID_TOKEN, token);
        assertEquals(TEST_REFRESH_TOKEN, tokenFetcher.refreshToken);
    }

    @Test
    @DisplayName("SRP認証でトークン取得のテスト")
    void testFetchTokenWithSRP() {
        FetchCognitoToken fetcher = new FetchCognitoToken(
                "ap-northeast-1_nXSBLO7v6",
                "3d7d0piq75halieshbi7o8keca");

        fetcher.setAuthParameters("ushio.s@gmail.com", "");
        fetcher.fetchToken();
    }
}