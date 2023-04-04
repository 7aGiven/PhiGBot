package given.phigros;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

class GameRecord implements Iterable<String> {
    private final byte[][] songs;
    private int position;
    public final short length;

    GameRecord(byte[] data) {
        final var reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
        int position = Util.readVarShort(data);
        reader.position(position);
        final ArrayList<byte[]> list = new ArrayList<>();
        byte length;
        while (position != data.length){
            length = reader.get();
            reader.position(position + length + 1);
            length += reader.get() + 2;
            byte[] tmp = new byte[length];
            reader.position(position);
            reader.get(tmp);
            list.add(tmp);
            position = reader.position();
        }
        songs = list.toArray(byte[][]::new);
        this.length = (short) songs.length;
    }
    SongLevel[] getSong() {
        int index = songs[position][0] +  2;
        final var reader = ByteBuffer.wrap(songs[position],index,songs[position].length - index);
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
        final var index = songs[position][0] +  2;
        final var buffer = ByteBuffer.wrap(songs[position],index,songs[position].length - index);
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
                    buffer.put(modifyBit(fcs,i,fc));
                    b = true;
                    break;
                }
                buffer.position(buffer.position()+8);
            }
        }
        if (!b)
            throw new RuntimeException("未打过这张谱子。");
        buffer.position(0);
        buffer.put(songs[position],index,songs[position].length - index);
    }
    byte[] getData() throws IOException {
        try (final var outputStream = new ByteArrayOutputStream()) {
            outputStream.writeBytes(Util.readVarShort(length));
            int position = 0;
            while (position != songs.length) {
                outputStream.writeBytes(songs[position]);
                position++;
            }
            return outputStream.toByteArray();
        }
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
        @Override
        public boolean hasNext() {
            position++;
            return position != length;
        }
        @Override
        public String next() {
            return new String(songs[position],1,songs[position][0] - 2);
        }
    }
}