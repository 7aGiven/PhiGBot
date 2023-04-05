package given.phigros;

import java.io.IOException;

class ModifyStrategyImpl {
    public static final short challengeScore = 3;
    public static void song(PhigrosUser user, String name, int level, int s, float a, boolean fc) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameRecord", data -> {
            boolean exist = false;
            GameRecord score = new GameRecord(data);
            for (String id:score) {
                if (name.equals(id)) {
                    exist = true;
                    score.modifySong(level,s,a,fc);
                    data = score.getData();
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
                    if (gameKey.getKey(4) == 1)
                        throw new RuntimeException("您已经拥有该头像");
                    gameKey.setKey(4,1);
                    data = gameKey.getData();
                    break;
                }
            }
            if (!exist) {
                gameKey.addKey(avater, new byte[] {16, 1});
                data = gameKey.getData();
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
                    gameKey.setKey(2, gameKey.getKey(2) +  1);
                    data = gameKey.getData();
                    break;
                }
            }
            if (!exist) {
                gameKey.addKey(collection, new byte[] {4, 1});
                data = gameKey.getData();
            }
            return data;
        });
    }
    public static void challenge(PhigrosUser user, short score) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameProgress", data -> {
            final var gameProgress = new GameProgress(data);
            gameProgress.setChallenge(score);
            return gameProgress.getData();
        });
    }
    public static void data(PhigrosUser user, short num) throws IOException, InterruptedException {
        SaveManager.modify(user,challengeScore,"gameProgress", data -> {
            final var gameProgress = new GameProgress(data);
            gameProgress.setGameData(num);
            return gameProgress.getData();
        });
    }
}