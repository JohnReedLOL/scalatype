package info.collaboration_station.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Contains array and byte array utilities for things like serialization,
 * deep equals, concatenation, and conversion to hexadeximal format.
 * @author john
 */
public final class ByteArrays {

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] concatByteArrays(byte[]... byteArrays) {

        int lengthSum = 0;
        for (int i = 0; i < byteArrays.length; ++i) {
            lengthSum += byteArrays[i].length;
        }
        byte[] toReturn = new byte[lengthSum];
        int runningTaly = 0;
        for (int i = 0; i < byteArrays.length; ++i) {
            System.arraycopy(byteArrays[i], 0, toReturn, runningTaly, byteArrays[i].length);
            runningTaly += byteArrays[i].length;
        }
        return toReturn;
    }

    public static long[] concatLongArrays(long[] a, long[] b) {
        int aLen = a.length;
        int bLen = b.length;
        long[] c = new long[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    public static long[] concatLongs(long... longsToConcat) {
        return longsToConcat; // Java VarArgs just turns it into an array.
    }

    public static <T> T[] concatGeneric(T[] a, T[] b) {
        int aLen = a.length;
        int bLen = b.length;
        @SuppressWarnings("unchecked")
        T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    /**
     * Makes a deep copy of a serializable object. The object being copied must
     * implement {@link java.io.Serializable}.
     * @return null if the things to clone is null or if the thing could not be
     * deep coped, non-null otherwise.
     */
    public static <Type> Type tryDeepCopy(Type toClone) {
        try {
            if (toClone == null) {
                return null;
            }
            Tester.check(toClone instanceof Serializable, "Only serializable objects can be deep copied.");
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(toClone);
            oos.flush();
            final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            final ObjectInputStream ois = new ObjectInputStream(bais);
            final Object toReturn = ois.readObject();
            // Tester.check(toReturn instanceof Type); // Doesn't work
            final Type toReturnCast = (Type) toReturn; // unsafe operation.
            return toReturnCast; 
        } catch(InvalidClassException ice) {
            Printer.printException("Something is wrong with the class", ice);
            return null;
        } catch (IOException ioe) {
            Printer.printException("Something is blocking this byte array read/write operation", ioe);
            return null;
        } catch (ClassNotFoundException e) {
            Tester.killApplication("You have to find this class because it was the same type going in as going out.");
            return null;
        } catch (Exception e) {
            Printer.printException(e);
            return null;
        }
    }

    public static boolean deepEquals(Object a, Object b) {
        if (!haveMatchingCanonicalClassNames(a, b)) {
            return false;
        }
        byte[] bytesA = nonPrimitiveArrayObjectToByteArray(a);
        byte[] bytesB = nonPrimitiveArrayObjectToByteArray(b);
        Tester.check(bytesA != null && bytesB != null, "Couldn't convert object to bytes.");
        if (bytesA.length != bytesB.length) {
            Printer.printGood("The lengths don't match up. Length1 = " + bytesA.length 
                    + ", Length2 = " + bytesB.length);
            return false;
        } else {
            for (int i = 0; i < bytesA.length; ++i) {
                if (bytesA[i] != bytesB[i]) {
                    Printer.printGood("The do match up, but byte: " + bytesB[i] + " is different.");
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean deepEquals(long[] a, long[] b) {
        if (!(a.length == b.length)) {
            return false;
        } else {
            for (int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean deepEquals(int[] a, int[] b) {
        if (!(a.length == b.length)) {
            return false;
        } else {
            for (int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean deepEquals(short[] a, short[] b) {
        if (!(a.length == b.length)) {
            return false;
        } else {
            for (int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean deepEquals(byte[] a, byte[] b) {
        if (!(a.length == b.length)) {
            return false;
        } else {
            for (int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean haveMatchingCanonicalClassNames(Object a, Object b) {
        final String objectAClassName = a.getClass().getCanonicalName();
        final String objectBClassName = b.getClass().getCanonicalName();
        if (objectAClassName == null || objectBClassName == null) {
            return false; // in case of anonymous classes
        } else {
            if (objectAClassName.equals(objectBClassName)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean deepEqualBytes(byte[] bytesA, byte[] bytesB) {
        if (bytesA.length != bytesB.length) {
            return false;
        } else {
            for (int i = 0; i < bytesA.length; ++i) {
                if (bytesA[i] != bytesB[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    public static byte[] longToByteArray(long someLong) {
        byte[] to_return = ByteBuffer.allocate(8).putLong(someLong).array();
        if (to_return.length != 8) {
            System.err.println("length is messed up");
            throw new RuntimeException("Impossible");
        }
        return to_return;
    }

    public static long byteArrayToLong(byte[] by) {
        if (by.length != 8) {
            throw new IllegalArgumentException("One long is eight bytes");
        }
        ByteBuffer bb = ByteBuffer.wrap(by);
        long l = bb.getLong();
        return l;
    }

    /**
     * Takes a byte array segment and makes 8 of its bytes into a long starting
     * from int start and stopping at int start+8 exclusive.
     */
    private static long byteArraySegmentToLong(byte[] by, int start) {
        if (by.length % 8 != 0) {
            throw new IllegalArgumentException("Each long is eight bytes");
        }
        if (start + 8 > by.length) {
            throw new IllegalArgumentException("Your start value is too high");
        }
        byte[] bytesToMakeIntoALong = Arrays.copyOfRange(by, start, start + 8);
        Tester.check(bytesToMakeIntoALong.length == 8, "Each long in is eight bytes");
        ByteBuffer bb = ByteBuffer.wrap(bytesToMakeIntoALong); // copyOfRange final parameter is exclusive
        long l = bb.getLong();
        return l;
    }

    public static long[] byteArrayToMultipleLongs(byte[] bytes, int numLongs) {
        if (bytes.length % 8 != 0) {
            throw new IllegalArgumentException("Each long is eight bytes");
        }

        long toReturn[] = new long[numLongs]; // 3 in the case of reliable header
        for (int index = 0; index < numLongs; ++index) {
            toReturn[index] = byteArraySegmentToLong(bytes, 8 * index);
            // each segment of the byte array gets turned into a long and inserted into toReturn.
        }
        return toReturn;
    }

    public static long[] EntireByteArrayToMultipleLongs(byte[] bytes) {
        try {
            Tester.check(bytes.length % 8 == 0, "Each long is 8 bytes, so the byte array must be a multiple of 8");
            return byteArrayToMultipleLongs(bytes, bytes.length / 8);
        } catch (IllegalArgumentException e) {
            Printer.printException(e);
            throw e;
        }
    }

    /**
     * DO NOT USE THIS METHOD TO CONVERT AN ARRAY OF INTS TO AN ARRAY OF BYTES
     * BECAUSE IT WILL SERIALIZE THE CLASS NAME AND INCLUDE IT IN THE BYTE
     * ARRAY.
     */
    public static byte[] nonPrimitiveArrayObjectToByteArray(final Object object) {
        Tester.check(!(object instanceof byte[]), "This method takes non-primitive-array type objects only");
        Tester.check(!(object instanceof short[]), "This method takes non-primitive-array type objects only");
        Tester.check(!(object instanceof int[]), "This method takes non-primitive-array type objects only");
        Tester.check(!(object instanceof long[]), "This method takes non-primitive-array type objects only");

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(2048);
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.flush();
            oos.writeObject(object);
            oos.flush();
            oos.close();
            // get the byte array of the object
            byte[] obj = baos.toByteArray();
            baos.flush();
            baos.close();
            return obj;
        } catch (Exception e) {
            Printer.printException(e);
            System.exit(-78);
            return null;
        }
    }

    /**
     * THROWS AN IO EXCEPTION [EOF EXCEPTION] IF THE BYTE ARRAY SO TOO SMALL TO
     * FIT THE OBJECT
     *
     * @param data
     * @return Null on failure, non-null on success
     */
    public static Object tryMakeByteArrayIntoObject(byte[] data) {
        Tester.check(data != null, "Null data");
        Object to_return = null;
        try(ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data))) {
            try {
                to_return = ois.readObject();
            } catch (ClassNotFoundException cnfe) {
                Printer.printException(cnfe);
            } catch(StreamCorruptedException sce) {
                Printer.printException(sce);
            }
        } catch (IOException ioe) {
            Printer.printException(ioe);
        } finally {
            return to_return;
        }
    }
}
