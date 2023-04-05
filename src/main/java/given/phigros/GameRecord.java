package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class GameRecord extends GameSave implements Iterable<String> {

    GameRecord(byte[] data) {
        super(data, 2);
    }
    SongLevel[] getSong() {
        int index = itemIndex(array[position]);
        final var reader = ByteBuffer.wrap(array[position],index,array[position].length - index);
        reader.order(ByteOrder.LITTLE_ENDIAN);
        byte length = reader.get();
        byte fc = reader.get();
        SongLevel[] songLevels = new SongLevel[4];
        for (int i = 0; i < 4; i++) {
            if (Util.getBit(length,i)) {
                songLevels[i] = new SongLevel();
                songLevels[i].fc = Util.getBit(fc,i);
                songLevels[i].score = reader.getInt();
                songLevels[i].acc = reader.getFloat();
             }
        }
        return songLevels;
    }
    void modifySong(int level,int score,float acc,boolean fc) {
        final var index = array[position][0] +  2;
        final var buffer = ByteBuffer.wrap(array[position],index,array[position].length - index);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        byte length = buffer.get();
        byte fcs = buffer.get();
        var b = false;
        for (int i = 0; i < 4; i++) {
            if (Util.getBit(length,i)) {
                if (i == level) {
                    buffer.putInt(score);
                    buffer.putFloat(acc);
                    buffer.position(0);
                    buffer.put(Util.modifyBit(fcs,i,fc));
                    b = true;
                    break;
                }
                buffer.position(buffer.position()+8);
            }
        }
        if (!b)
            throw new RuntimeException("未打过这张谱子。");
        buffer.position(0);
        buffer.put(array[position],index,array[position].length - index);
    }
}