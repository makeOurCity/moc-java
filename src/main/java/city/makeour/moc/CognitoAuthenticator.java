package city.makeour.moc;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ChallengeNameType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.RespondToAuthChallengeResponse;

public class CognitoAuthenticator {

    private final CognitoIdentityProviderClient cognitoClient;
    private final String clientId;
    private final String userPoolId;

    public CognitoAuthenticator(Region region, String clientId, String userPoolId) {
        this.cognitoClient = CognitoIdentityProviderClient.builder()
                .region(region)
                .build();
        this.clientId = clientId;
        this.userPoolId = userPoolId;
    }

    public String login(String username, String password) {
        AuthenticationHelper authHelper = new AuthenticationHelper(userPoolId);

        // ステップ1: SRP_A計算
        String srpAString = authHelper.getA().toString(16);

        // ステップ2: InitiateAuthで認証スタート
        Map<String, String> initAuthParams = new HashMap<>();
        initAuthParams.put("USERNAME", username);
        initAuthParams.put("SRP_A", srpAString);

        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_SRP_AUTH)
                .clientId(clientId)
                .authParameters(initAuthParams)
                .build();

        InitiateAuthResponse initiateAuthResponse = cognitoClient.initiateAuth(initiateAuthRequest);

        if (!ChallengeNameType.PASSWORD_VERIFIER.toString().equals(initiateAuthResponse.challengeNameAsString())) {
            throw new RuntimeException("Unexpected challenge: " + initiateAuthResponse.challengeNameAsString());
        }

        // ステップ3: チャレンジ受け取り
        Map<String, String> challengeParameters = initiateAuthResponse.challengeParameters();

        String salt = challengeParameters.get("SALT");
        String srpB = challengeParameters.get("SRP_B");
        String secretBlock = challengeParameters.get("SECRET_BLOCK");
        String userIdForSRP = challengeParameters.get("USER_ID_FOR_SRP");

        // ステップ4: パスワード認証キー計算
        byte[] passwordAuthKey = authHelper.getPasswordAuthenticationKey(userPoolId, username, password, srpB, salt,
                secretBlock);

        // ステップ5: 署名生成
        String timestamp = authHelper.getCurrentFormattedTimestamp();
        String signature = authHelper.calculateSignature(userIdForSRP, secretBlock, timestamp, passwordAuthKey);

        // ステップ6: RespondToAuthChallenge
        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", secretBlock);
        challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", signature);
        challengeResponses.put("TIMESTAMP", timestamp);
        challengeResponses.put("USERNAME", userIdForSRP);

        RespondToAuthChallengeRequest challengeRequest = RespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.PASSWORD_VERIFIER)
                .clientId(clientId)
                .session(initiateAuthResponse.session())
                .challengeResponses(challengeResponses)
                .build();

        RespondToAuthChallengeResponse challengeResponse = cognitoClient.respondToAuthChallenge(challengeRequest);

        // ステップ7: 成功！ IDToken取得
        return challengeResponse.authenticationResult().idToken();
    }
}
