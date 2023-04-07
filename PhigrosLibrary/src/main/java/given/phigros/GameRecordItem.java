package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class GameRecordItem implements Iterable<SongLevel> {
    byte[] data;
    GameRecordItem(byte[] data) {
        this.data = data;
    }
    public String getId() {
        return new String(data, 1, data[0] - 2);
    }
    public void modifySong(int level,int score,float acc,boolean fc) {
        int position = data[0] + 2;
        byte length = data[position];
        if (!Util.getBit(length, level))
            throw new RuntimeException("未游玩此曲目的此难度。");
        position++;
        data[position] = Util.modifyBit(data[position], level, fc);
        position++;
        for (int i = 0; i < 4; i++) {
            if (Util.getBit(length, i)) {
                if (i == level) {
                    ByteBuffer writer = ByteBuffer.allocate(8);
                    writer.order(ByteOrder.LITTLE_ENDIAN);
                    writer.putInt(score);
                    writer.putFloat(acc);
                    writer.position(0);
                    writer.get(data, position, 8);
                    break;
                }
                position += 8;
            }
        }

    }
    @Override
    public Iterator<SongLevel> iterator() {
        return new GameRecordItemIterator();
    }
    private class GameRecordItemIterator implements Iterator<SongLevel> {
        private final String id;
        private final float[] difficulty;
        private final ByteBuffer reader;
        private final byte length;
        private final byte fc;
        int level = -1;
        GameRecordItemIterator() {
            id = GameRecordItem.this.getId();
            length = data[data[0] + 2];
            fc = data[data[0] + 3];
            reader = ByteBuffer.wrap(data,data[0] + 4, data.length - data[0] - 4);
            reader.order(ByteOrder.LITTLE_ENDIAN);
            difficulty = PhigrosUser.getInfo(id).level;
        }
        @Override
        public boolean hasNext() {
            while (level != 3) {
                if (Util.getBit(length, ++level)) {
                    return true;
                }
            }
            return false;
        }
        @Override
        public SongLevel next() {
            SongLevel songLevel = new SongLevel();
            songLevel.id = id;
            songLevel.level = level;
            songLevel.difficulty = difficulty[level];
            songLevel.fc = Util.getBit(fc, level);
            songLevel.score = reader.getInt();
            songLevel.acc = reader.getFloat();
            if (songLevel.acc < 70f)
                return songLevel;
            else if (songLevel.score == 1000000)
                songLevel.rks = songLevel.difficulty;
            else
                songLevel.rks = (float) Math.pow((songLevel.acc - 55) / 45, 2) * songLevel.difficulty;
            return songLevel;
        }
    }
}
