package city.makeour.moc;

import java.time.ZonedDateTime;

public class Token implements RefreshTokenInterface {
    private String idToken;
    private String refreshToken;
    private String accessToken;
    private ZonedDateTime expirationDate;

    public Token(String idToken, String refreshToken, String accessToken, Integer expiresIn) {
        this(idToken, refreshToken, accessToken, expiresIn, ZonedDateTime.now());
    }

    public Token(String idToken, String refreshToken, String accessToken, Integer expiresIn, ZonedDateTime now) {
        if (expiresIn <= 0) {
            throw new IllegalArgumentException("Expiration date must be greater than 0");
        }
        this.idToken = idToken;
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;

        if (expiresIn != null) {
            if (now == null) {
                now = ZonedDateTime.now();
            }
            ZonedDateTime date = now.plusSeconds(expiresIn);
            this.expirationDate = date;
        }
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

    public ZonedDateTime getExpirationDate() {
        return expirationDate;
    }

    public boolean isExpired() {
        return this.isExpired(ZonedDateTime.now());
    }

    public boolean isExpired(ZonedDateTime now) {
        return now.isAfter(expirationDate);
    }

    public boolean hasRefreshToken() {
        return refreshToken != null && !refreshToken.isEmpty();
    }
}
