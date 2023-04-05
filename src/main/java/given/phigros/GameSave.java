package given.phigros;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

class GameSave implements Iterable<String> {
    protected int version;
    private final int end;
    protected final byte[][] array;
    protected final ArrayList<byte[]> list = new ArrayList<>();
    protected int position;
    protected GameSave(byte[] data, int end) {
        this.end = end;
        final var reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
        int position = Util.getBit(data[0],7) ? 2 : 1;
        reader.position(position);
        final ArrayList<byte[]> list = new ArrayList<>();
        byte length;
        while (position < data.length - 1){
            length = reader.get();
            reader.position(position + length + 1);
            length += reader.get() + 2;
            byte[] tmp = new byte[length];
            reader.position(position);
            reader.get(tmp);
            list.add(tmp);
            position = reader.position();
        }
        array = list.toArray(byte[][]::new);
    }
    byte[] getData() throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            outputStream.writeBytes(Util.getVarShort(array.length + list.size()));
            for (int position = 0; position != array.length; position++)
                outputStream.writeBytes(array[position]);
            for (int position = 0; position != list.size(); position++)
                outputStream.writeBytes(list.get(position));
            if (version != 0)
                outputStream.writeBytes(Util.getVarShort(version));
            return outputStream.toByteArray();
        }
    }
    protected int itemIndex(byte[] data) {
        return data[0] + 2;
    }
    @Override
    public Iterator<String> iterator(){
        return new GameRecordIterator();
    }
    class GameRecordIterator implements Iterator<String> {
        @Override
        public boolean hasNext() {
            position++;
            return position != array.length;
        }
        @Override
        public String next() {
            return new String(array[position],1,array[position][0] - end);
        }
    }
}
