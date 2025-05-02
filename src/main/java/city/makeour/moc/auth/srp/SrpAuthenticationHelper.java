package city.makeour.moc.auth.srp;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class SrpAuthenticationHelper {

    private final SmallA a;
    private final LargeA A;
    private final LargeN N;
    private final SmallG g = new SmallG();
    private final SmallK k;
    private final String userPoolId;
    private final String userPoolName;;

    public SrpAuthenticationHelper(String userPoolId) {
        this.N = new LargeN();
        this.a = new SmallA(N);
        this.A = new LargeA(N, this.a, this.g);
        this.k = new SmallK(N, g);
        this.userPoolId = userPoolId;
        this.userPoolName = userPoolId.split("_")[1];
    }

    public String getA() {
        return this.A.value().toString(16);
    }

    public String getHexA() {
        String hex = A.value().toString(16);
        return String.format("%0256x", new BigInteger(hex, 16)); // 256桁になるように0埋め
    }

    public byte[] getPasswordAuthenticationKey(
            String userIdForSrp,
            String password,
            String srpBHex,
            String saltHex,
            String secretBlock) throws InvalidKeyException, NoSuchAlgorithmException {

        LargeB B = new LargeB(srpBHex);
        if (B.value().mod(this.N.value()).equals(BigInteger.ZERO)) {
            throw new IllegalStateException("Invalid server B value");
        }
        SmallU u = new SmallU(A, B);
        BigInteger salt = new BigInteger(1, Helper.hexStringToByteArray(saltHex));
        SmallX x = new SmallX(this.userPoolName, userIdForSrp, password, salt);
        LargeS S = new LargeS(B, this.k, this.g, x, this.a, u, this.N);
        HKDF hkdf = new HKDF(S, u);

        return hkdf.value();
    }

    public String calculateSignature(String userIdForSRP, String secretBlock, String timestamp, byte[] key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));

        byte[] poolNameBytes = this.userPoolName.getBytes(StandardCharsets.UTF_8);
        byte[] userIdBytes = userIdForSRP.getBytes(StandardCharsets.UTF_8);
        byte[] secretBlockBytes = Base64.getDecoder().decode(secretBlock);
        byte[] timestampBytes = timestamp.getBytes(StandardCharsets.UTF_8);

        byte[] message = new byte[poolNameBytes.length + userIdBytes.length + secretBlockBytes.length
                + timestampBytes.length];
        int pos = 0;
        System.arraycopy(poolNameBytes, 0, message, pos, poolNameBytes.length);
        pos += poolNameBytes.length;
        System.arraycopy(userIdBytes, 0, message, pos, userIdBytes.length);
        pos += userIdBytes.length;
        System.arraycopy(secretBlockBytes, 0, message, pos, secretBlockBytes.length);
        pos += secretBlockBytes.length;
        System.arraycopy(timestampBytes, 0, message, pos, timestampBytes.length);

        String p = new String(poolNameBytes);
        String u = new String(userIdBytes);
        String s = new String(secretBlockBytes);
        String t = new String(timestampBytes);
        String hm = Helper.toHex(message);

        byte[] rawSignature = mac.doFinal(message);
        return Base64.getEncoder().encodeToString(rawSignature);
    }

    public String getCurrentFormattedTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", java.util.Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format.format(new Date());
    }
}
