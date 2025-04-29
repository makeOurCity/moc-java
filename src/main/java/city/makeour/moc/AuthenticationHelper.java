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
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(padHex(N));
            digest.update(padHex(g));
            k = new BigInteger(1, digest.digest());
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
        a = new BigInteger(1024, random).mod(N);
        A = g.modPow(a, N);
        poolName = userPoolId.split("_", 2)[1];
    }

    public BigInteger getA() {
        return A;
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

        BigInteger base = B.subtract(k.multiply(g.modPow(x, N))).mod(N);
        BigInteger exp = a.add(u.multiply(x));
        BigInteger S = base.modPow(exp, N).mod(N);

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

            String cleanedSecretBlock = secretBlock.trim(); // 余計な改行やスペース除去
            byte[] secretBlockBytes = Base64.getDecoder().decode(cleanedSecretBlock);
            System.out.println("secretBlockBytes.length = " + secretBlockBytes.length);

            byte[] message = concatBytes(
                    poolName.getBytes(StandardCharsets.UTF_8),
                    userIdForSRP.getBytes(StandardCharsets.UTF_8),
                    Base64.getDecoder().decode(secretBlock),
                    timestamp.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(mac.doFinal(message));
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

    public String getCurrentFormattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss 'UTC' yyyy", Locale.US);
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    // --- 内部ヘルパーメソッド ---

    private static byte[] padHex(BigInteger bigInt) {
        byte[] hex = bigInt.toByteArray();
        if (hex.length == 256) {
            return hex;
        } else if (hex.length > 256) {
            byte[] trimmed = new byte[256];
            System.arraycopy(hex, hex.length - 256, trimmed, 0, 256);
            return trimmed;
        } else {
            byte[] padded = new byte[256];
            System.arraycopy(hex, 0, padded, 256 - hex.length, hex.length);
            return padded;
        }
    }

    private static BigInteger computeU(BigInteger A, BigInteger B) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(padHex(A));
            digest.update(padHex(B));
            return new BigInteger(1, digest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BigInteger calculateX(String poolName, String username, String password, BigInteger salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Step1: (poolName + username + ":" + password) をSHA-256
            String userIdHashInput = poolName + username + ":" + password;
            byte[] userIdHash = digest.digest(userIdHashInput.getBytes(StandardCharsets.UTF_8));

            // Step2: (saltバイト列 + userIdHash) をSHA-256
            digest.reset();
            digest.update(salt.toByteArray());
            digest.update(userIdHash);

            byte[] xHash = digest.digest();

            return new BigInteger(1, xHash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] computeHKDF(byte[] ikm, byte[] salt) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(salt, "HmacSHA256"));
            byte[] prk = mac.doFinal(ikm);

            mac.init(new SecretKeySpec(prk, "HmacSHA256"));
            byte[] info = "Caldera Derived Key".getBytes(StandardCharsets.UTF_8);
            byte[] okm = mac.doFinal(info);

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
