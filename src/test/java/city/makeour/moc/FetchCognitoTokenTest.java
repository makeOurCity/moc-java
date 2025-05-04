package city.makeour.moc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariables;
import org.springframework.context.annotation.Description;

class FetchCognitoTokenTest {

    @Test
    @Description("Cognitoのトークンを取得するテスト")
    @EnabledIfEnvironmentVariables({
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USER_POOL_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_CLIENT_ID", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_USERNAME", matches = ".*"),
            @EnabledIfEnvironmentVariable(named = "TEST_COGNITO_PASSWORD", matches = ".*")
    })
    public void testFetchTokenWithSrpAuth() throws Exception {
        String cognitoUserPoolId = System.getenv("TEST_COGNITO_USER_POOL_ID");
        String cognitoClientId = System.getenv("TEST_COGNITO_CLIENT_ID");
        String username = System.getenv("TEST_COGNITO_USERNAME");
        String password = System.getenv("TEST_COGNITO_PASSWORD");

        FetchCognitoToken fetchCognitoToken = new FetchCognitoToken(cognitoUserPoolId, cognitoClientId);
        fetchCognitoToken.setAuthParameters(username, password);
        Token token = fetchCognitoToken.fetchTokenWithSrpAuth();
        assertNotNull(token.getIdToken());
        assertNotNull(token.getRefreshToken());
    }
}