package given.phigros;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.RemoteException;

class ModifyStrategyImpl {
    public static final short challengeScore = 3;
    public static void song(PhigrosUser user, String name, int level, int s, float a, boolean fc) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameRecord", data -> {
            boolean exist = false;
            GameRecord score = new GameRecord(data);
            for (String id:score) {
                if (name.equals(id)) {
                    exist = true;
                    SongLevel[] songLevels = score.getSong();
                    if (songLevels[level].score == 0) {
                        throw new RuntimeException("您尚未游玩此歌曲的该难度");
                    }
                    data = score.modifySong(level,s,a,fc);
                    break;
                }
            }
            if (!exist) {
                throw new RuntimeException("您尚未游玩此歌曲");
            }
            return data;
        });
    }
    public static void avater(PhigrosUser user, String avater) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameKey", data -> {
            GameKey gameKey = new GameKey(data);
            boolean exist = false;
            for (String key:gameKey) {
                if (key.equals(avater)) {
                    exist = true;
                    data = gameKey.getKey();
                    if (data[4] == 1) throw new RuntimeException("您已经拥有该头像");
                    data = gameKey.modifyAvater();
                    break;
                }
            }
            if (!exist) {
                data = gameKey.addAvater(avater.getBytes());
            }
            return data;
        });
    }
    public static void collection(PhigrosUser user, String collection) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameKey", data -> {
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
    }
    public static void challenge(PhigrosUser user, short score) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameProgress", data -> {
            ByteBuffer byteBuffer = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putShort(score);
            byteBuffer.position(0);
            byteBuffer.get(data,6,2);
            return data;
        });
    }
    public static void data(PhigrosUser user, short num) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameProgress", data -> {
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
    }
}