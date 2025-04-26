package city.makeour.moc;

import java.util.List;

import city.makeour.ngsi.v2.api.ApiEntryPointApi;
import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;
import city.makeour.ngsi.v2.invoker.ApiException;
import city.makeour.ngsi.v2.model.ListEntitiesResponse;
import city.makeour.ngsi.v2.model.RetrieveApiResourcesResponse;

public class MocClient {
    private String accessToken;
    private String refreshToken;
    private String fiwareService;
    private final ApiClient apiClient;
    private final ApiEntryPointApi apiEntryPoint;
    private final EntitiesApi entitiesApi;

    public MocClient(String host, String basePath) throws ApiException {
        this.apiClient = new ApiClient();
        this.apiClient.setHost(host);
        this.apiClient.setBasePath(basePath);
        this.apiEntryPoint = new ApiEntryPointApi(apiClient);
        this.entitiesApi = new EntitiesApi(apiClient);
        validateConnection();
    }

    // テスト用のコンストラクタ
    MocClient(String baseUrl, ApiClient apiClient, ApiEntryPointApi apiEntryPoint, EntitiesApi entitiesApi)
            throws ApiException {
        apiClient.setBasePath(baseUrl);
        this.apiClient = apiClient;
        this.apiEntryPoint = apiEntryPoint;
        this.entitiesApi = entitiesApi;
        validateConnection();
    }

    private void validateConnection() throws ApiException {
        RetrieveApiResourcesResponse result = apiEntryPoint.retrieveAPIResources();
        if (result == null) {
            throw new ApiException("Failed to retrieve API resources");
        }
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Fiware-Serviceヘッダの値を設定します
     *
     * @param fiwareService テナント名
     */
    public void setFiwareService(String fiwareService) {
        this.fiwareService = fiwareService;
        // TODO: ApiClientの正しいヘッダー設定方法を調査する
    }

    /**
     * 設定されているFiware-Serviceヘッダの値を取得します
     *
     * @return テナント名
     */
    public String getFiwareService() {
        return fiwareService;
    }

    /**
     * エンティティの一覧を取得します
     *
     * @return エンティティのリスト
     * @throws ApiException API呼び出しが失敗した場合
     */
    public List<ListEntitiesResponse> listEntities() throws ApiException {
        return entitiesApi.listEntities(null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    /**
     * エンティティの一覧をページ指定で取得します
     * 
     * @param offset オフセット値
     * @return エンティティのリスト
     * @throws ApiException API呼び出しが失敗した場合
     */
    public List<ListEntitiesResponse> listEntities(Double offset) throws ApiException {
        return entitiesApi.listEntities(null, null, null, null, null, null, null, null,
                null, null, offset, null, null, null, null);
    }
}
