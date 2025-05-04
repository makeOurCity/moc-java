package city.makeour.moc;

import java.time.ZonedDateTime;

public interface RefreshTokenInterface {

    public String getRefreshToken();

    public ZonedDateTime getExpirationDate();

    public boolean isExpired();

    public boolean isExpired(ZonedDateTime now);

    public boolean hasRefreshToken();
}
