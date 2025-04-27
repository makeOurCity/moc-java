package city.makeour.moc;

public class MocClient {
    private final String basePath;
    private final HttpClient httpClient;

    public MocClient() {
        this("https://orion.sandbox.makeour.city");
    }

    public MocClient(String basePath) {
        this.basePath = basePath;
        this.httpClient = createHttpClient();
    }

    protected HttpClient createHttpClient() {
        return new HttpClient(basePath);
    }

    public HttpClient getHttpClient() {
        return httpClient;
    }

    public static class HttpClient {
        private final String basePath;

        public HttpClient(String basePath) {
            this.basePath = basePath;
        }

        public String getBasePath() {
            return basePath;
        }
    }
}
