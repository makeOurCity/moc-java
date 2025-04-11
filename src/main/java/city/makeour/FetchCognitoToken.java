package city.makeour;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminInitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;

public class FetchCognitoToken {

    protected CognitoClient client;
    protected String cognitoUserPoolId;
    protected String cognitoClientId;
    protected String refreshToken;

    public void construct(String cognitoUserPoolId, String cognitoClientId) {
        this.cognitoClientId = cognitoClientId;
        this.cognitoUserPoolId = cognitoUserPoolId;
        this.refreshToken = null;
        this.client = CognitoClient.getInstance();
    }

    public String fetchToken(String username, String password) {
        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        AdminInitiateAuthRequest request = new AdminInitiateAuthRequest();
        request
                .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .withUserPoolId(this.cognitoUserPoolId)
                .withClientId(this.cognitoClientId)
                .withAuthParameters(authParameters);

        String idToken = null;
        AdminInitiateAuthResult response = client.adminInitiateAuth(request);
        response.getAuthenticationResult().ifPresent(authResult -> {
            this.refreshToken = authResult.getRefreshToken();
            idToken = authResult.getIdToken();
            System.out.println("Access Token: " + authResult.getAccessToken());
            System.out.println("ID Token: " + authResult.getIdToken());
            System.out.println("Refresh Token: " + this.refreshToken);
        });
        if (this.refreshToken == null) {
            System.out.println("Failed to fetch token");
        }

        return idToken;
    }

    public String refreshToken() {
        if (this.refreshToken == null) {
            System.out.println("Refresh token is null");
            return null;
        }

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("REFRESH_TOKEN", this.refreshToken);

        AdminInitiateAuthRequest request = new AdminInitiateAuthRequest();
        request
                .withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .withUserPoolId(this.cognitoUserPoolId)
                .withClientId(this.cognitoClientId)
                .withAuthParameters(authParameters);

        String idToken = null;
        AdminInitiateAuthResult response = client.adminInitiateAuth(request);
        response.getAuthenticationResult().ifPresent(authResult -> {
            this.refreshToken = authResult.getRefreshToken();
            idToken = authResult.getIdToken();
            System.out.println("Access Token: " + authResult.getAccessToken());
            System.out.println("ID Token: " + authResult.getIdToken());
            System.out.println("Refresh Token: " + this.refreshToken);
        });
        if (this.refreshToken == null) {
            System.out.println("Failed to refresh token");
        }
        return idToken;
    }
}
