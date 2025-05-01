package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class FetchCognitoTokenTest {

    @Test
    public void testFetchTokenWithSrpAuth() throws Exception {
        String cognitoUserPoolId = "ap-northeast-1_nXSBLO7v6";
        String cognitoClientId = "3d7d0piq75halieshbi7o8keca";
        String username = "ushio.s@gmail.com";
        String password = "";

        FetchCognitoToken fetchCognitoToken = new FetchCognitoToken(cognitoUserPoolId, cognitoClientId);
        fetchCognitoToken.setAuthParameters(username, password);
        String token = fetchCognitoToken.fetchTokenWithSrpAuth();
        assertNotNull(token);
    }
}