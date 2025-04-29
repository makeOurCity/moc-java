package city.makeour.moc;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Description;

import software.amazon.awssdk.regions.Region;

public class CognitoAuthenticationHelperTest {

    // Test cases for the CognitoAuthenticationHelper class
    // Add your test methods here

    static {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
        System.setProperty("software.amazon.awssdk.eventstream.rpc", "debug");
    }

    // Example test method
    @Test
    @Description("CognitoAuthenticatorのloginメソッドのテスト")
    public void testLogin() {
        String userPoolId = "ap-northeast-1_nXSBLO7v6";
        String clientId = "3d7d0piq75halieshbi7o8keca";
        String username = "ushio.s@gmail.com";
        String password = "ushioshugo";

        CognitoAuthenticator authenticator = new CognitoAuthenticator(
                Region.AP_NORTHEAST_1,
                clientId,
                userPoolId);

        String idToken = authenticator.login(username, password);

        System.out.println("ID Token:");
        System.out.println(idToken);
    }
}
