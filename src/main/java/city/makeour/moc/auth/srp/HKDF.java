package city.makeour.moc.auth.srp;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HKDF {

    private LargeS S;
    private SmallU u;

    public HKDF(LargeS S, SmallU u) {
        this.S = S;
        this.u = u;
    }

    public byte[] value() throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] ikm = Helper.padHex(S.value());
        byte[] salt = u.value().toByteArray();

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(ikm);

        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        byte[] info = "Caldera Derived Key".getBytes(StandardCharsets.UTF_8);
        byte[] infoWithCounter = new byte[info.length + 1];
        System.arraycopy(info, 0, infoWithCounter, 0, info.length);
        infoWithCounter[info.length] = 0x01;

        byte[] okm = mac.doFinal(infoWithCounter);
        return Arrays.copyOfRange(okm, 0, 16);
    }
}
