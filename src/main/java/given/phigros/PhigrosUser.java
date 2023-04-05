package given.phigros;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PhigrosUser {
    public String session;
    URI zipUrl;
    public long time;
    public final static HashMap<String,SongInfo> info = new HashMap<>();
    public PhigrosUser(String session) {
        if (!session.matches("[a-z0-9]{25}"))
            throw new RuntimeException("SessionToken格式错误。");
        this.session = session;
    }
    public PhigrosUser(URI zipUrl) {this.zipUrl = zipUrl;}
    public String update() throws IOException, InterruptedException {
        if (time == 0)
            time = System.currentTimeMillis();
        else if (System.currentTimeMillis() - time <= 90)
            throw new RuntimeException("更新间隔在90秒以内。");
        return SaveManager.update(this);
    }
    public static void readInfo(Path path) throws IOException {
        info.clear();
        try (Stream<String> stream = Files.lines(path)) {
            stream.forEach(s -> {
                String[] line = s.split(",");
                SongInfo songInfo = new SongInfo();
                songInfo.name = line[1];
                for (int i = 2; i < line.length; i++) {
                    songInfo.level[i-2] = Float.parseFloat(line[i]);
                }
                info.put(line[0],songInfo);
            });
        }
    }
    public static void validSession(String session) throws IOException, InterruptedException {
        SaveManager.save(session);
    }
    public SongLevel[] getB19() throws IOException, InterruptedException {
        return b19(extractZip("gameRecord"));
    }
    public SongExpect[] getExpect() throws IOException, InterruptedException {
        byte[] data = extractZip("gameRecord");
        float min = b19(data)[19].rks;
        final var gameRecord = new GameRecord(data);
        final ArrayList<SongExpect> arrayList = new ArrayList<>();
        for (String id:gameRecord) {
            final var songLevels = gameRecord.getSong();
            final var songInfo = PhigrosUser.info.get(id);
            for (int i = 0; i < 4; i++) {
                if (songLevels[i] == null || songInfo.level[i] < min)
                    continue;
                final var expect = (float) Math.sqrt(min/songInfo.level[i])*45+55;
                if (expect > songLevels[i].acc)
                    arrayList.add(new SongExpect(songInfo.name,i,songLevels[i].acc,expect));
            }
        }
        final var array = arrayList.toArray(SongExpect[]::new);
        Arrays.sort(array);
        return array;
    }
    private SongLevel[] b19(byte[] data) {
        final var gameRecord = new GameRecord(data);
        final var b19 = new SongLevel[20];
        int num;
        for (String id:gameRecord) {
            SongLevel[] songLevels = gameRecord.getSong();
            SongInfo songInfo = PhigrosUser.info.get(id);
            if (songInfo == null) throw new NullPointerException(String.format("不存在%s的定数",id));
            for (int i = 0; i < 4; i++) {
                if (songLevels[i] == null)
                    continue;
                if (songLevels[i].score != 0 && songLevels[i].acc >= 70) {
                    final var difficulty = songInfo.level[i];
                    final var rks = (float) Math.pow((songLevels[i].acc - 55) / 45,2) * difficulty;
                    if (b19[0] == null || songLevels[i].score == 1000000 && rks > b19[0].rks) {
                        b19[0] = songLevels[i];
                        b19[0].set(id,i,difficulty,rks);
                    }
                    num = min(b19);
                    if (num == -1) continue;
                    if (b19[num] == null || rks > b19[num].rks) {
                        b19[num] = songLevels[i];
                        b19[num].set(id,i,difficulty,rks);
                    }
                }
            }
        }
        for (num = 1; num < 20; num++)
            if (b19[num] == null)
                break;
        Arrays.sort(b19,1,num);
        return b19;
    }
    public void key() throws IOException, InterruptedException {
        new GameKey(extractZip("gameKey"));
    }
    public void modifyData(short num) throws Exception {
        ModifyStrategyImpl.data(this,num);
    }
    public void modifyAvater(String avater) throws Exception {
        ModifyStrategyImpl.avater(this,avater);
    }
    public void modifyCollection(String collection) throws Exception {
        ModifyStrategyImpl.collection(this,collection);
    }
    public void modifyChallenge(short challenge) throws Exception {
        ModifyStrategyImpl.challenge(this,challenge);
    }
    public void modifySong(String songId,int level,int s,float a,boolean fc) throws Exception {
        ModifyStrategyImpl.song(this,songId,level,s,a,fc);
    }
    public void downloadZip(Path path) throws IOException, InterruptedException {
        Files.write(path,getData());
    }
    public void uploadZip(Path path) throws IOException, InterruptedException {
        SaveManager saveManager = new SaveManager(this);
        saveManager.data = Files.readAllBytes(path);
        saveManager.uploadZip((short) 3);
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
    private byte[] extractZip(String name) throws IOException, InterruptedException {
        byte[] buffer;
        byte[] data = getData();
        try (ByteArrayInputStream reader = new ByteArrayInputStream(data)) {
            try (ZipInputStream zipReader = new ZipInputStream(reader)) {
                while (true) {
                    ZipEntry entry = zipReader.getNextEntry();
                    System.out.println(entry);
                    if (entry.getName().equals(name)) {
                        break;
                    }
                }
                zipReader.skip(1);
                buffer = zipReader.readAllBytes();
                zipReader.closeEntry();
            }
        }
        return SaveManager.decrypt(buffer);
    }
    private byte[] getData() throws IOException, InterruptedException {
        HttpResponse<byte[]> response = SaveManager.client.send(HttpRequest.newBuilder(zipUrl).build(),HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 404) throw new RuntimeException("存档文件不存在");
        return response.body();
    }
}
