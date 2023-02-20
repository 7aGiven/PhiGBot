import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MyCompositeCommand extends JCompositeCommand {
    public static final MyCompositeCommand INSTANCE = new MyCompositeCommand();
    private final String[] challenges = new String[]{"白","绿","蓝","橙","金","彩"};
    private final short challengeScore = 0;

    private MyCompositeCommand() {
        super(MyPlugin.INSTANCE,"p");
        setDescription("Phigros机器人");
    }
    @SubCommand
    @Description("绑定")
    public void bind(CommandContext context, @Name("token")String session) {
        SenderFacade sender = SenderFacade.getInstance(context,false);
        try {
            if (sender.subject instanceof Group) {
                sender.sendMessage("请私聊绑定");
                MessageSource.recall(context.getOriginalMessage());
            } else {
                MyUser myUser = new MyUser();
                myUser.session = session;
                myUser.zipUrl = SaveManagement.getZipUrl(session);
                sender.putUser(myUser);
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
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            System.out.println(sender.myUser.session);
            String[] update = SaveManagement.update(sender.myUser.session);
            sender.myUser.zipUrl = update[0];
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
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            byte[] data = extractZip(sender.myUser.zipUrl, "gameRecord");
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
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            byte[] data = extractZip(sender.myUser.zipUrl, "gameRecord");
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
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            HttpResponse<byte[]> response = SaveManagement.client.send(HttpRequest.newBuilder(new URI(sender.myUser.zipUrl)).build(),HttpResponse.BodyHandlers.ofByteArray());
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
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            byte[] data = Files.readAllBytes(MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%s.zip",sender.user.getId())).toPath());
            SaveManagement.uploadZip(sender.myUser.session,data,challengeScore);
            sender.sendMessage("恢复成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("添加头像")
    public void avater(CommandContext context,@Name("头像名")String avater) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("avater.txt").toPath())) {
            if (stream.filter(s -> s.equals(avater)).findFirst().isEmpty()) {
                sender.sendMessage("该头像不存在");
                return;
            }
            byte[] data = ModifyStrategyImpl.avater(sender.myUser.zipUrl,avater);
            if (data == null) {
                sender.sendMessage("您已经拥有该头像");
                return;
            }
            SaveManagement.uploadZip(sender.myUser.session,data,challengeScore);
            sender.sendMessage("添加头像成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改data")
    public void data(CommandContext context,@Name("MB数")short num) {
        System.out.println("data");
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            if (num >= 1024 || num < 0) {
                sender.sendMessage("不可超过1024MB");
                return;
            }
            System.out.println("try");
            byte[] data = ModifyStrategyImpl.data(sender.myUser.zipUrl,num);
            System.out.println(data);
            System.out.println(Arrays.toString(data));
            SaveManagement.uploadZip(sender.myUser.session,data,challengeScore);
            sender.sendMessage("修改data成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("添加收藏品")
    public void collection(CommandContext context,@Name("收藏名")String collection) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("collection.txt").toPath())) {
            if (stream.filter(s -> s.equals(collection)).findFirst().isEmpty()) {
                sender.sendMessage("该收藏品不存在");
                return;
            }
            byte[] data = ModifyStrategyImpl.collection(sender.myUser.zipUrl,collection);
            SaveManagement.uploadZip(sender.myUser.session,data,challengeScore);
            sender.sendMessage("添加头像成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改课题分")
    public void challenge(CommandContext context,@Name("分")short score) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        if (score > 599 || score < 0) {
            sender.sendMessage("非法课题分");
        }
        try {
            byte[] data = ModifyStrategyImpl.challenge(sender.myUser.zipUrl,score);
            SaveManagement.uploadZip(sender.myUser.session,data,score);
            sender.sendMessage("课题分修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改成绩")
    public void modify(CommandContext context,@Name("歌名") String song,@Name("难度")String levelString,@Name("分数")int s,@Name("acc") float a,@Name("fc") boolean fc) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        if (sender.subject instanceof Group) {
            try {
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
                for (int i = 0; i < B19.levels.length; i++) {
                    if (B19.levels[i].equals(levelString)) {
                        level = i;
                        break;
                    }
                }
                if (level == -1) {
                    sender.sendMessage("难度为EZ,HD,IN,AT");
                    return;
                }
                byte[] data = ModifyStrategyImpl.song(sender.myUser.zipUrl, song, level, s, a, fc);
                if (data == null) {
                    sender.sendMessage("您尚未游玩此歌曲");
                    return;
                }
                SaveManagement.uploadZip(sender.myUser.session, data, challengeScore);
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
}