package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface TokenFetcherInterface {
    void setAuthParameters(String username, String password);

    Token fetchTokenWithSrpAuth() throws InvalidKeyException, NoSuchAlgorithmException;

    Token refleshToken(String refreshToken) throws InvalidKeyException, NoSuchAlgorithmException;
}
