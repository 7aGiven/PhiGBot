package given.phigros;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class GameRecord implements Iterable<GameRecordItem> {
    private final GameRecordItem[] array;

    GameRecord(byte[] data) {
        final ArrayList<GameRecordItem> list = new ArrayList<>();
        int position = Util.getBit(data[0],7) ? 2 : 1;
        int start;
        while (position != data.length){
            start = position;
            position += data[position] + 1;
            position += data[position] + 1;
            byte[] tmp = new byte[position - start];
            System.arraycopy(data, start, tmp, 0, position - start);
            list.add(new GameRecordItem(tmp));
        }
        array = list.toArray(GameRecordItem[]::new);
    }
    byte[] getData() throws IOException {
        try (var outputStream = new ByteArrayOutputStream()) {
            outputStream.writeBytes(Util.getVarShort(array.length));
            for (int position = 0; position != array.length; position++)
                outputStream.writeBytes(array[position].data);
            return outputStream.toByteArray();
        }
    }
    @Override
    public Iterator<GameRecordItem> iterator() {
        return new ArrayIterator<>(array);
    }
    public SongExpect[] getExpect() {
        final var min = getB19()[19].rks;
        final ArrayList<SongExpect> arrayList = new ArrayList<>();
        for (GameRecordItem item:this) {
            for (SongLevel songLevel:item) {
                if (songLevel.difficulty < min || songLevel.rks > min)
                    continue;
                final var expect = (float) Math.sqrt(min/songLevel.difficulty)*45+55;
                arrayList.add(new SongExpect(PhigrosUser.info.get(songLevel.id).name,songLevel.level,songLevel.acc,expect));
            }
        }
        final var array = arrayList.toArray(SongExpect[]::new);
        Arrays.sort(array);
        return array;
    }
    public SongLevel[] getB19() {
        final var b19 = new SongLevel[20];
        int num;
        int minIndex = 1;
        for (GameRecordItem item:this) {
            for (SongLevel songLevel:item) {
                if (songLevel.rks == 0f)
                    continue;
                if (b19[0] == null || songLevel.score == 1000000 && songLevel.rks > b19[0].rks)
                    b19[0] = songLevel;
                if (b19[minIndex] != null && songLevel.rks < b19[minIndex].rks)
                    continue;
                if (b19[minIndex] == null || songLevel.rks > b19[minIndex].rks) {
                    b19[minIndex] = songLevel;
                    minIndex = min(b19);
                }
            }
        }
        for (num = 1; num < 20; num++)
            if (b19[num] == null)
                break;
        Arrays.sort(b19,1,num);
        return b19;
    }
    private int min(SongLevel[] array) {
        int index = -1;
        double min = 17;
        for (int i = 1; i < 20; i++) {
            if (array[i] == null) return i;
            if (array[i].rks < min) {
                index = i;
                min = array[i].rks;
            }
        }
        return index;
    }
}
