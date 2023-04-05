package given.phigros;

public class GameUser {
    private final byte[] data;
    GameUser(byte[] data) {
        this.data = data;
    }
    String getIntroduction() {
        return getItem(0);
    }
    String getAvater() {
        return getItem(1);
    }
    String getIllustration() {
        return getItem(2);
    }
    private String getItem(int index) {
        int num = 1;
        for (int i = 0; i < index; i++) {
            num += data[num] + 1;
        }
        return new String(data, num + 1, data[num]);
    }
}
