package city.makeour.moc.auth.srp;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SmallK {
    private LargeN N;
    private SmallG g;

    SmallK(LargeN N, SmallG g) {
        this.N = N;
        this.g = g;
    }

    public BigInteger value() throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] nPadded = Helper.padHex(this.N.value());
        byte[] gPadded = Helper.padHex(this.g.value());
        byte[] ng = new byte[nPadded.length + gPadded.length];
        System.arraycopy(nPadded, 0, ng, 0, nPadded.length);
        System.arraycopy(gPadded, 0, ng, nPadded.length, gPadded.length);
        byte[] hash = digest.digest(ng);
        return new BigInteger(1, hash);
    }
}
