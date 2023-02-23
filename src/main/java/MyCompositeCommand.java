import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class MyCompositeCommand extends JCompositeCommand {
    public static final MyCompositeCommand INSTANCE = new MyCompositeCommand();
    private final String[] challenges = new String[]{"白","绿","蓝","橙","金","彩"};

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
    private String update(MyUser user) throws Exception {
        if (user.time == 0) {
            user.time = System.currentTimeMillis();
        }else if (System.currentTimeMillis() - user.time < 90 * 1000) {
            return null;
        }
        System.out.println(user.session);
        String summary = SaveManagement.update(user);
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(summary));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.position(1);
        int challenge = byteBuffer.getShort();
        float rks = byteBuffer.getFloat();
        return String.format("%.4f\n%s%d",rks,challenges[challenge/100],challenge%100);
    }
    @SubCommand
    @Description("B19图")
    public void b19(CommandContext context) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            String summary = update(sender.myUser);
            if (summary != null) sender.sendMessage(summary);
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
            String summary = update(sender.myUser);
            if (summary != null) sender.sendMessage(summary);
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
            Path path = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d.zip",sender.user.getId())).toPath();
            Files.write(path,getData(sender.myUser.zipUrl),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
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
            SaveManagement saveManagement = new SaveManagement(sender.user.getId(),sender.myUser);
            Path path = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d.zip",sender.user.getId())).toPath();
            saveManagement.data = Files.readAllBytes(path);
            saveManagement.uploadZip(ModifyStrategyImpl.challengeScore);
            sender.sendMessage("恢复成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("改data")
    public void data(CommandContext context,@Name("MB数")short num) {
        SenderFacade sender = SenderFacade.getInstance(context);
        if (sender == null) return;
        try {
            if (num >= 1024 || num < 0) {
                sender.sendMessage("不可超过1024MB");
                return;
            }
            ModifyStrategyImpl.data(sender.user.getId(),sender.myUser,num);
            sender.sendMessage("修改data成功");
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
        try {
            try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("avater.txt").toPath())) {
                if (stream.noneMatch(s -> s.equals(avater))) {
                    sender.sendMessage("该头像不存在");
                    return;
                }
            }
            ModifyStrategyImpl.avater(sender.user.getId(),sender.myUser,avater);
            sender.sendMessage("添加头像成功");
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
        try {
            try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("collection.txt").toPath())) {
                if (stream.noneMatch(s -> s.equals(collection))) {
                    sender.sendMessage("该收藏品不存在");
                    return;
                }
            }
            ModifyStrategyImpl.collection(sender.user.getId(),sender.myUser,collection);
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
        try {
            if (score > 599 || score < 0) {
                sender.sendMessage("非法课题分");
            }
            ModifyStrategyImpl.challenge(sender.user.getId(),sender.myUser,score);
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
                ModifyStrategyImpl.song(sender.user.getId(),sender.myUser, song, level, s, a, fc);
                sender.sendMessage(String.format("%s %s %d %.2f %b",song,levelString,s,a,fc));
            } catch (Exception e) {
                e.printStackTrace();
                sender.sendMessage(e.toString());
            }
        } else {
            sender.sendMessage("只能在群内发送modify指令");
        }
    }
    public byte[] extractZip(String zipUrl,String name) throws Exception {
        byte[] buffer;
        try (ByteArrayInputStream reader = new ByteArrayInputStream(getData(zipUrl))) {
            try (ZipInputStream zipReader = new ZipInputStream(reader)) {
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
            }
        }
        return buffer;
    }
    private byte[] getData(String zipUrl) throws Exception {
        HttpResponse<byte[]> response = SaveManagement.client.send(HttpRequest.newBuilder(new URI(zipUrl)).build(),HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 404) {
            throw new Exception("存档文件不存在");
        }
        return response.body();
    }
}