package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GameSettings {
    private final ByteBuffer reader;
    GameSettings(byte[] data) {
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
    public String getDevice() {
        reader.position(1);
        int length = reader.get();
        byte[] data = new byte[length];
        reader.get(data);
        return new String(data);
    }
    public float 背景亮度() {
        return getItem(0);
    }
    public float 音乐音量() {
        return getItem(1);
    }
    public float 界面音效音量() {
        return getItem(2);
    }
    public float 打击音效音量() {
        return getItem(3);
    }
    public float 铺面延迟() {
        return getItem(4);
    }
    public float 按键缩放() {
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
