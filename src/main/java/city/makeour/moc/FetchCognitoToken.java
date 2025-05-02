package city.makeour.moc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import city.makeour.moc.auth.srp.SrpAuthenticationHelper;
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

public class FetchCognitoToken implements TokenFetcherInterface {

    private String cognitoUserPoolId;
    private String cognitoClientId;
    private String username;
    private String password;
    private Region region = Region.AP_NORTHEAST_1;

    private SrpAuthenticationHelper helper;
    private CognitoIdentityProviderClient cognitoClient;

    public FetchCognitoToken(String cognitoUserPoolId, String cognitoClientId) {
        this(Region.AP_NORTHEAST_1, cognitoUserPoolId, cognitoClientId);
    }

    public FetchCognitoToken(Region region, String cognitoUserPoolId, String cognitoClientId) {
        this.cognitoUserPoolId = cognitoUserPoolId;
        this.cognitoClientId = cognitoClientId;
        this.region = region;

        this.helper = new SrpAuthenticationHelper(this.cognitoUserPoolId);

        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(this.region)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    public void setAuthParameters(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public Token fetchTokenWithSrpAuth() throws InvalidKeyException, NoSuchAlgorithmException {
        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_SRP_AUTH)
                .clientId(this.cognitoClientId)
                .authParameters(Map.of(
                        "USERNAME", this.username,
                        "SRP_A", helper.getA()))
                .build();
        InitiateAuthResponse initiateAuthResponse = cognitoClient.initiateAuth(initiateAuthRequest);
        Map<String, String> challengeParameters = initiateAuthResponse.challengeParameters();

        String userIdForSrp = challengeParameters.get("USER_ID_FOR_SRP");
        String salt = challengeParameters.get("SALT");
        String srpB = challengeParameters.get("SRP_B");
        String secretBlock = challengeParameters.get("SECRET_BLOCK");

        byte[] signatureKey = helper.getPasswordAuthenticationKey(userIdForSrp, password,
                srpB, salt,
                secretBlock);
        String timestamp = helper.getCurrentFormattedTimestamp();
        String signature = helper.calculateSignature(userIdForSrp, secretBlock, timestamp, signatureKey);

        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("USERNAME", userIdForSrp);
        challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", secretBlock);
        challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", signature);
        challengeResponses.put("TIMESTAMP", timestamp);

        RespondToAuthChallengeRequest respondRequest = RespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.PASSWORD_VERIFIER)
                .clientId(this.cognitoClientId)
                .challengeResponses(challengeResponses)
                .build();

        RespondToAuthChallengeResponse authChallengeResponse = cognitoClient.respondToAuthChallenge(respondRequest);
        AuthenticationResultType authResult = authChallengeResponse.authenticationResult();

        return new Token(authResult.idToken(), authResult.refreshToken());
    }

    public Token fetchToken() throws InvalidKeyException, NoSuchAlgorithmException {
        return this.fetchTokenWithSrpAuth();
    }
}
