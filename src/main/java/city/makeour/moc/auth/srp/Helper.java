package city.makeour.moc.auth.srp;

import java.math.BigInteger;

public class Helper {
    public static byte[] padHex(BigInteger bigInt) {
        boolean isNegative = bigInt.signum() < 0;
        String hex = bigInt.abs().toString(16);

        // 奇数桁なら先頭に 0 を追加
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }

        // MSBが立っていれば "00" を先頭に追加（符号ビットを避ける）
        if (hex.matches("^[89a-fA-F].*")) {
            hex = "00" + hex;
        }

        if (isNegative) {
            // 補数計算: 反転 → +1
            StringBuilder flipped = new StringBuilder();
            for (char c : hex.toCharArray()) {
                int nibble = ~Character.digit(c, 16) & 0xF;
                flipped.append(Integer.toHexString(nibble));
            }
            BigInteger flippedBigInt = new BigInteger(flipped.toString(), 16).add(BigInteger.ONE);
            hex = flippedBigInt.toString(16);

            // MSBがFF8〜なら短縮される→不要（JSでも無視できるレベル）
            if (hex.length() % 2 != 0)
                hex = "0" + hex;
        }

        return hexStringToByteArray(hex);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
