package city.makeour.moc;

import city.makeour.ngsi.v2.api.EntitiesApi;
import city.makeour.ngsi.v2.invoker.ApiClient;

public class MocClient {
    protected ApiClient apiClient;

    protected EntitiesApi entitiesApi;

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
}
