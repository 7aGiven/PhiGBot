import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModifyStrategyImpl {
    public static final short challengeScore = 0;
    public static void song(long qqid,MyUser user, String name, int level, int s, float a, boolean fc) throws Exception {
        SaveManagement saveManagement = new SaveManagement(qqid,user);
        saveManagement.modify("gameRecord", data -> {
            boolean exist = false;
            Score score = new Score(data);
            for (String id:score) {
                if (name.equals(id)) {
                    Song song = score.getSong();
                    if (song.get(level).score == 0) {
                        throw new Exception("您尚未游玩此歌曲的该难度");
                    }
                    score.modifySong(level,s,a,fc);
                    data = score.getData();
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                throw new Exception("您尚未游玩此歌曲");
            }
            return data;
        });
        saveManagement.uploadZip(challengeScore);
    }
    public static void avater(long id,MyUser user,String avater) throws Exception {
        SaveManagement saveManagement = new SaveManagement(id,user);
        saveManagement.modify("gameKey", data -> {
            GameKey gameKey = new GameKey(data);
            boolean exist = false;
            for (String key:gameKey) {
                if (key.equals(avater)) {
                    exist = true;
                    data = gameKey.getKey();
                    if (data[4] == 1) {
                        throw new Exception("您已经拥有该头像");
                    }
                    data = gameKey.modifyAvater();
                    break;
                }
            }
            if (!exist) {
                data = gameKey.addAvater(avater.getBytes());
            }
            return data;
        });
        saveManagement.uploadZip(challengeScore);
    }
    public static void collection(long id,MyUser user,String collection) throws Exception {
        SaveManagement saveManagement = new SaveManagement(id,user);
        saveManagement.modify("gameKey", data -> {
            GameKey gameKey = new GameKey(data);
            boolean exist = false;
            for (String key:gameKey) {
                if (key.equals(collection)) {
                    exist = true;
                    data = gameKey.modifyCollection();
                    break;
                }
            }
            if (!exist) {
                data = gameKey.addCollection(collection.getBytes());
            }
            return data;
        });
        saveManagement.uploadZip(challengeScore);
    }
    public static void challenge(long id,MyUser user,short score) throws Exception {
        SaveManagement saveManagement = new SaveManagement(id,user);
        saveManagement.modify("gameProgress", data -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putShort(score);
            byteBuffer.position(0);
            byteBuffer.get(data,6,2);
            return data;
        });
        saveManagement.uploadZip(score);
    }
    public static void data(long id,MyUser user,short num) throws Exception {
        SaveManagement saveManagement = new SaveManagement(id,user);
        saveManagement.modify("gameProgress", data -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
                    outputStream.writeBytes(inputStream.readNBytes(8));
                    for (int i = 0; i < 5; i++) {
                        while (true) {
                            if (inputStream.read() > 0) {
                                break;
                            }
                        }
                    }
                    outputStream.writeBytes(new byte[1]);
                    if (num < 128) {
                        outputStream.write(num);
                    } else {
                        outputStream.writeBytes(new byte[]{(byte) (num%128|-128),(byte) (num/128)});
                    }
                    outputStream.writeBytes(new byte[3]);
                    outputStream.writeBytes(inputStream.readNBytes(inputStream.available()));
                }
                data = outputStream.toByteArray();
            }
            return data;
        });
        saveManagement.uploadZip(challengeScore);
    }
}