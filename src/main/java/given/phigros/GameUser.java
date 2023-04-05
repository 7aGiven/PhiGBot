package given.phigros;

public class GameUser {
    private byte[] data;
    GameUser(byte[] data) {
        this.data = data;
    }
    String getIntroduction() {
        return getItem(0);
    }
    void setIntroduction(String introduction) {
        setItem(0, introduction);
    }
    String getAvater() {
        return getItem(1);
    }
    void setAvater(String avater) {
        setItem(1, avater);
    }
    String getIllustration() {
        return getItem(2);
    }
    void setIllustration(String illustration) {
        setItem(2, illustration);
    }
    private String getItem(int index) {
        final var num = getItemIndex(index);
        return new String(data, num + 1, data[num]);
    }
    private int getItemIndex(int index) {
        int num = 1;
        for (int i = 0; i < index; i++) {
            num += data[num] + 1;
        }
        return num;
    }
    private void setItem(int index, String str) {
        var num = data[getItemIndex(index)];
        final var bytes = str.getBytes();
        byte[] result = Util.modifyBytes(data, num + 1, data[num], bytes);
        result[num] = (byte) bytes.length;
        data = result;
    }
}
