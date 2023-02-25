import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Util {
    private static MyUser myUser;
    public static HashMap<Long, MyUser> readUser() {
        try {
            Path path = MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath();
            if (!Files.exists(path)) Files.createFile(path);
            HashMap<Long, MyUser> users = new HashMap<>();
            try (Stream<String> stream = Files.lines(path)) {
                stream.forEach(s -> {
                    String[] line = s.split(",");
                    myUser = new MyUser();
                    myUser.session = line[1];
                    users.put(Long.valueOf(line[0]), myUser);
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void writeUser() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long,MyUser> entry:SenderFacade.users.entrySet()) {
            builder.append(String.format("%d,%s\n",entry.getKey(),entry.getValue().session));
        }
        try {
            Files.writeString(MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath(),builder.toString(),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static HashMap<String,SongInfo> readLevel() {
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("info.csv").toPath())) {
            HashMap<String,SongInfo> level = new HashMap<>();
            stream.forEach(s -> {
                String[] line = s.split(",");
                SongInfo songInfo = new SongInfo();
                songInfo.name = line[1];
                for (int i = 2; i < line.length; i++) {
                    songInfo.level[i-2] = Double.valueOf(line[i]);
                }
                level.put(line[0],songInfo);
            });
            return level;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static boolean getBit(int data, int index) {return (data & 1 << index) != 0;}
}