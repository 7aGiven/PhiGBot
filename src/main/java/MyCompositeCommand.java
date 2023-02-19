import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MyCompositeCommand extends JCompositeCommand {
    public static final MyCompositeCommand INSTANCE = new MyCompositeCommand();
    HashMap<Long, MyUser> users;
    private final String[] challenges = new String[]{"白","绿","蓝","橙","金","彩"};
    private final short challengeScore = 0;

    public MyCompositeCommand() {
        super(MyPlugin.INSTANCE,"p");
        setDescription("Phigros机器人");
    }
    @SubCommand
    @Description("绑定")
    public void bind(CommandContext context, @Name("token")String session) {
        Sender sender = new Sender(context);
        try {
            if (sender.subject instanceof Group) {
                sender.sendMessage("请私聊绑定");
                MessageSource.recall(context.getOriginalMessage());
            } else {
                MyUser myUser = new MyUser();
                myUser.session = session;
                myUser.zipUrl = SaveManagement.getZipUrl(session);
                users.put(sender.user.getId(), myUser);
                sender.sendMessage("绑定成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("更新")
    public void update(CommandContext context) {
        System.out.println("更新");
        Sender sender = new Sender(context);
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            System.out.println(user.session);
            String[] update = SaveManagement.update(user.session);
            user.zipUrl = update[0];
            int challenge = 0;
            float rks = 0;
            if (update[1] == null) {
                sender.sendMessage("警告！请修复存档！");
            } else {
                ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(update[1]));
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                byteBuffer.position(1);
                challenge = byteBuffer.getShort();
                rks = byteBuffer.getFloat();
            }
            sender.sendMessage(String.format("%.4f\n%s%d",rks,challenges[challenge/100],challenge%100));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("B19图")
    public void b19(CommandContext context) {
        Sender sender = new Sender(context);
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = extractZip(user.zipUrl, "gameRecord");
            data = SaveManagement.decrypt(data);
            new B19(data).b19Pic();
            ExternalResource ex = ExternalResource.create(MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/xx.png"));
            sender.sendImage(ex);
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("推分")
    public void improve(CommandContext context) {
        Sender sender = new Sender(context);
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = extractZip(user.zipUrl, "gameRecord");
            data = SaveManagement.decrypt(data);
            sender.sendMessage(new B19(data).expectCalc(sender.user, data));
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("备份")
    public void backup(CommandContext context) {
        Sender sender = new Sender(context);
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            HttpResponse<byte[]> response = SaveManagement.client.send(HttpRequest.newBuilder(new URI(user.zipUrl)).build(),HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 404) {
                sender.sendMessage("文件不存在");
                return;
            }
            Files.write(MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%s.zip",sender.user.getId())).toPath(),response.body(), StandardOpenOption.CREATE,StandardOpenOption.WRITE);
            sender.sendMessage("备份成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("恢复备份")
    public void restore(CommandContext context) {
        Sender sender = new Sender(context);
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = Files.readAllBytes(MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%s.zip",sender.user.getId())).toPath());
            SaveManagement.uploadZip(user.session,data,challengeScore);
            sender.sendMessage("恢复成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("添加头像")
    public void avater(CommandContext context,@Name("头像名")String avater) {
        Sender sender = new Sender(context);
        try {
            Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("avater.txt").toPath());
            if (stream.filter(s -> s.equals(avater)).findFirst().isEmpty()) {
                sender.sendMessage("该头像不存在");
                return;
            }
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = SaveManagement.addAvater(user.zipUrl,avater);
            if (data == null) {
                sender.sendMessage("您已经拥有该头像");
                return;
            }
            SaveManagement.uploadZip(user.session,data,challengeScore);
            sender.sendMessage("添加头像成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("添加收藏品")
    public void collection(CommandContext context,@Name("收藏名")String collection) {
        Sender sender = new Sender(context);
        try {
            Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("collection.txt").toPath());
            if (stream.filter(s -> s.equals(collection)).findFirst().isEmpty()) {
                sender.sendMessage("该收藏品不存在");
                return;
            }
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = SaveManagement.addCollection(user.zipUrl,collection);
            SaveManagement.uploadZip(user.session,data,challengeScore);
            sender.sendMessage("添加头像成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改课题分")
    public void challenge(CommandContext context,@Name("分")short score) {
        Sender sender = new Sender(context);
        if (score > 599) {
            sender.sendMessage("非法课题分");
        }
        try {
            MyUser user = getUser(sender);
            if (user == null) return;
            byte[] data = SaveManagement.challenge(user.zipUrl,score);
            SaveManagement.uploadZip(user.session,data,score);
            sender.sendMessage("课题分修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改成绩")
    public void modify(CommandContext context,@Name("歌名") String song,@Name("难度")String levelString,@Name("分数")int s,@Name("acc") float a,@Name("fc") boolean fc) {
        Sender sender = new Sender(context);
        if (sender.subject instanceof Group) {
            try {
                MyUser user = getUser(sender);
                if (user == null) return;
                boolean exist = false;
                for (Map.Entry<String,SongInfo> entry:B19.info.entrySet()) {
                    if (entry.getKey().equals(song)) {
                        exist = true;
                        break;
                    }
                    if (entry.getValue().name.equals(song)) {
                        song = entry.getKey();
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    sender.sendMessage("该歌曲不存在");
                    return;
                }
                System.out.println(song);
                int level = -1;
                for (int i = 0; i < 4; i++) {
                    if (B19.levels[i].equals(levelString)) {
                        level = i;
                        break;
                    }
                }
                if (level == -1) {
                    sender.sendMessage("难度为EZ,HD,IN,AT");
                    return;
                }
                byte[] data = SaveManagement.modifySong(user.zipUrl, song, level, s, a, fc);
                if (data == null) {
                    sender.sendMessage("您尚未游玩此歌曲");
                    return;
                }
                SaveManagement.uploadZip(user.session, data, challengeScore);
                sender.sendMessage(String.format("%s %s %d %.2f %b",song,levelString,s,a,fc));
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(e.toString());
            }
        } else {
            sender.sendMessage("只能在群内发送modify指令");
        }
    }
    public byte[] extractZip(String zipUrl,String name) {
        byte[] buffer = null;
        try {
            HttpResponse<InputStream> response = SaveManagement.client.send(HttpRequest.newBuilder(new URI(zipUrl)).build(),HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() == 404) {
                return null;
            }
            InputStream reader = response.body();
            ZipInputStream zipReader = new ZipInputStream(reader);
            while (true) {
                ZipEntry entry = zipReader.getNextEntry();
                System.out.println(entry);
                if (entry.getName().equals(name)) {
                    break;
                }
            }
            zipReader.read();
            buffer = zipReader.readAllBytes();
            zipReader.closeEntry();
            zipReader.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }
    private MyUser getUser(Sender sender) {
        MyUser user = users.get(sender.user.getId());
        if (user == null) {
            sender.sendMessage("您尚未绑定SessionToken");
        }
        return user;
    }
}