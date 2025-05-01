package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class SmallG {
    private static final BigInteger G = BigInteger.valueOf(2);;

    public BigInteger value() {
        return G;
    }

}
