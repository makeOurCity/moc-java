package city.makeour.moc;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

public class Exec {
    public static void main(String[] args) {
        String userPoolId = "ap-northeast-1_nXSBLO7v6"; // ← ここあなたの設定
        String userPoolName = "nXSBLO7v6";
        String clientId = "3d7d0piq75halieshbi7o8keca"; // ← ここあなたの設定
        String username = "ushio.s@gmail.com"; // ← ここあなたの設定
        String password = ""; // ← ここあなたの設定
        Region region = Region.AP_NORTHEAST_1;

        AuthenticationHelper helper = new AuthenticationHelper(userPoolId);

        CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                .region(region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        try {
            InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                    .authFlow(AuthFlowType.USER_SRP_AUTH)
                    .clientId(clientId)
                    .authParameters(Map.of(
                            "USERNAME", username,
                            "SRP_A", helper.getA().toString(16)))
                    .build();

            System.out.println("SRP_A = " + helper.getHexA());

            InitiateAuthResponse initiateAuthResponse = cognitoClient.initiateAuth(initiateAuthRequest);
            Map<String, String> challengeParameters = initiateAuthResponse.challengeParameters();

            String userIdForSrp = challengeParameters.get("USER_ID_FOR_SRP");
            String salt = challengeParameters.get("SALT");
            String srpB = challengeParameters.get("SRP_B");
            String secretBlock = challengeParameters.get("SECRET_BLOCK");

            // ← ここで一度ログ出力して比較用データを取得
            System.out.println("userIdForSrp = " + userIdForSrp);
            System.out.println("salt = " + salt);
            System.out.println("srpB = " + srpB);
            System.out.println("secretBlock = " + secretBlock);

            byte[] signatureKey = helper.getPasswordAuthenticationKey(userPoolName, userIdForSrp, password, srpB, salt,
                    secretBlock);
            String timestamp = helper.getCurrentFormattedTimestamp();
            String signature = helper.calculateSignature(userIdForSrp, secretBlock, timestamp, signatureKey);

            // ← signature計算結果も出力
            System.out.println("timestamp = " + timestamp);
            System.out.println("signature = " + signature);

            Map<String, String> challengeResponses = new HashMap<>();
            challengeResponses.put("USERNAME", userIdForSrp);
            challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", secretBlock);
            challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", signature);
            challengeResponses.put("TIMESTAMP", timestamp);

            System.out.println("challengeResponses = " + challengeResponses);

            RespondToAuthChallengeRequest respondRequest = RespondToAuthChallengeRequest.builder()
                    .challengeName(ChallengeNameType.PASSWORD_VERIFIER)
                    .clientId(clientId)
                    .challengeResponses(challengeResponses)
                    .build();

            RespondToAuthChallengeResponse authChallengeResponse = cognitoClient.respondToAuthChallenge(respondRequest);
            AuthenticationResultType authResult = authChallengeResponse.authenticationResult();

            System.out.println("ID Token:");
            System.out.println(authResult.idToken());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cognitoClient.close();
        }
    }

    public static class AuthenticationHelper {
        private static final BigInteger N = new BigInteger(1, hexStringToByteArray(
                "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD"
                        +
                        "3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F"
                        +
                        "24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552"
                        +
                        "BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF0"
                        +
                        "6F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64EC"
                        +
                        "FB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A"
                        +
                        "0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D1"
                        +
                        "20A93AD2CAFFFFFFFFFFFFFFFF"));
        private static final BigInteger g = BigInteger.valueOf(2);
        private static final BigInteger k = calculateK(N, g);

        private final BigInteger a;
        private final BigInteger A;
        private final String poolName;

        public AuthenticationHelper(String userPoolId) {
            this.poolName = userPoolId;
            // this.a = new BigInteger(1, generateRandomBytes(128)).mod(N);
            this.a = new BigInteger("123456789abcdef123456789abcdef123456789abcdef", 16);
            this.A = g.modPow(a, N);
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

        public byte[] getPasswordAuthenticationKey(String poolName, String username, String password, String srpBHex,
                String saltHex,
                String secretBlock) {
            // BigInteger B = new BigInteger(1, hexStringToByteArray(srpBHex));
            // srpBHex =
            // "6472ea13e9cd1f054fb17c211f26831c47a615ca8c42cdab2893c1a015965c658b93c8324e90436eb6c6cf730ffd7db2200f716b74e340f9b2c02654362f99f1f78c610f6c0d4bc4996963063e4ca6af87f3516536fda0b0116695ec7a564ec9992f1b6e862e86c4f326d205d3c1accad76e41eca1e5a83ffdf8a58b3c5673f21a26f5cbc8452bf84945b5898ddb5a4b120ff09de4a7a63cbd7ec11f9601b90735c65b02075054e448a64ba7ebf826682e1a1e92990714b8725862bb6a6f0b1eb803ad13fddcac087a8b79a2402fc15421261caaad1db51fc3133469ef5db789af25baddb513986d5e5555683fe5d47f2e0c0e9eb048e81ff3dc372a9b862345f0a4ab0818f72d89f464959f22795b38347627e3fee375ac8fbc51ab8d31e438dac6926c7b1c690d32c5c6a75ee37e84886973d18cb8dcb75c3e835007df6ddcc91d13e4e99b6e291d7e9260ddcfbee8620ddc2f9fc7406369c5d085cc3ceba7207b376df8806a88c3a3ff404eba276298edb936111b41d0f9042c07581dd0e1";
            BigInteger B = new BigInteger(1, hexStringToByteArray(srpBHex));
            if (B.mod(N).equals(BigInteger.ZERO)) {
                throw new IllegalStateException("Invalid server B value");
            }
            BigInteger u = computeU(A, B);
            System.out.println("Java U = " + u.toString(16));
            BigInteger salt = new BigInteger(1, hexStringToByteArray(saltHex));
            BigInteger x = calculateX(poolName, username, password, salt);
            System.out.println("Java x = " + x.toString(16));
            BigInteger S = calculateS(B, k, g, x, a, u, N);
            System.out.println("Java S = " + S.toString(16));
            byte[] signatureKey = computeHKDF(padHex(S), u.toByteArray());
            System.out.println("Java K = " + Base64.getEncoder().encodeToString(signatureKey));

            System.out.println("Java N = " + N.toString(16));
            return signatureKey;
        }

        public String calculateSignature(String userIdForSRP, String secretBlock, String timestamp, byte[] key) {
            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(key, "HmacSHA256"));

                byte[] poolNameBytes = poolName.split("_")[1].getBytes(StandardCharsets.UTF_8);
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

                System.out.println("Java message (userPoolName) = " + new String(poolNameBytes));
                System.out.println("Java message (username)     = " + new String(userIdBytes));
                System.out.println(
                        "Java message (secretBlock)  = " + Base64.getEncoder().encodeToString(secretBlockBytes));
                System.out.println("Java message (timestamp)    = " + new String(timestampBytes));
                System.out.println("Java message (hex)          = " + toHex(message));

                byte[] rawSignature = mac.doFinal(message);
                return Base64.getEncoder().encodeToString(rawSignature);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public String getCurrentFormattedTimestamp() {
            SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", java.util.Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            return format.format(new Date());
        }

        private static BigInteger calculateK(BigInteger N, BigInteger g) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] nPadded = padHex(N);
                byte[] gPadded = padHex(g);
                byte[] ng = new byte[nPadded.length + gPadded.length];
                System.arraycopy(nPadded, 0, ng, 0, nPadded.length);
                System.arraycopy(gPadded, 0, ng, nPadded.length, gPadded.length);
                byte[] hash = digest.digest(ng);
                return new BigInteger(1, hash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static BigInteger calculateX(String poolName, String username, String password, BigInteger salt) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                String userPass = poolName + username + ":" + password;
                byte[] userPassHash = digest.digest(userPass.getBytes(StandardCharsets.UTF_8)); // inner hash

                // JS の padHex 相当：符号なしの even-length hex 文字列を作成
                String saltHex = salt.toString(16);
                if (saltHex.length() % 2 != 0)
                    saltHex = "0" + saltHex;
                if (saltHex.matches("^[89a-fA-F].*"))
                    saltHex = "00" + saltHex;
                byte[] saltBytes = hexStringToByteArray(saltHex);

                byte[] combined = new byte[saltBytes.length + userPassHash.length];
                System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
                System.arraycopy(userPassHash, 0, combined, saltBytes.length, userPassHash.length);

                byte[] xHash = digest.digest(combined); // final hash
                return new BigInteger(1, xHash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static BigInteger computeU(BigInteger A, BigInteger B) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] aPadded = padHex(A);
                byte[] bPadded = padHex(B);
                System.out.println("padHex(A) = " + toHex(padHex(A)));
                System.out.println("padHex(B) = " + toHex(padHex(B)));
                byte[] combined = new byte[aPadded.length + bPadded.length];
                System.arraycopy(aPadded, 0, combined, 0, aPadded.length);
                System.arraycopy(bPadded, 0, combined, aPadded.length, bPadded.length);
                byte[] uHash = digest.digest(combined);
                return new BigInteger(1, uHash);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static String toHex(byte[] bytes) {
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        private static BigInteger calculateS(BigInteger B, BigInteger k, BigInteger g, BigInteger x, BigInteger a,
                BigInteger u, BigInteger N) {
            BigInteger gModPowX = g.modPow(x, N);
            BigInteger kgx = k.multiply(gModPowX).mod(N);
            BigInteger base = B.subtract(kgx).mod(N);
            BigInteger exp = a.add(u.multiply(x));
            return base.modPow(exp, N);
        }

        private static byte[] computeHKDF(byte[] ikm, byte[] salt) {
            try {
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private static byte[] padHexOld(BigInteger n) {
            byte[] hex = n.toByteArray();
            if (hex.length == 256) {
                return hex;
            }
            byte[] padded = new byte[256];
            if (hex.length > 256) {
                System.arraycopy(hex, hex.length - 256, padded, 0, 256);
            } else {
                System.arraycopy(hex, 0, padded, 256 - hex.length, hex.length);
            }
            return padded;
        }

        private static byte[] padHex(BigInteger bigInt) {
            boolean isNegative = bigInt.signum() < 0;
            String hex = bigInt.abs().toString(16);

            // 奇数桁なら先頭に 0 を追加
            if (hex.length() % 2 != 0) {
                hex = "0" + hex;
            }

            // MSBが立っていれば "00" を先頭に追加（符号ビットを避ける）
            if (hex.matches("^[89a-fA-F].*")) {
                hex = "00" + hex;
            }

            if (isNegative) {
                // 補数計算: 反転 → +1
                StringBuilder flipped = new StringBuilder();
                for (char c : hex.toCharArray()) {
                    int nibble = ~Character.digit(c, 16) & 0xF;
                    flipped.append(Integer.toHexString(nibble));
                }
                BigInteger flippedBigInt = new BigInteger(flipped.toString(), 16).add(BigInteger.ONE);
                hex = flippedBigInt.toString(16);

                // MSBがFF8〜なら短縮される→不要（JSでも無視できるレベル）
                if (hex.length() % 2 != 0)
                    hex = "0" + hex;
            }

            return hexStringToByteArray(hex);
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
    }
}
