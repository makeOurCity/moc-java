package city.makeour.moc;

public class RefreshTokenStorage implements RefreshTokenStorageInterface {

    private String refreshToken;

    public RefreshTokenStorage() {
        this.refreshToken = null;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean hasRefreshToken() {
        return this.refreshToken != null;
    }
}
