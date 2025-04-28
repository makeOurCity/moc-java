package city.makeour;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.awscore.defaultsmode.DefaultsMode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;

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

    public String fetchToken() {
        InitiateAuthRequest authRequest = null;

        if (this.refreshToken != null) {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("REFRESH_TOKEN", this.refreshToken);

            authRequest = InitiateAuthRequest.builder()
                    .clientId(this.cognitoClientId)
                    // .userPoolId(this.cognitoUserPoolId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.USER_SRP_AUTH)
                    .build();
        } else {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("PASSWORD", password);

            authRequest = InitiateAuthRequest.builder()
                    .clientId(this.cognitoClientId)
                    // .userPoolId(this.cognitoUserPoolId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.USER_SRP_AUTH)
                    .build();
        }

        InitiateAuthResponse response = this.client.initiateAuth(authRequest);
        AuthenticationResultType result = response.authenticationResult();

        this.refreshToken = result.refreshToken();

        if (this.refreshToken == null) {
            System.out.println("Failed to fetch token");
        }

        return result.idToken();
    }
}
