package city.makeour;

public interface TokenFetcherInterface {
    void setAuthParameters(String username, String password);

    String fetchToken();
}
