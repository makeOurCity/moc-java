package city.makeour.moc;

public interface TokenFetcherInterface {
    void setAuthParameters(String username, String password);

    String fetchToken();
}
