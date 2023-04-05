package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GameSettings {
    private final ByteBuffer reader;
    GameSettings(byte[] data) {
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
    String getPhone() {
        reader.position(1);
        int length = reader.get();
        byte[] data = new byte[length];
        reader.get(data);
        return new String(data);
    }
    float getBeiJingLiangDu() {
        return getItem(0);
    }
    float getYinYueYinLiang() {
        return getItem(1);
    }
    float getJieMianYinXiaoYinLiang() {
        return getItem(2);
    }
    float getDaJiYinXiaoYinLiang() {
        return getItem(3);
    }
    float getPuMianYanChi() {
        return getItem(4);
    }
    float getAnJianSuoFang() {
        return getItem(5);
    }
    private float getItem(int index) {
        reader.position(1);
        reader.position(reader.get() + 2);
        for (int i = 0; i < 6; i++) {
            if (i == index) {
                return reader.getFloat();
            }
            reader.position(reader.position() + 4);
        }
        throw new RuntimeException("index错误");
    }
}
