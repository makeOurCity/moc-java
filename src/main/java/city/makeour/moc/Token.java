package city.makeour.moc;

public class Token {
    private String idToken;
    private String refreshToken;

    public Token(String idToken, String refreshToken) {
        this.idToken = idToken;
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
