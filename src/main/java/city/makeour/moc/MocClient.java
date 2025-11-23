package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec;

import city.makeour.moc.ngsiv2.Ngsiv2Client;
import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;
import city.makeour.ngsi.v2.model.UpdateExistingEntityAttributesRequest;

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

    /**
     * Retrieves an entity with the specified parameters.
     *
     * @param entityId The ID of the entity to retrieve.
     * @param type The type of the entity.
     * @param attrs Comma-separated list of attribute names to include in the response. If null, all attributes are returned.
     * @param metadata Comma-separated list of metadata names to include. If null, all metadata is returned.
     * @param options Options to modify the response format (e.g., "keyValues" for simplified representation).
     * @return The response specification for the entity retrieval request.
     * 
     * <p>
     * Use this overload to fully customize the entity retrieval, including which attributes,
     * metadata, and options are used. The {@code options} parameter allows you to control
     * the response format; for example, "keyValues" returns a simplified JSON object.
     * </p>
     */
    public ResponseSpec getEntity(String entityId, String type, String attrs, String metadata, String options) {
        return this.entities().retrieveEntityWithResponseSpec(entityId, type, attrs, metadata, "keyValues");
    }

    /**
     * Retrieves an entity with the specified ID, type, and attributes.
     *
     * @param entityId The ID of the entity to retrieve.
     * @param type The type of the entity.
     * @param attrs Comma-separated list of attribute names to include in the response. If null, all attributes are returned.
     * @return The response specification for the entity retrieval request.
     *
     * <p>
     * This overload defaults {@code metadata} to {@code null} (all metadata) and {@code options} to {@code "keyValues"}
     * for a simplified response format.
     * </p>
     */
    public ResponseSpec getEntity(String entityId, String type, String attrs) {
        return this.entities().retrieveEntityWithResponseSpec(entityId, type, attrs, null, "keyValues");
    }

    /**
     * Retrieves an entity with the specified ID and type.
     *
     * @param entityId The ID of the entity to retrieve.
     * @param type The type of the entity.
     * @return The response specification for the entity retrieval request.
     *
     * <p>
     * This overload defaults {@code attrs} and {@code metadata} to {@code null} (all attributes and metadata),
     * and {@code options} to {@code "keyValues"} for a simplified response format.
     * </p>
     */
    public ResponseSpec getEntity(String entityId, String type) {
        return this.entities().retrieveEntityWithResponseSpec(entityId, type, null, null, "keyValues");
    }

    public ResponseSpec updateEntity(String id, String type, Map<String, Object> attributesToUpdate) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id is required");
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type is required");
        if (attributesToUpdate == null) attributesToUpdate = java.util.Collections.emptyMap();
    
        try {
            // Existence check
            this.entities()
                .retrieveEntityWithResponseSpec(id, type, null, null, "keyValues")
                .toEntity(Object.class);

            // Exists -> POST (keyValues 形式でそのまま送る)
            return this.client.updateEntityAttributes(
                id,
                "application/json",
                attributesToUpdate, // Object(Map) をそのまま PATCH
                type,
                "keyValues"
            );

        } catch (org.springframework.web.client.RestClientResponseException e) {
            if (e.getStatusCode().value() != 404) throw e;

            // Not found -> create (従来通り)
            java.util.Map<String, Object> body = new java.util.HashMap<>();
            body.put("id", id);
            body.put("type", type);
            body.putAll(attributesToUpdate);
            return this.createEntity("application/json", body);
        }   
    }

}
