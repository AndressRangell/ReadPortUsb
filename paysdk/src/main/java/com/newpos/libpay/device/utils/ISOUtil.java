package com.newpos.libpay.device.utils;

public class ISOUtil {

    /**
     * convierte una representacion BCD de un numero a una cadena
     *
     * @param source - representacion BCD
     * @param offset - desplazamiento inicial
     * @param length - tama�o de los Bytes a convertir
     * @retorna una cadena con la representacion del numero
     */
    public static String bcd2str(byte[] source, int offset, int length) {
        char[] ret = new char[length * 2];
        byte car;

        int counter;
        int indexString = 0;

        for (counter = offset; counter < length + offset; counter++) {
            car = (byte) ((source[counter] & 0xF0) >> 4);
            ret[indexString] = (char) (car + ((car < 10) ? '0' : '7'));
            indexString++;
            car = (byte) (source[counter] & 0x0F);
            ret[indexString] = (char) (car + ((car < 10) ? '0' : '7'));
            indexString++;
        }

        return new String(ret);
    }

    /**
     * converts a BCD representation of a number to a String
     *
     * @param b       - BCD representation
     * @param offset  - starting offset
     * @param len     - BCD field len
     * @param padLeft - was padLeft packed?
     * @return the String representation of the number
     */
    public static String bcd2str(byte[] b, int offset, int len, boolean padLeft) {
        StringBuilder d = new StringBuilder(len);
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            int shift = (i & 1) == 1 ? 0 : 4;
            char c = Character.forDigit(b[offset + (i >> 1)] >> shift & 0x0F, 16);
            if (c == 'd')
                c = '=';
            d.append(Character.toUpperCase(c));
        }
        return d.toString();
    }

    /**
     * converts to BCD
     *
     * @param s       - the number
     * @param padLeft - flag indicating left/right padding
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft) {
        if (s == null)
            return null;
        int len = s.length();
        byte[] d = new byte[len + 1 >> 1];
        return str2bcd(s, padLeft, d, 0);
    }

    /**
     * converts to BCD
     *
     * @param s       - the number
     * @param padLeft - flag indicating left/right padding
     * @param d       The byte array to copy into.
     * @param offset  Where to start copying into.
     * @return BCD representation of the number
     */
    public static byte[] str2bcd(String s, boolean padLeft, byte[] d, int offset) {
        char c;
        int len = s.length();
        int start = (len & 1) == 1 && padLeft ? 1 : 0;
        for (int i = start; i < len + start; i++) {
            c = s.charAt(i - start);
            if (c >= '0' && c <= '?') // 30~3f
                c -= '0';
            else {
                c &= ~0x20;
                c -= 'A' - 10;
            }
            d[offset + (i >> 1)] |= c << ((i & 1) == 1 ? 0 : 4);
        }
        return d;
    }

    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        //StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            //temp.append(decimal);
        }
        //Logger.debug("Decimal : " + temp.toString());
        //temp.toString();
        return sb.toString();
    }

}
