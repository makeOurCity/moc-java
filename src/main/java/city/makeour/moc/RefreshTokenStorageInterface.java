package city.makeour.moc;

public interface RefreshTokenStorageInterface {

    public String getRefreshToken();

    public void setRefreshToken(String refreshToken);

    public boolean hasRefreshToken();
}
