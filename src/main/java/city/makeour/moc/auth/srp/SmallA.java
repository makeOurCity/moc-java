package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class SmallA {

    private LargeN N;
    private final BigInteger a;

    public SmallA(LargeN N) {
        this.N = N;
        a = new BigInteger(1, generateRandomBytes(128)).mod(this.N.value());
    }

    public BigInteger value() {
        return a;
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new java.security.SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
