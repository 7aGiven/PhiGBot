package given.phigros;

import java.nio.ByteBuffer;

public class Util {
    static boolean getBit(int data, int index) {return (data & 1 << index) != 0;}
    static int readVarShort(byte[] data) {
        if(Util.getBit(data[0],7))
            return 2;
        return 1;
    }
    static byte[] readVarShort(short num) {
        if (num < 128)
            return new byte[] {(byte) num};
        return new byte[] {(byte) (num % 128 + 128), (byte) (num / 128)};
    }
    static void writeVarShort(ByteBuffer buffer, short num) {
        if (num < 128) {
            buffer.put((byte) num);
        } else {
            buffer.put((byte) (num % 128 + 128));
            buffer.put((byte) (num / 128));
        }
    }
    public static void deleteFile(String session,String objectId) throws Exception {
        SaveManager.deleteFile(session,objectId);
    }
    public static void delete(String session,String objectId) throws Exception {
        SaveManager.delete(session,objectId);
    }
}
