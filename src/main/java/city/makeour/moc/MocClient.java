package city.makeour.moc;

import city.makeour.ngsi.v2.api.ApiEntryPointApi;
import city.makeour.ngsi.v2.invoker.ApiException;
import city.makeour.ngsi.v2.invoker.Configuration;
import city.makeour.ngsi.v2.model.RetrieveApiResourcesResponse;

public class MocClient {
    private final String baseUrl;
    private String accessToken;
    private String refreshToken;
    private final ApiEntryPointApi apiEntryPoint;

    public MocClient(String baseUrl) throws ApiException {
        this(baseUrl, new ApiEntryPointApi(Configuration.getDefaultApiClient()));
    }

    // テスト用のコンストラクタ
    MocClient(String baseUrl, ApiEntryPointApi apiEntryPoint) throws ApiException {
        this.baseUrl = baseUrl;
        this.apiEntryPoint = apiEntryPoint;
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

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
