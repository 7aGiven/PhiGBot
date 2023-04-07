package given.phigros;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
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
    public static void readInfo(BufferedReader reader) throws IOException {
        info.clear();
        while (true) {
            String lineString = reader.readLine();
            if (lineString == null)
                break;
            String[] line = lineString.split(",");
            SongInfo songInfo = new SongInfo();
            songInfo.name = line[1];
            if (line.length != 5 && line.length != 6)
                throw new RuntimeException(String.format("曲目%s的定数数量错误。",songInfo.name));
            final var difficulty = new float[line.length - 2];
            for (int i = 0; i < line.length - 2; i++) {
                difficulty[i] = Float.parseFloat(line[i + 2]);
            }
            songInfo.level = difficulty;
            info.put(line[0],songInfo);
        }
    }
    static SongInfo getInfo(String id) {
        final var songInfo = info.get(id);
        if (songInfo == null)
            throw new RuntimeException(String.format("缺少%s的信息。", id));
        return songInfo;
    }
    public static void validSession(String session) throws IOException, InterruptedException {
        SaveManager.save(session);
    }
    public SongLevel[] getB19() throws IOException, InterruptedException {
        return new B19(extractZip("gameRecord")).getB19();
    }
    public SongExpect[] getExpect() throws IOException, InterruptedException {
        return new B19(extractZip("gameRecord")).getExpect();
    }
    private int min(SongLevel[] array) {
        int index = -1;
        double min = 17;
        for (int i = 1; i < 20; i++) {
            if (array[i].rks == 0f) return i;
            if (array[i].rks < min) {
                index = i;
                min = array[i].rks;
            }
        }
        return index;
    }
    public GameRecord getGameRecord() throws IOException, InterruptedException {
        return new GameRecord(extractZip("gameRecord"));
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
