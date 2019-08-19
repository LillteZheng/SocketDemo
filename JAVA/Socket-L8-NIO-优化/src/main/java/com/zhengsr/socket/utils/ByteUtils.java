package net.qiujuer.library.clink.utils;

public class ByteUtils {
    /**
     * Does this byte array begin with match array content?
     *
     * @param source Byte array to examine
     * @param match  Byte array to locate in <code>source</code>
     * @return true If the starting bytes are equal
     */
    public static boolean startsWith(byte[] source, byte[] match) {
        return startsWith(source, 0, match);
    }

    /**
     * Does this byte array begin with match array content?
     *
     * @param source Byte array to examine
     * @param offset An offset into the <code>source</code> array
     * @param match  Byte array to locate in <code>source</code>
     * @return true If the starting bytes are equal
     */
    public static boolean startsWith(byte[] source, int offset, byte[] match) {

        if (match.length > (source.length - offset)) {
            return false;
        }

        for (int i = 0; i < match.length; i++) {
            if (source[offset + i] != match[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Does the source array equal the match array?
     *
     * @param source Byte array to examine
     * @param match  Byte array to locate in <code>source</code>
     * @return true If the two arrays are equal
     */
    public static boolean equals(byte[] source, byte[] match) {

        if (match.length != source.length) {
            return false;
        }
        return startsWith(source, 0, match);
    }

    /**
     * Copies bytes from the source byte array to the destination array
     *
     * @param source      The source array
     * @param srcBegin    Index of the first source byte to copy
     * @param srcEnd      Index after the last source byte to copy
     * @param destination The destination array
     * @param dstBegin    The starting offset in the destination array
     */
    public static void getBytes(byte[] source, int srcBegin, int srcEnd, byte[] destination,
                                int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }

    /**
     * Return a new byte array containing a sub-portion of the source array
     *
     * @param srcBegin The beginning index (inclusive)
     * @param srcEnd   The ending index (exclusive)
     * @return The new, populated byte array
     */
    public static byte[] subbytes(byte[] source, int srcBegin, int srcEnd) {
        byte destination[];

        destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);

        return destination;
    }

    /**
     * Return a new byte array containing a sub-portion of the source array
     *
     * @param srcBegin The beginning index (inclusive)
     * @return The new, populated byte array
     */
    public static byte[] subbytes(byte[] source, int srcBegin) {
        return subbytes(source, srcBegin, source.length);
    }
}
