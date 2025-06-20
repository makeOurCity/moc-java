package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.springframework.web.client.RestClient.ResponseSpec;

import city.makeour.moc.ngsiv2.Ngsiv2Client;
import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;

public class MocClient {
    protected Ngsiv2Client client;

    protected TokenFetcherInterface tokenFetcher;

    protected RefreshTokenStorageInterface refreshTokenStorage;

    public MocClient() {
        this("https://orion.sandbox.makeour.city");
    }

    public MocClient(String basePath) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(basePath);

        this.client = new Ngsiv2Client(apiClient);

        this.refreshTokenStorage = new RefreshTokenStorage();
    }

    public EntitiesApi entities() {
        return this.client.getEntitiesApi();
    }

    public void setMocAuthInfo(String cognitoUserPoolId, String cognitoClientId) {
        this.tokenFetcher = new FetchCognitoToken(cognitoUserPoolId, cognitoClientId);
    }

    public void auth(String username, String password) throws InvalidKeyException, NoSuchAlgorithmException {
        if (this.tokenFetcher == null) {
            throw new IllegalStateException("MocClient is not initialized with Cognito auth info.");
        }

        this.tokenFetcher.setAuthParameters(username, password);

        if (this.refreshTokenStorage.hasRefreshToken()) {
            RefreshTokenInterface refreshToken = this.refreshTokenStorage.getRefreshToken();
            if (!refreshToken.isExpired()) {
                this.refreshToken();
            }
            return;
        }

        this.login(username, password);
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
        this.client.getApiClient().addDefaultHeader("Authorization", token);
    }

    public void setFiwareService(String fiwareService) {
        this.client.getApiClient().addDefaultHeader("Fiware-Service", fiwareService);
    }

    public ResponseSpec createEntity(String contentType, Object body) {
        return this.client.createEntity(contentType, body);
    }
}
