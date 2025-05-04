package city.makeour.moc;

public class Token {
    private String idToken;
    private String refreshToken;
    private String accessToken;

    public Token(String idToken, String refreshToken, String accessToken) {
        this.idToken = idToken;
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }
}
