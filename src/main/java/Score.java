import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class Score implements Iterable<String> {
    private final ByteBuffer reader;
    private int globalPosition;
    private final int globalLength;
    private String id;
    Score(byte[] data) {
        globalLength = data.length;
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
    @Override
    public Iterator iterator(){
        return new ScoreIterator();
    }
    public Song getSong() {
        reader.position(globalPosition);
        byte length = reader.get();
        byte fc = reader.get();
        Song song = new Song();
        song.id = id;
        for (int i = 0; i < 4; i++) {
            if (getBit(length,i)) {
                song.get(i).fc = getBit(fc,i);
                song.get(i).score = reader.getInt();
                song.get(i).acc = reader.getFloat();
             }
        }
        return song;
    }
    public void modifySong(int level,int score,float acc,boolean c) {
        reader.position(globalPosition);
        byte length = reader.get();
        byte fc = reader.get();
        for (int i = 0; i < 4; i++) {
            if (getBit(length,i)) {
                if (i == level) {
                    reader.putInt(score);
                    reader.putFloat(acc);
                    reader.position(globalPosition+1);
                    reader.put(modifyBit(fc,i,c));
                    break;
                }
                reader.position(reader.position()+8);
            }
        }
    }
    public byte[] getData() {
        reader.position(0);
        byte[] data = new byte[globalLength];
        reader.get(data,0,globalLength);
        return data;
    }
    class ScoreIterator implements Iterator {
        int position;
        int length;
        ScoreIterator() {
            int a = reader.get();
            position = 1;
            if (getBit(a,7)) {
                position = 2;
            }
        }
        @Override
        public boolean hasNext() {
            try {
                reader.position(position);
                length = reader.get() - 2;
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public String next() {
            byte[] buf = new byte[length];
            reader.get(buf,0,length);
            id = new String(buf);
            reader.position(reader.position()+2);
            length = reader.get();
            globalPosition = reader.position();
            position = reader.position() + length;
            return id;
        }
}
    private static boolean getBit(int data, int index) {
        return (data & (1 << index)) != 0;
    }
    private static byte modifyBit(byte data, int index, boolean b) {
        byte result = (byte)(1 << index);
        if (b) {
            result |= data;
        } else {
            result &= (~result);
        }
        return result;
    }
}