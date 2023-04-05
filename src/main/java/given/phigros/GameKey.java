package given.phigros;

class GameKey extends GameSave implements Iterable<String>{
    private final int version;

    public GameKey(byte[] data) {
        super(data, 0);
        super.version = data[data.length - 1];
        version = super.version;
    }
    public byte getKey(int index) {
        int num = itemIndex(array[position]);
        byte key = array[position][num];
        if (Util.getBit(key,index))
            return 0;
        num++;
        for (int i = 0; i < 5; i++) {
            if (!Util.getBit(key,i))
                continue;
            if (i == index) {
                key = array[position][num];
                break;
            }
            num++;
        }
        return key;
    }
    void setKey(int index, int value) {
        int num = itemIndex(array[position]);
        byte key = array[position][num];
        num++;
        if (Util.getBit(key, index)) {
            for (int i = 0; i < 5; i++) {
                if (!Util.getBit(key, i))
                    continue;
                if (i == index) {
                    array[position][num] = (byte) value;
                }
                num++;
            }
        } else {
            array[position][num - 1]++;
            key = Util.modifyBit(key, index, true);
            array[position][num] = key;
            byte[] data = new byte[array[position].length + 1];
            System.arraycopy(array[position], 0, data, 0, num);
            byte b = 0;
            for (int i = 0; i < 5; i++) {
                if (!Util.getBit(key, i))
                    continue;
                if (i == index) {
                    data[num] = (byte) value;
                    b = 1;
                } else
                    data[num + b] = array[position][num];
                num++;
            }
            array[position] = data;
        }
    }
    void addKey(String key, byte[] data) {
        var byteString = key.getBytes();
        var result = new byte[byteString.length + data.length + 2];
        result[0] = (byte) byteString.length;
        System.arraycopy(byteString,0,result,1,byteString.length);
        result[byteString.length + 1] = (byte) data.length;
        System.arraycopy(data,0, result,byteString.length + 2, data.length);
        list.add(result);
    }
}