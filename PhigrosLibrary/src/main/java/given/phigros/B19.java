package given.phigros;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class B19 implements Iterable<String> {
    private final byte[] data;
    final ByteBuffer reader;
    private int dataPosition;
    private byte fc;

    B19(byte[] data) {
        this.data = data;
        reader = ByteBuffer.wrap(data);
        reader.order(ByteOrder.LITTLE_ENDIAN);
    }

    SongLevel[] getB19() {
        int minIndex = 1;
        final var b19 = new SongLevel[20];
        Arrays.fill(b19,new SongLevel());
        for (String id:this) {
            final float[] levels = PhigrosUser.getInfo(id).level;
            int num = levels.length - 1;
            for (; num >= 0; num--) {
                if (levels[num] <= b19[minIndex].rks && levels[num] <= b19[0].rks)
                    break;
            }
            if (++num == levels.length)
                continue;
            int mark = reader.position();
            reader.position(dataPosition);
            byte length = reader.get();
            fc = reader.get();
            go(length, num);
            for (; num < levels.length; num++) {
                if (!Util.getBit(length, num))
                    continue;
                final var songLevel = new SongLevel();
                songLevel.score = reader.getInt();
                songLevel.acc = reader.getFloat();
                if (songLevel.acc < 70f)
                    continue;
                songLevel.level = num;
                songLevel.difficulty = levels[num];
                if (songLevel.score == 1000000) {
                    songLevel.rks = levels[num];
                    if (levels[num] > b19[0].rks) {
                        songLevel.id = id;
                        songLevel.fc = getFC(num);
                        b19[0] = songLevel;
                    }
                } else {
                    songLevel.rks = (songLevel.acc - 55f) / 45f;
                    songLevel.rks *= songLevel.rks * songLevel.difficulty;
                }
                if (songLevel.rks < b19[minIndex].rks)
                    continue;
                songLevel.id = id;
                songLevel.fc = getFC(num);
                b19[minIndex] = songLevel;
                minIndex = min(b19);
            }
            reader.position(mark);
        }
        Arrays.sort(b19);
        return b19;
    }
    SongExpect[] getExpect() {
        float minRks = getMinRks();
        final var list = new ArrayList<SongExpect>();
        for (String id:this) {
            final float[] levels = PhigrosUser.getInfo(id).level;
            int num = levels.length - 1;
            for (; num >= 0; num--) {
                if (levels[num] <= minRks)
                    break;
            }
            if (++num == levels.length)
                continue;
            int mark = reader.position();
            reader.position(dataPosition);
            byte length = reader.get();
            fc = reader.get();
            go(length, num);
            for (; num < levels.length; num++) {
                if (!Util.getBit(length, num))
                    continue;
                int score = reader.getInt();
                float acc = reader.getFloat();
                if (acc < 70f || score == 1000000)
                    continue;
                var rks = (acc - 55f) / 45f;
                rks *= rks * levels[num];
                if (rks >= minRks)
                    continue;
                final var expect = (float) Math.sqrt(minRks / levels[num]) * 45f + 55f;
                list.add(new SongExpect(id, num, acc, expect));
            }
            reader.position(mark);
        }
        final var array = list.toArray(SongExpect[]::new);
        Arrays.sort(array);
        return array;
    }

    private float getMinRks() {
        int minIndex = 1;
        final var b19 = new SongLevel[20];
        Arrays.fill(b19,new SongLevel());
        for (String id:this) {
            final float[] levels = PhigrosUser.getInfo(id).level;
            int num = levels.length - 1;
            for (; num >= 0; num--) {
                if (levels[num] <= b19[minIndex].rks)
                    break;
            }
            if (++num == levels.length)
                continue;
            int mark = reader.position();
            reader.position(dataPosition);
            byte length = reader.get();
            fc = reader.get();
            go(length, num);
            for (; num < levels.length; num++) {
                if (!Util.getBit(length, num))
                    continue;
                final var songLevel = new SongLevel();
                songLevel.score = reader.getInt();
                songLevel.acc = reader.getFloat();
                if (songLevel.acc < 70f)
                    continue;
                if (songLevel.score == 1000000)
                    songLevel.rks = levels[num];
                else {
                    songLevel.rks = (songLevel.acc - 55f) / 45f;
                    songLevel.rks *= songLevel.rks * levels[num];
                }
                if (songLevel.rks < b19[minIndex].rks)
                    continue;
                songLevel.id = id;
                songLevel.level = num;
                songLevel.fc = getFC(num);
                songLevel.difficulty = levels[num];
                b19[minIndex] = songLevel;
                minIndex = min(b19);
            }
            reader.position(mark);
        }
        return b19[minIndex].rks;
    }

    private int min(SongLevel[] array) {
        int index = -1;
        double min = 17;
        for (int i = 1; i < 20; i++) {
            if (array[i].id == null) return i;
            if (array[i].rks < min) {
                index = i;
                min = array[i].rks;
            }
        }
        return index;
    }

    private void go(byte length, int index) {
        for (int i = 0; i < index; i++) {
            if (Util.getBit(length, i))
                reader.position(reader.position() + 8);
        }
    }

    boolean getFC(int index) {
        return Util.getBit(fc, index);
    }


    @Override
    public Iterator<String> iterator() {
        return new B19Iterator();
    }

    private class B19Iterator implements Iterator<String> {
        B19Iterator() {
            reader.position(Util.getBit(data[0], 7) ? 2 : 1);
        }

        @Override
        public boolean hasNext() {
            return reader.position() != data.length;
        }

        @Override
        public String next() {
            var length = reader.get();
            String id = new String(data, reader.position(), length - 2);
            reader.position(reader.position() + length);
            length = reader.get();
            dataPosition = reader.position();
            reader.position(reader.position() + length);
            return id;
        }
    }
}