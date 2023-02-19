import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;

public class GameKey implements Iterable<String>{
    ByteBuffer reader;
    int dataPosition;
    int globalLength;
    GameKey(byte[] data) {
        globalLength = data.length;
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte[] getKey() {
        reader.position(dataPosition);
        int key = reader.get();
        byte[] keys = new byte[5];
        for (int i = 0; i < 5; i++) {
            if (getBit(key,i)) {
                keys[i] = reader.get();
            }
        }
        return keys;
    }
    public byte[] modifyCollection() {
        reader.position(dataPosition);
        int key = reader.get();
        if (getBit(key,0)) {
            reader.put((byte) (reader.get()+1));
        }
        return reader.array();
    }
    public byte[] addCollection(byte[] name) throws IOException {
        byte[] data = new byte[2];
        reader.position(0);
        data[0] = reader.get();
        int oldLength = 2;
        int length = 2;
        if (getBit(data[0],7)) {
            data[1] = reader.get();
            if (data[0] == -1) {
                data[0] = -128;
                data[1] += 1;
            } else {
                data[0] += 1;
            }
        } else {
            oldLength = 1;
            if (data[0] == 127) {
                data[0] = -128;
                data[1] = 1;
            } else {
                data[0] += 1;
                length = 1;
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(globalLength+name.length+4-oldLength+length);
        byteArrayOutputStream.write(data,0,length);
        byteArrayOutputStream.write(name.length);
        byteArrayOutputStream.write(name);
        data = new byte[] {2,1,1};
        byteArrayOutputStream.write(data);
        data = new byte[globalLength-length];
        reader.get(data,0,globalLength-oldLength);
        byteArrayOutputStream.write(data);
        return byteArrayOutputStream.toByteArray();
    }
    public byte[] modifyAvater() {
        byte[] data = new byte[globalLength+1];
        reader.position(0);
        reader.get(data,0,dataPosition-1);
        data[dataPosition-1] = (byte) (reader.get()+1);
        int key = reader.get();
        data[dataPosition] = (byte) (key+16);
        int index = 1;
        for (int i = 0; i < 4; i++) {
            if (getBit(key,i)) {
                data[dataPosition+index] = reader.get();
                index++;
            }
        }
        data[dataPosition+index] = 1;
        reader.get(data,dataPosition+index+1,globalLength-dataPosition-index);
        return data;
    }
    public byte[] addAvater(byte[] name) throws IOException {
        byte[] data = new byte[2];
        reader.position(0);
        data[0] = reader.get();
        int oldLength = 2;
        int length = 2;
        if (getBit(data[0],7)) {
            data[1] = reader.get();
            if (data[0] == -1) {
                data[0] = -128;
                data[1] += 1;
            } else {
                data[0] += 1;
            }
        } else {
            oldLength = 1;
            if (data[0] == 127) {
                data[0] = -128;
                data[1] = 1;
            } else {
                data[0] += 1;
                length = 1;
            }
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(globalLength+name.length+4-oldLength+length);
        byteArrayOutputStream.write(data,0,length);
        byteArrayOutputStream.write(name.length);
        byteArrayOutputStream.write(name);
        data = new byte[] {2,16,1};
        byteArrayOutputStream.write(data);
        data = new byte[globalLength-length];
        reader.get(data,0,globalLength-oldLength);
        byteArrayOutputStream.write(data);
        return byteArrayOutputStream.toByteArray();
    }

    private boolean getBit(int data, int index) {
        return (data & (1 << index)) != 0;
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return new GameKeyIterator();
    }
    private class GameKeyIterator implements Iterator<String>{
        int position = 2;
        int length;
        byte[] buf;
        String id;
        GameKeyIterator() {
            int a = reader.get();
            position = 1;
            if (getBit(a,7)) {
                position = 2;
            }
        }
        @Override
        public boolean hasNext() {
            if (position == globalLength - 1) {
                return false;
            } else {
                reader.position(position);
                length = reader.get();
                return true;
            }
        }

        @Override
        public String next() {
            buf = new byte[length];
            reader.get(buf,0,length);
            id = new String(buf);
            System.out.println(id);
            length = reader.get();
            dataPosition = reader.position();
            position = dataPosition + length;
            return id;
        }
    }
}