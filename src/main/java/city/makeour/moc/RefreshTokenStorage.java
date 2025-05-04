package city.makeour.moc;

public class RefreshTokenStorage implements RefreshTokenStorageInterface {

    private RefreshTokenInterface refreshToken;

    public RefreshTokenStorage() {
        this.refreshToken = null;
    }

    public RefreshTokenInterface getRefreshToken() {
        return this.refreshToken;
    }

    public void setRefreshToken(RefreshTokenInterface refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean hasRefreshToken() {
        return this.refreshToken != null;
    }
}
