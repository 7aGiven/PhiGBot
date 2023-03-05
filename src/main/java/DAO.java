import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class DAO {
    public static final DAO INSTANCE;
    static {
        try {
            INSTANCE = new DAO();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public final HashMap<Long, GameUser> users;
    public final HashMap<String,SongInfo> info;
    private DAO() throws IOException {
        users = readUser();
        info = readLevel();
    }
    private HashMap<Long, GameUser> readUser() throws IOException {
        Path path = MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath();
        HashMap<Long, GameUser> users = new HashMap<>();
        if (!Files.exists(path)) return users;
        try (Stream<String> stream = Files.lines(path)) {
            stream.forEach(s -> {
                String[] line = s.split(",");
                GameUser myUser = new GameUser();
                myUser.session = line[1];
                users.put(Long.valueOf(line[0]), myUser);
            });
        }
        return users;
    }
    public void writeUser() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long, GameUser> entry:users.entrySet()) {
            builder.append(String.format("%d,%s\n",entry.getKey(),entry.getValue().session));
        }
        try {
            Files.writeString(MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath(),builder.toString(),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private HashMap<String,SongInfo> readLevel() throws IOException {
        HashMap<String,SongInfo> level = new HashMap<>();
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("info.csv").toPath())) {
            stream.forEach(s -> {
                String[] line = s.split(",");
                SongInfo songInfo = new SongInfo();
                songInfo.name = line[1];
                for (int i = 2; i < line.length; i++) {
                    songInfo.level[i-2] = Double.valueOf(line[i]);
                }
                level.put(line[0],songInfo);
            });
        }
        return level;
    }
    public static boolean getBit(int data, int index) {return (data & 1 << index) != 0;}
}