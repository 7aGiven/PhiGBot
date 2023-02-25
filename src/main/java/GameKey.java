import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class GameKey implements Iterable<String>{
    ByteBuffer reader;
    int dataPosition;
    int globalLength;
    GameKey(byte[] data) throws Exception {
        globalLength = data.length;
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }
    public byte[] getKey() throws Exception {
        reader.position(dataPosition);
        int key = reader.get();
        byte[] keys = new byte[5];
        for (int i = 0; i < 5; i++) {
            if (Util.getBit(key,i)) {
                keys[i] = reader.get();
            }
        }
        return keys;
    }
    public byte[] modifyCollection() throws Exception {
        reader.position(dataPosition);
        int key = reader.get();
        if (Util.getBit(key,0)) {
            byte b = (byte) (reader.get()+1);
            reader.position(dataPosition+1);
            reader.put(b);
        }
        return reader.array();
    }
    public byte[] addCollection(byte[] name) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.length);
        byteArrayOutputStream.writeBytes(name);
        byteArrayOutputStream.writeBytes(new byte[]{2,1,1});
        return addData(byteArrayOutputStream.toByteArray());
    }
    public byte[] modifyAvater() throws Exception {
        byte[] data = new byte[globalLength+1];
        reader.position(0);
        reader.get(data,0,dataPosition-1);
        data[dataPosition-1] = (byte) (reader.get()+1);
        int key = reader.get();
        data[dataPosition] = (byte) (key+16);
        int index = 1;
        for (int i = 0; i < 4; i++) {
            if (Util.getBit(key,i)) {
                data[dataPosition+index] = reader.get();
                index++;
            }
        }
        data[dataPosition+index] = 1;
        reader.get(data,dataPosition+index+1,globalLength-dataPosition-index);
        return data;
    }
    public byte[] addAvater(byte[] name) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(name.length);
        byteArrayOutputStream.writeBytes(name);
        byteArrayOutputStream.writeBytes(new byte[]{2,16,1});
        return addData(byteArrayOutputStream.toByteArray());
    }
    private byte[] addData(byte[] d) throws Exception {
        reader.position(0);
        byte[] data = new byte[2];
        data[0] = reader.get();
        int oldLength = Util.getBit(data[0],7)?2:1;
        int length = oldLength;
        if (oldLength == 2) data[1] = reader.get();
        if (oldLength == 1 && data[0] == Byte.MAX_VALUE) {
            length++;
            data[1]++;
        } else if (oldLength == 2 && data[0] == -1) {
            data[1]++;
        }
        data[0]++;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(data,0,length);
        byteArrayOutputStream.writeBytes(d);
        data = new byte[globalLength-oldLength];
        reader.get(data,0,globalLength-oldLength);
        byteArrayOutputStream.writeBytes(data);
        return byteArrayOutputStream.toByteArray();
    }
    @Override
    public Iterator<String> iterator() {return new GameKeyIterator();}
    private class GameKeyIterator implements Iterator<String>{
        private int position;
        GameKeyIterator() {
            int a = reader.get();
            position = 1;
            if (Util.getBit(a,7)) {
                position = 2;
            }
        }
        @Override
        public boolean hasNext() {
            if (position == globalLength - 1) {
                return false;
            } else {
                reader.position(position);
                return true;
            }
        }

        @Override
        public String next() {
            int length = reader.get();
            byte[] buf = new byte[length];
            reader.get(buf,0, length);
            String id = new String(buf);
            System.out.println(id);
            length = reader.get();
            dataPosition = reader.position();
            position = dataPosition + length;
            return id;
        }
    }
}