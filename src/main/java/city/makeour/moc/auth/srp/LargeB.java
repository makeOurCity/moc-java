package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class LargeB {

    private String B;

    public LargeB(String B) {
        this.B = B;
    }

    public BigInteger value() {
        return new BigInteger(1, Helper.hexStringToByteArray(this.B));
    }
}
