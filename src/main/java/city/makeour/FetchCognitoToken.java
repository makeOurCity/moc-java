package city.makeour;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

public class FetchCognitoToken {

    protected CognitoIdentityProviderClient client;
    protected String cognitoUserPoolId;
    protected String cognitoClientId;
    protected String refreshToken;

    protected String username;
    protected String password;

    public void construct(String cognitoUserPoolId, String cognitoClientId) {
        this.cognitoClientId = cognitoClientId;
        this.cognitoUserPoolId = cognitoUserPoolId;
        this.refreshToken = null;
        // this.client = CognitoIdentityProviderClient.getInstance();
    }

    public void setAuthParameters(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String fetchToken() {
        AdminInitiateAuthRequest authRequest = null;

        if (this.refreshToken != null) {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("REFRESH_TOKEN", this.refreshToken);

            authRequest = AdminInitiateAuthRequest.builder()
                    .clientId(this.cognitoClientId)
                    .userPoolId(this.cognitoUserPoolId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .build();
        } else {
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", username);
            authParameters.put("PASSWORD", password);

            authRequest = AdminInitiateAuthRequest.builder()
                    .clientId(this.cognitoClientId)
                    .userPoolId(this.cognitoUserPoolId)
                    .authParameters(authParameters)
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .build();
        }

        AdminInitiateAuthResponse response = this.client.adminInitiateAuth(authRequest);
        AuthenticationResultType result = response.authenticationResult();

        this.refreshToken = result.refreshToken();

        if (this.refreshToken == null) {
            System.out.println("Failed to fetch token");
        }

        return result.idToken();
    }
}
