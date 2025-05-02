package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface TokenFetcherInterface {
    void setAuthParameters(String username, String password);

    Token fetchToken() throws InvalidKeyException, NoSuchAlgorithmException;
}
