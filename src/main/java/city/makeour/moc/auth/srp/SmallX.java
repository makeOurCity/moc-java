package city.makeour.moc.auth.srp;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SmallX {

    private String userPoolName;
    private String userIdForSrp;
    private String password;
    private BigInteger salt;

    public SmallX(String userPoolName, String userIdForSrp, String password, BigInteger salt) {
        this.userPoolName = userPoolName;
        this.userIdForSrp = userIdForSrp;
        this.password = password;
        this.salt = salt;
    }

    public BigInteger value() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String userPass = this.userPoolName + this.userIdForSrp + ":" + password;
        byte[] userPassHash = digest.digest(userPass.getBytes(StandardCharsets.UTF_8)); // inner hash

        // JS の padHex 相当：符号なしの even-length hex 文字列を作成
        String saltHex = this.salt.toString(16);
        if (saltHex.length() % 2 != 0)
            saltHex = "0" + saltHex;
        if (saltHex.matches("^[89a-fA-F].*"))
            saltHex = "00" + saltHex;
        byte[] saltBytes = Helper.hexStringToByteArray(saltHex);

        byte[] combined = new byte[saltBytes.length + userPassHash.length];
        System.arraycopy(saltBytes, 0, combined, 0, saltBytes.length);
        System.arraycopy(userPassHash, 0, combined, saltBytes.length, userPassHash.length);

        byte[] xHash = digest.digest(combined); // final hash
        return new BigInteger(1, xHash);
    }
}
