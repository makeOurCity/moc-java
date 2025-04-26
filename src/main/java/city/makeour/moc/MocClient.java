package city.makeour.moc;

import city.makeour.ngsi.v2.api.ApiEntryPointApi;
import city.makeour.ngsi.v2.invoker.ApiClient;
import city.makeour.ngsi.v2.invoker.ApiException;
import city.makeour.ngsi.v2.invoker.Configuration;
import city.makeour.ngsi.v2.model.RetrieveApiResourcesResponse;

public class MocClient {
    private String baseUrl;
    private String accessToken;
    private String refreshToken;

    public MocClient(String baseUrl) throws ApiException {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        // Configure clients using the `defaultClient` object, such as
        // overriding the host and port, timeout, etc.
        ApiEntryPointApi apiInstance = new ApiEntryPointApi(defaultClient);

        RetrieveApiResourcesResponse result = apiInstance.retrieveAPIResources();
        System.out.println(result);
        this.baseUrl = baseUrl;
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
