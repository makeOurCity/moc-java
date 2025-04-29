package city.makeour.moc;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

public class FetchCognitoToken implements TokenFetcherInterface {

    protected CognitoIdentityProviderClient client;
    protected String cognitoUserPoolId;
    protected String cognitoClientId;
    protected String refreshToken;

    protected String username;
    protected String password;

    public FetchCognitoToken(String cognitoUserPoolId, String cognitoClientId) {
        this.cognitoClientId = cognitoClientId;
        this.cognitoUserPoolId = cognitoUserPoolId;
        this.refreshToken = null;
        this.client = CognitoIdentityProviderClient.builder()
                .defaultsMode(DefaultsMode.STANDARD)
                .region(Region.AP_NORTHEAST_1)
                .build();
    }

    public void setAuthParameters(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // SRPに必要な定数
    private static final BigInteger N = new BigInteger(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956AE515D2261898FA051015728E5A8AAAC42DAD33170D04507A33A85521ABDF1CBA64ECFB850458DBEF0A8AEA71575D060C7DB3970F85A6E1E4C7ABF5AE8CDB0933D71E8C94E04A25619DCEE3D2261AD2EE6BF12FFA06D98A0864D87602733EC86A64521F2B18177B200CBBE117577A615D6C770988C0BAD946E208E24FA074E5AB3143DB5BFCE0FD108E4B82D120A93AD2CAFFFFFFFFFFFFFFFF",
            16);
    private static final BigInteger g = BigInteger.valueOf(2);
    private static final BigInteger k = BigInteger.valueOf(3);
    private static final int EPHEMERAL_KEY_LENGTH = 1024;

    public String fetchToken() {
        if (this.refreshToken != null) {
            return fetchTokenWithRefresh();
        }
        return fetchTokenWithSRP();
    }

    private String fetchTokenWithRefresh() {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", this.refreshToken);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .clientId(this.cognitoClientId)
                .authParameters(authParameters)
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .build();

        InitiateAuthResponse response = this.client.initiateAuth(authRequest);
        AuthenticationResultType result = response.authenticationResult();
        this.refreshToken = result.refreshToken();
        return result.idToken();
    }

    private String fetchTokenWithSRP() {
        try {
            // Step 1: Generate SRP-A and initiate auth
            SecureRandom random = new SecureRandom();
            BigInteger a = new BigInteger(EPHEMERAL_KEY_LENGTH, random);
            BigInteger A = g.modPow(a, N);

            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("SRP_A", A.toString(16));

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .clientId(this.cognitoClientId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.USER_SRP_AUTH)
                    .build();

            InitiateAuthResponse initAuthResponse = this.client.initiateAuth(authRequest);

            // Step 2: Process challenge
            Map<String, String> challengeParams = initAuthResponse.challengeParameters();
            String userIdForSRP = challengeParams.get("USER_ID_FOR_SRP");
            BigInteger B = new BigInteger(challengeParams.get("SRP_B"), 16);
            String salt = challengeParams.get("SALT");
            String secretBlock = challengeParams.get("SECRET_BLOCK");

            // Step 3: Calculate proof and respond to challenge
            byte[] key = calculateSRPKey(a, B, salt, userIdForSRP);
            // Format timestamp as required by Cognito
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss 'UTC' yyyy", Locale.US);
            String dateNow = now.format(formatter);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(cognitoUserPoolId.split("_")[1].getBytes(StandardCharsets.UTF_8));
            digest.update(userIdForSRP.getBytes(StandardCharsets.UTF_8));
            digest.update(Base64.getDecoder().decode(secretBlock));
            digest.update(dateNow.getBytes(StandardCharsets.UTF_8));

            byte[] hmac = calculateHMAC(key, digest.digest());
            String proof = Base64.getEncoder().encodeToString(hmac);

            Map<String, String> challengeResponses = new HashMap<>();
            challengeResponses.put("USERNAME", userIdForSRP);
            challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", secretBlock);
            challengeResponses.put("TIMESTAMP", dateNow);
            challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", proof);

            RespondToAuthChallengeRequest challengeRequest = RespondToAuthChallengeRequest.builder()
                    .clientId(this.cognitoClientId)
                    .challengeName("PASSWORD_VERIFIER")
                    .challengeResponses(challengeResponses)
                    .build();

            RespondToAuthChallengeResponse challengeResponse = this.client.respondToAuthChallenge(challengeRequest);
            AuthenticationResultType result = challengeResponse.authenticationResult();

            this.refreshToken = result.refreshToken();
            return result.idToken();

        } catch (Exception e) {
            throw new RuntimeException("SRP authentication failed" + e.getMessage(), e);
        }
    }

    private byte[] calculateSRPKey(BigInteger a, BigInteger B, String salt, String userIdForSRP)
            throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // Calculate u = H(A || B)
        String concatenated = a.toString(16) + B.toString(16);
        byte[] uHash = digest.digest(concatenated.getBytes(StandardCharsets.UTF_8));
        BigInteger u = new BigInteger(1, uHash);

        // Calculate x = H(salt || H(userIdForSRP || ":" || password))
        digest.reset();
        digest.update((userIdForSRP + ":" + password).getBytes(StandardCharsets.UTF_8));
        byte[] userHash = digest.digest();

        digest.reset();
        digest.update(Base64.getDecoder().decode(salt));
        digest.update(userHash);
        BigInteger x = new BigInteger(1, digest.digest());

        // Calculate S = (B - k * g^x) ^ (a + u * x) % N
        BigInteger gx = g.modPow(x, N);
        BigInteger kgx = k.multiply(gx).mod(N);
        BigInteger difference = B.subtract(kgx).mod(N);
        BigInteger exponent = a.add(u.multiply(x));
        BigInteger S = difference.modPow(exponent, N);

        // Calculate K = H(S)
        digest.reset();
        return digest.digest(S.toString(16).getBytes(StandardCharsets.UTF_8));
    }

    private byte[] calculateHMAC(byte[] key, byte[] message) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec keySpec = new javax.crypto.spec.SecretKeySpec(key, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }
}
