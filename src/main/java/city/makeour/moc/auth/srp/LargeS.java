package city.makeour.moc.auth.srp;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class LargeS {

    private LargeB B;
    private SmallK k;
    private SmallG g;
    private SmallX x;
    private SmallA a;
    private SmallU u;
    private LargeN N;

    public LargeS(LargeB B, SmallK k, SmallG g, SmallX x, SmallA a, SmallU u, LargeN N) {
        this.B = B;
        this.k = k;
        this.g = g;
        this.x = x;
        this.a = a;
        this.u = u;
        this.N = N;
    }

    public BigInteger value() throws NoSuchAlgorithmException {
        BigInteger NValue = this.N.value();
        BigInteger BValue = this.B.value();
        BigInteger xValue = this.x.value();
        BigInteger aValue = this.a.value();
        BigInteger uValue = this.u.value();
        BigInteger kValue = this.k.value();
        BigInteger gValue = this.g.value();

        BigInteger gModPowX = gValue.modPow(xValue, NValue);
        BigInteger kgx = kValue.multiply(gModPowX).mod(NValue);
        BigInteger base = BValue.subtract(kgx).mod(NValue);
        BigInteger exp = aValue.add(uValue.multiply(xValue));
        return base.modPow(exp, NValue);
    }
}
