package city.makeour.moc;

public interface RefreshTokenStorageInterface {

    public RefreshTokenInterface getRefreshToken();

    public void setRefreshToken(RefreshTokenInterface refreshToken);

    public boolean hasRefreshToken();
}
