import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class Util {
    private static MyUser myUser;
    public static HashMap<Long, MyUser> readUser() {
        HashMap<Long, MyUser> users = new HashMap<>();
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath())) {
            stream.forEach(s -> {
                String[] line = s.split(",");
                myUser = new MyUser();
                myUser.session = line[1];
                myUser.zipUrl = line[2];
                users.put(Long.valueOf(line[0]), myUser);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return users;
    }
    public static void writeUser() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Long,MyUser> entry:SenderFacade.users.entrySet()) {
            builder.append(String.format("%d,%s,%s\n",entry.getKey(),entry.getValue().session, entry.getValue().zipUrl));
        }
        try {
            Files.writeString(MyPlugin.INSTANCE.resolveDataFile("user.csv").toPath(),builder.toString(), StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static HashMap<String,SongInfo> readLevel() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return level;
    }
}