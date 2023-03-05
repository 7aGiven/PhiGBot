import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
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
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class PhigrosCompositeCommand extends JCompositeCommand {
    public static final PhigrosCompositeCommand INSTANCE = new PhigrosCompositeCommand();
    private final String[] challenges = new String[]{"白","绿","蓝","橙","金","彩"};

    private PhigrosCompositeCommand() {
        super(MyPlugin.INSTANCE,"pImpl");
        setDescription("Phigros机器人");
    }
    public void bind(CommandSender sender,String session) throws Exception {
        GameUser myUser = new GameUser();
        myUser.session = session;
        myUser.zipUrl = SaveManager.getZipUrl(session);
        DAO.INSTANCE.users.put(sender.getUser().getId(), myUser);
        sender.sendMessage("绑定成功");
    }
    @SubCommand
    @Description("绑定")
    public void bind(CommandContext context, @Name("token")String session) throws Exception {
        SenderFacade sender = new SenderFacade(context,false);
        if (sender.subject instanceof Group) {
            sender.sendMessage("请私聊绑定");
            MessageSource.recall(context.getOriginalMessage());
        } else {
            GameUser myUser = new GameUser();
            myUser.session = session;
            myUser.zipUrl = SaveManager.getZipUrl(session);
            sender.putUser(myUser);
            sender.sendMessage("绑定成功");
        }
    }
    private String update(GameUser user) throws Exception {
        if (user.time == 0) {
            user.time = System.currentTimeMillis();
        }else if (System.currentTimeMillis() - user.time < 90 * 1000) {
            return null;
        }
        System.out.println(user.session);
        String summary = SaveManager.update(user);
        System.out.println(summary);
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(summary));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.position(1);
        int challenge = byteBuffer.getShort();
        float rks = byteBuffer.getFloat();
        return String.format("%.4f\n%s%d",rks,challenges[challenge/100],challenge%100);
    }
    @SubCommand
    @Description("B19图")
    public void b19(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        String summary = update(sender.myUser);
        if (summary != null) sender.sendMessage(summary);
        byte[] data = extractZip(sender.myUser.zipUrl, "gameRecord");
        System.out.println(System.currentTimeMillis());
        data = new B19(data).b19Pic();
        System.out.println(System.currentTimeMillis());
        ExternalResource ex = ExternalResource.create(data);
        sender.sendImage(ex);
    }
    @SubCommand
    @Description("B19测试")
    public void tt(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        String summary = update(sender.myUser);
        if (summary != null) sender.sendMessage(summary);
        byte[] data = extractZip(sender.myUser.zipUrl, "gameRecord");
        new B19(data).tt();
        ExternalResource ex = ExternalResource.create(MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/xx.png"));
        sender.sendImage(ex);
    }
    @SubCommand
    @Description("推分")
    public void improve(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        String summary = update(sender.myUser);
        if (summary != null) sender.sendMessage(summary);
        byte[] data = extractZip(sender.myUser.zipUrl, "gameRecord");
        sender.sendMessage(new B19(data).expectCalc(sender.user, data));
    }
    @SubCommand
    @Description("备份")
    public void backup(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        update(sender.myUser);
        Path path = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d.zip",sender.user.getId())).toPath();
        Files.write(path,getData(sender.myUser.zipUrl),StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        sender.sendMessage("备份成功");
    }
    @SubCommand
    @Description("恢复备份")
    public void restore(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        SaveManager saveManagement = new SaveManager(sender.user.getId(),sender.myUser);
        Path path = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d.zip",sender.user.getId())).toPath();
        saveManagement.data = Files.readAllBytes(path);
        saveManagement.uploadZip(ModifyStrategyImpl.challengeScore);
        sender.sendMessage("恢复成功");
    }
    @SubCommand
    @Description("改data")
    public void data(CommandContext context,@Name("MB数")short num) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (num >= 1024 || num < 0) {
            sender.sendMessage("不可超过1024MB");
            return;
        }
        ModifyStrategyImpl.data(sender.user.getId(),sender.myUser,num);
        sender.sendMessage("修改data成功");
    }
    @SubCommand
    @Description("添加头像")
    public void avater(CommandContext context,@Name("头像名")String avater) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("avater.txt").toPath())) {
            if (stream.noneMatch(s -> s.equals(avater))) {
                sender.sendMessage("该头像不存在");
                return;
            }
        }
        ModifyStrategyImpl.avater(sender.user.getId(),sender.myUser,avater);
        sender.sendMessage("添加头像成功");
    }
    @SubCommand
    @Description("添加收藏品")
    public void collection(CommandContext context,@Name("收藏名")String collection) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        try (Stream<String> stream = Files.lines(MyPlugin.INSTANCE.resolveDataFile("collection.txt").toPath())) {
            if (stream.noneMatch(s -> s.equals(collection))) {
                sender.sendMessage("该收藏品不存在");
                return;
            }
        }
        ModifyStrategyImpl.collection(sender.user.getId(),sender.myUser,collection);
        sender.sendMessage("添加头像成功");
    }
    @SubCommand
    @Description("改课题分")
    public void challenge(CommandContext context,@Name("分")short score) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (score > 599 || score < 0) {
            sender.sendMessage("非法课题分");
        }
        ModifyStrategyImpl.challenge(sender.user.getId(),sender.myUser,score);
        sender.sendMessage("课题分修改成功");
    }
    @SubCommand
    @Description("改成绩")
    public void modify(CommandContext context,@Name("歌名") String song,@Name("难度")String levelString,@Name("分数")int s,@Name("acc") float a,@Name("fc") boolean fc) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (!(sender.subject instanceof Group)) throw new Exception("只能在群内发送modify指令");
        final String streamSong = song;
        Optional<Map.Entry<String, SongInfo>> optional = DAO.INSTANCE.info.entrySet().stream().filter(entry -> entry.getValue().name.equals(streamSong)).findFirst();
        if (optional.isPresent()) {
            song = optional.get().getKey();
        } else {
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
                zipReader.skip(1);
                buffer = zipReader.readAllBytes();
                zipReader.closeEntry();
            }
        }
        return SaveManager.decrypt(buffer);
    }
    private byte[] getData(String zipUrl) throws Exception {
        HttpResponse<byte[]> response = SaveManager.client.send(HttpRequest.newBuilder(new URI(zipUrl)).build(),HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() == 404) throw new Exception("存档文件不存在");
        return response.body();
    }
}