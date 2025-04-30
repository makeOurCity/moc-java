package city.makeour.moc;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class AuthenticationHelper {

    private static final String HEX_N = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD1" +
            "29024E088A67CC74020BBEA63B139B22514A08798E3404DD" +
            "EF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245" +
            "E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED" +
            "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3D" +
            "C2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F" +
            "83655D23DCA3AD961C62F356208552BB9ED529077096966D" +
            "670C354E4ABC9804F1746C08CA237327FFFFFFFFFFFFFFFF";

    private static final BigInteger N = new BigInteger(HEX_N, 16);
    private static final BigInteger g = BigInteger.valueOf(2);
    private static final BigInteger k;

    static {
        try {
            // MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // digest.update(padHex(N));
            // digest.update(padHex(g));
            k = calculateK(N, g);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final SecureRandom random;
    private final BigInteger a;
    private final BigInteger A;
    private final String poolName;

    public AuthenticationHelper(String userPoolId) {
        random = new SecureRandom();
        a = new BigInteger(1, generateRandomBytes(128)).mod(N);
        A = g.modPow(a, N);
        poolName = userPoolId.split("_", 2)[1];
    }

    public BigInteger getA() {
        return A;
    }

    public String getHexA() {

        String hex = A.toString(16);
        return String.format("%0256x", new BigInteger(hex, 16)); // 256桁になるように0埋め
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new java.security.SecureRandom().nextBytes(bytes);
        return bytes;
    }

    public byte[] getPasswordAuthenticationKey(
            String poolName, String username, String password, String srpBHex, String saltHex, String secretBlock) {

        BigInteger B = new BigInteger(1, hexStringToByteArray(srpBHex));
        if (B.mod(N).equals(BigInteger.ZERO)) {
            throw new IllegalStateException("Invalid server B value");
        }

        BigInteger u = computeU(A, B);
        BigInteger salt = new BigInteger(1, hexStringToByteArray(saltHex));
        BigInteger x = calculateX(poolName, username, password, salt);

        // BigInteger base = B.subtract(k.multiply(g.modPow(x, N))).mod(N);
        // BigInteger exp = a.add(u.multiply(x));
        BigInteger S = calculateS(B, k, g, x, a, u, N);

        return computeHKDF(padHex(S), u.toByteArray());
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public String calculateSignature(String userIdForSRP, String secretBlock, String timestamp, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            byte[] poolNameBytes = poolName.getBytes(StandardCharsets.UTF_8);
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

            byte[] rawSignature = mac.doFinal(message);

            return Base64.getEncoder().encodeToString(rawSignature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static BigInteger calculateS(BigInteger B, BigInteger k, BigInteger g, BigInteger x, BigInteger a,
            BigInteger u, BigInteger N) {
        // Step1: base = (B - k * g^x) mod N
        BigInteger gModPowX = g.modPow(x, N);
        BigInteger kgx = k.multiply(gModPowX).mod(N);
        BigInteger base = B.subtract(kgx).mod(N);

        // Step2: exponent = (a + u * x)
        BigInteger exp = a.add(u.multiply(x));

        // Step3: S = base ^ exp mod N
        BigInteger S = base.modPow(exp, N);

        return S;
    }

    public String getCurrentFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss 'UTC' yyyy", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // --- 内部ヘルパーメソッド ---

    private static byte[] padHex(BigInteger n) {
        byte[] hex = n.toByteArray();
        if (hex.length == 256) {
            return hex;
        }
        byte[] padded = new byte[256];
        if (hex.length > 256) {
            // Remove leading 0 byte (sign byte)
            System.arraycopy(hex, hex.length - 256, padded, 0, 256);
        } else {
            System.arraycopy(hex, 0, padded, 256 - hex.length, hex.length);
        }
        return padded;
    }

    private static BigInteger computeU(BigInteger A, BigInteger B) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] aPadded = padHex(A);
            byte[] bPadded = padHex(B);

            byte[] combined = new byte[aPadded.length + bPadded.length];
            System.arraycopy(aPadded, 0, combined, 0, aPadded.length);
            System.arraycopy(bPadded, 0, combined, aPadded.length, bPadded.length);

            byte[] uHash = digest.digest(combined);

            return new BigInteger(1, uHash); // 必ず符号なし
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BigInteger calculateX(String poolName, String username, String password, BigInteger salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Step1: poolName + username + ":" + password を SHA-256
            String userPass = poolName + username + ":" + password;
            byte[] userPassHash = digest.digest(userPass.getBytes(StandardCharsets.UTF_8));

            // Step2: saltバイト列をpadして + userPassHash を SHA-256
            digest.reset();
            byte[] saltPadded = padHex(salt);

            byte[] combined = new byte[saltPadded.length + userPassHash.length];
            System.arraycopy(saltPadded, 0, combined, 0, saltPadded.length);
            System.arraycopy(userPassHash, 0, combined, saltPadded.length, userPassHash.length);

            byte[] xHash = digest.digest(combined);

            return new BigInteger(1, xHash); // 必ず符号なし
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static BigInteger calculateK(BigInteger N, BigInteger g) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] nPadded = padHex(N); // さっき作ったやつ
            byte[] gPadded = padHex(g);

            // 連結
            byte[] ng = new byte[nPadded.length + gPadded.length];
            System.arraycopy(nPadded, 0, ng, 0, nPadded.length);
            System.arraycopy(gPadded, 0, ng, nPadded.length, gPadded.length);

            byte[] hash = digest.digest(ng);

            return new BigInteger(1, hash); // 必ず符号なし！
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] computeHKDF(byte[] ikm, byte[] salt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            // Step1: PRK = HMAC(salt, ikm)
            mac.init(new SecretKeySpec(salt, "HmacSHA256"));
            byte[] prk = mac.doFinal(ikm);

            // Step2: OKM = HMAC(prk, info)
            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            byte[] info = "Caldera Derived Key".getBytes(StandardCharsets.UTF_8);
            byte[] okm = mac.doFinal(info);

            // Step3: 最初の16バイトだけ取り出す
            byte[] result = new byte[16];
            System.arraycopy(okm, 0, result, 0, 16);

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] concatBytes(byte[]... arrays) {
        int totalLength = 0;
        for (byte[] array : arrays) {
            totalLength += array.length;
        }

        byte[] result = new byte[totalLength];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }
}
