package given.phigros;

class GameKeyItem {
    byte[] data;

    GameKeyItem(byte[] data) {
        this.data = data;
    }
    public String getId() {
        return new String(data, 1, data[0]);
    }
    public boolean getReadCollection() {
        return getBoolKey(0);
    }
    public void setReadCollection(boolean b) {
        setBoolKey(0, b);
    }
    public boolean getSingleUnlock() {
        return getBoolKey(1);
    }
    public void setSingleUnlock(boolean b) {
        setBoolKey(1, b);
    }
    public byte getCollection() {
        return getKey(2);
    }
    public void setCollection(byte num) {
        setKey(2, (byte) num);
    }
    public boolean getIllustration() {
        return getBoolKey(3);
    }
    public void setIllustration(boolean b) {
        setBoolKey(3, b);
    }
    public boolean getAvater() {
        return getBoolKey(4);
    }
    public void setAvater(boolean b) {
        setBoolKey(4, b);
    }
    private boolean getBoolKey(int index) {
        switch (getKey(index)) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                throw new RuntimeException("存档GameKey部分有误");
        }
    }
    private byte getKey(int index) {
        if (index < 0 || index >= 5)
            throw new RuntimeException("GameKeyItem的setKey参数错误。");
        int num = data[0] + 2;
        byte key = data[num];
        if (Util.getBit(key,index))
            return 0;
        num++;
        for (int i = 0; i < 5; i++) {
            if (!Util.getBit(key,i))
                continue;
            if (i == index)
                return data[num];
            num++;
        }
        throw new RuntimeException("存档GameKey部分有误");
    }
    private void setBoolKey(int index, boolean b) {
        setKey(index, (byte) (b ? 1: 0));
    }
    private void setKey(int index, byte value) {
        if (index < 0 || index >= 5)
            throw new RuntimeException("GameKeyItem的setKey参数错误。");
        int num = data[0] + 2;
        byte key = data[num];
        if (Util.getBit(key, index)) {
            num++;
            for (int i = 0; i < 5; i++) {
                if (!Util.getBit(key, i))
                    continue;
                if (i == index) {
                    data[num] = (byte) value;
                    return;
                }
                num++;
            }
        } else {
            data[num - 1]++;
            key = Util.modifyBit(key, index, true);
            data[num] = key;
            num++;
            byte[] result = new byte[data.length + 1];
            System.arraycopy(data, 0, result, 0, num);
            byte b = 0;
            for (int i = 0; i < 5; i++) {
                if (!Util.getBit(key, i))
                    continue;
                if (i == index) {
                    result[num] = (byte) value;
                    b = 1;
                } else
                    result[num + b] = data[num];
                num++;
            }
            data = result;
        }
    }
}