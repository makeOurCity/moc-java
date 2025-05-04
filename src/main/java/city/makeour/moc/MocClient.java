package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;

public class MocClient {
    protected ApiClient apiClient;

    protected EntitiesApi entitiesApi;

    protected TokenFetcherInterface tokenFetcher;

    protected RefreshTokenStorageInterface refreshTokenStorage;

    public MocClient() {
        this("https://orion.sandbox.makeour.city");
    }

    public MocClient(String basePath) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(basePath);

        this.entitiesApi = new EntitiesApi(this.apiClient);
        this.refreshTokenStorage = new RefreshTokenStorage();
    }

    public EntitiesApi entities() {
        return this.entitiesApi;
    }

    public void setMocAuthInfo(String cognitoUserPoolId, String cognitoClientId) {
        this.tokenFetcher = new FetchCognitoToken(cognitoUserPoolId, cognitoClientId);
    }

    public void login(String username, String password) throws InvalidKeyException, NoSuchAlgorithmException {
        if (this.tokenFetcher == null) {
            throw new IllegalStateException("MocClient is not initialized with Cognito auth info.");
        }

        this.tokenFetcher.setAuthParameters(username, password);
        Token token = this.tokenFetcher.fetchTokenWithSrpAuth();

        this.setToken(token.getIdToken());
        this.refreshTokenStorage.setRefreshToken(token);
    }

    public void refreshToken() throws InvalidKeyException, NoSuchAlgorithmException {
        if (this.tokenFetcher == null) {
            throw new IllegalStateException("MocClient is not initialized with Cognito auth info.");
        }

        if (!this.refreshTokenStorage.hasRefreshToken()) {
            throw new IllegalStateException("No refresh token available.");
        }

        RefreshTokenInterface refreshToken = this.refreshTokenStorage.getRefreshToken();
        Token token = this.tokenFetcher.refleshToken(refreshToken.getRefreshToken());
        this.setToken(token.getIdToken());
    }

    public void setToken(String token) {
        this.apiClient.addDefaultHeader("Authorization", token);
    }
}
