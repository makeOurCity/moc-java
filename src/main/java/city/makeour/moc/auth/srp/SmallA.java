package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class SmallA {

    private LargeN N;

    public SmallA(LargeN N) {
        this.N = N;
    }

    public BigInteger value() {
        return new BigInteger(1, generateRandomBytes(128)).mod(this.N.value());
    }

    private static byte[] generateRandomBytes(int size) {
        byte[] bytes = new byte[size];
        new java.security.SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
