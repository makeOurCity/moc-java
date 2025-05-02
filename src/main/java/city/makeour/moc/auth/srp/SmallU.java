package city.makeour.moc.auth.srp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SmallU {

    private LargeA A;
    private LargeB B;

    public SmallU(LargeA A, LargeB B) {
        this.A = A;
        this.B = B;
    }

    public BigInteger value() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aPadded = Helper.padHex(A.value());
        byte[] bPadded = Helper.padHex(B.value());
        byte[] combined = new byte[aPadded.length + bPadded.length];
        System.arraycopy(aPadded, 0, combined, 0, aPadded.length);
        System.arraycopy(bPadded, 0, combined, aPadded.length, bPadded.length);
        byte[] uHash = digest.digest(combined);
        return new BigInteger(1, uHash);
    }
}
