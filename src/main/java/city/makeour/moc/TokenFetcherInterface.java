package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface TokenFetcherInterface {
    void setAuthParameters(String username, String password);

    String fetchToken() throws InvalidKeyException, NoSuchAlgorithmException;
}
