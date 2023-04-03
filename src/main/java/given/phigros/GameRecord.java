package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

class GameRecord implements Iterable<String> {
    private final ByteBuffer reader;
    private int dataPosition;
    private final int globalLength;

    GameRecord(byte[] data) {
        globalLength = data.length;
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
    public SongLevel[] getSong() {
        reader.position(dataPosition);
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
    public byte[] modifySong(int level,int score,float acc,boolean b) {
        reader.position(dataPosition);
        byte length = reader.get();
        byte fc = reader.get();
        for (int i = 0; i < 4; i++) {
            if (Util.getBit(length,i)) {
                if (i == level) {
                    reader.putInt(score);
                    reader.putFloat(acc);
                    reader.position(dataPosition +1);
                    reader.put(modifyBit(fc,i,b));
                    break;
                }
                reader.position(reader.position()+8);
            }
        }
        return reader.array();
    }
    private byte modifyBit(byte data, int index, boolean b) {
        byte result = (byte)(1 << index);
        if (b) {
            result |= data;
        } else {
            result &= (~result);
        }
        return result;
    }
    @Override
    public Iterator<String> iterator(){
        return new ScoreIterator();
    }
    class ScoreIterator implements Iterator<String> {
        private int position;
        ScoreIterator() {
            position = 1;
            if (Util.getBit(reader.get(),7)) position = 2;
        }
        @Override
        public boolean hasNext() {
            if (position == globalLength) {
                return false;
            } else {
                reader.position(position);
                return true;
            }
        }
        @Override
        public String next() {
            int length = reader.get() - 2;
            byte[] buf = new byte[length];
            reader.get(buf,0,length);
            String id = new String(buf);
            reader.position(reader.position()+2);
            length = reader.get();
            dataPosition = reader.position();
            position = dataPosition + length;
            return id;
        }
    }
}