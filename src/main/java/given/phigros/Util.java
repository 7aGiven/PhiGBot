package given.phigros;

public class Util {
    static boolean getBit(int data, int index) {return (data & 1 << index) != 0;}
    public static void deleteFile(String session,String objectId) throws Exception {
        SaveManager.deleteFile(session,objectId);
    }
    public static void delete(String session,String objectId) throws Exception {
        SaveManager.delete(session,objectId);
    }
}
