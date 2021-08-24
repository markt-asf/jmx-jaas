package org.apache.markt;

public class Decoder {

    public static char[] decode(char[] encodedPassword) {
        char[] decodedPassword = new char[encodedPassword.length];
        int j = 0;
        for (int i = encodedPassword.length - 1; i > -1; i--) {
            decodedPassword[j++] = encodedPassword[i];
        }
        return decodedPassword;
    }
}
