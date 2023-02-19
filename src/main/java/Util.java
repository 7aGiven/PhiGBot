import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Util {
    public static void readUser() {
        HashMap<Long, MyUser> users = new HashMap<>();
        try (FileReader fileReader = new FileReader(MyPlugin.INSTANCE.resolveDataFile("user.csv"))) {
            try (BufferedReader reader = new BufferedReader(fileReader) ) {
                String lineStr;
                String[] line;
                MyUser myUser;
                while ((lineStr = reader.readLine()) != null) {
                    line = lineStr.split(",");
                    myUser = new MyUser();
                    myUser.session = line[1];
                    myUser.zipUrl = line[2];
                    users.put(Long.valueOf(line[0]), myUser);
                }
            }
        } catch (Exception e) {}
        MyCompositeCommand.INSTANCE.users = users;
    }
    public static void writeUser() {
        try (FileWriter writer = new FileWriter(MyPlugin.INSTANCE.resolveDataFile("user.csv"))) {
            HashMap<Long, MyUser> users = MyCompositeCommand.INSTANCE.users;
            for (Long key:users.keySet()) {
                MyUser myUser = users.get(key);
                writer.write(String.format("%d,%s,%s\n",key, myUser.session, myUser.zipUrl));
            }
        } catch (Exception e) {}
    }
    public static void readLevel() {
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
        B19.info = level;
    }
}