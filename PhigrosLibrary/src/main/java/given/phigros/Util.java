package given.phigros;

public class Util {
    static boolean getBit(byte data, int index) {return (data & 1 << index) != 0;}
    static byte modifyBit(byte data, int index, boolean b) {
        byte result = (byte)(1 << index);
        if (b) {
            result |= data;
        } else {
            result &= (~result);
        }
        return result;
    }
    static byte[] modifyBytes(byte[] src, int offset, int length, byte[] dst) {
        byte[] data = new byte[src.length + dst.length - length];
        System.arraycopy(src, 0, data, 0, offset);
        System.arraycopy(dst, 0, data, offset, dst.length);
        System.arraycopy(src, offset + length, data, offset + dst.length, src.length - offset - length);
        return data;
    }
    static int readVarShort(byte[] data, int offset) {
        if(Util.getBit(data[offset],7)) {
            return data[offset + 1] * 128 + data[offset] - 128;
        } else {
            return data[offset];
        }
    }
    static byte[] getVarShort(int num) {
        if (num < 128)
            return new byte[] {(byte) num};
        return new byte[] {(byte) (num % 128 + 128), (byte) (num / 128)};
    }
    public static void deleteFile(String session,String objectId) throws Exception {
        SaveManager.deleteFile(session,objectId);
    }
    public static void delete(String session,String objectId) throws Exception {
        SaveManager.delete(session,objectId);
    }
}
