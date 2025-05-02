package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class LargeA {
    private LargeN N;
    private SmallA a;
    private SmallG g;

    public LargeA(LargeN N, SmallA a, SmallG g) {
        this.N = N;
        this.a = a;
        this.g = g;
    }

    public BigInteger value() {
        BigInteger gValue = this.g.value();
        BigInteger aValue = this.a.value();
        BigInteger NValue = this.N.value();
        return gValue.modPow(aValue, NValue);
    }
}
