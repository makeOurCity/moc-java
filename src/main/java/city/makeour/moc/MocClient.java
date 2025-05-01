package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;

public class MocClient {
    protected ApiClient apiClient;

    protected EntitiesApi entitiesApi;

    protected TokenFetcherInterface tokenFetcher;

    public MocClient() {
        this("https://orion.sandbox.makeour.city");
    }

    public MocClient(String basePath) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(basePath);

        this.entitiesApi = new EntitiesApi(this.apiClient);
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
        this.setToken(this.tokenFetcher.fetchToken());
    }

    public void setToken(String token) {
        this.apiClient.addDefaultHeader("Authorization", token);
    }
}
