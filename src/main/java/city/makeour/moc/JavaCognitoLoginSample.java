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

public class JavaCognitoLoginSample {

    public static void main(String[] args) {
        // あなたの設定
        String userPoolId = "ap-northeast-1_nXSBLO7v6";
        String clientId = "3d7d0piq75halieshbi7o8keca";
        String username = "ushio.s@gmail.com"; // ここはUUIDじゃない、メールアドレス！
        String password = "";

        // クライアント作成
        CognitoIdentityProviderClient client = CognitoIdentityProviderClient.builder()
                .region(Region.AP_NORTHEAST_1)
                .build();

        // SRP認証用ヘルパー
        AuthenticationHelper authHelper = new AuthenticationHelper(userPoolId);

        // SRP_A送信
        Map<String, String> initAuthParams = new HashMap<>();
        initAuthParams.put("USERNAME", username);
        initAuthParams.put("SRP_A", authHelper.getA().toString(16));

        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_SRP_AUTH)
                .clientId(clientId)
                .authParameters(initAuthParams)
                .build();

        InitiateAuthResponse initiateAuthResponse = client.initiateAuth(initiateAuthRequest);

        if (!ChallengeNameType.PASSWORD_VERIFIER.toString().equals(initiateAuthResponse.challengeNameAsString())) {
            throw new RuntimeException("Unexpected challenge: " + initiateAuthResponse.challengeNameAsString());
        }

        Map<String, String> challengeParams = initiateAuthResponse.challengeParameters();
        String salt = challengeParams.get("SALT");
        String srpB = challengeParams.get("SRP_B");
        String secretBlock = challengeParams.get("SECRET_BLOCK");
        String userIdForSRP = challengeParams.get("USER_ID_FOR_SRP");

        // SRP計算
        byte[] passwordKey = authHelper.getPasswordAuthenticationKey(username, password, srpB, salt, secretBlock);
        String timestamp = authHelper.getCurrentFormattedTimestamp();
        String signature = authHelper.calculateSignature(userIdForSRP, secretBlock, timestamp, passwordKey);

        // チャレンジ応答
        Map<String, String> challengeResponses = new HashMap<>();
        challengeResponses.put("PASSWORD_CLAIM_SECRET_BLOCK", secretBlock);
        challengeResponses.put("PASSWORD_CLAIM_SIGNATURE", signature);
        challengeResponses.put("TIMESTAMP", timestamp);
        challengeResponses.put("USERNAME", username);

        RespondToAuthChallengeRequest challengeRequest = RespondToAuthChallengeRequest.builder()
                .challengeName(ChallengeNameType.PASSWORD_VERIFIER)
                .clientId(clientId)
                .session(initiateAuthResponse.session())
                .challengeResponses(challengeResponses)
                .build();

        RespondToAuthChallengeResponse authChallengeResponse = client.respondToAuthChallenge(challengeRequest);

        // 成功したらIDトークン表示！
        String idToken = authChallengeResponse.authenticationResult().idToken();
        System.out.println("ID Token:");
        System.out.println(idToken);
    }
}
