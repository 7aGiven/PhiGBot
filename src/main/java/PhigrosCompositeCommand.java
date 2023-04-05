import given.phigros.PhigrosUser;
import given.phigros.SongInfo;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class PhigrosCompositeCommand extends JCompositeCommand {
    public static final PhigrosCompositeCommand INSTANCE = new PhigrosCompositeCommand();
    private final String[] challenges = new String[]{"白","绿","蓝","橙","金","彩"};

    private PhigrosCompositeCommand() {
        super(MyPlugin.INSTANCE,"p");
        setDescription("Phigros机器人");
    }
    public void bind(User user, String session) throws Exception {
        PhigrosUser myUser = new PhigrosUser(session);
        PhigrosUser.validSession(session);
        DAO.INSTANCE.users.put(user.getId(), myUser);
        user.sendMessage("绑定成功");
    }
    private String update(PhigrosUser user) throws Exception {
        final var summary = user.update();
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
        sender.sendMessage(summary);
        byte[] data = new B19(sender.myUser).b19Pic();
        sender.sendImage(ExternalResource.create(data));
    }
    @SubCommand
    @Description("推分")
    public void improve(CommandContext context) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        String summary = update(sender.myUser);
        sender.sendMessage(summary);
        sender.sendMessage(new B19(sender.myUser).expectCalc(sender.user));
    }
    @SubCommand
    @Description("改data")
    public void data(CommandContext context,@Name("MB数")short num) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (num >= 1024 || num < 0) {
            sender.sendMessage("不可超过1024MB");
            return;
        }
        sender.myUser.modifyData(num);
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
        sender.myUser.modifyAvater(avater);
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
        sender.myUser.modifyCollection(collection);
        sender.sendMessage("添加头像成功");
    }
    @SubCommand
    @Description("改课题分")
    public void challenge(CommandContext context,@Name("分")short score) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (score > 599 || score < 0) {
            sender.sendMessage("非法课题分");
        }
        sender.myUser.modifyChallenge(score);
        sender.sendMessage("课题分修改成功");
    }
    @SubCommand
    @Description("改成绩AP")
    public void modify(CommandContext context,@Name("歌名") String song,@Name("难度")String levelString) throws Exception {
        modify(context,song,levelString,1000000,100f,true);
    }
    @SubCommand
    @Description("改成绩")
    public void modify(CommandContext context,@Name("歌名") String song,@Name("难度")String levelString,@Name("分数")int s,@Name("acc") float a,@Name("fc") boolean fc) throws Exception {
        SenderFacade sender = new SenderFacade(context);
        if (!(sender.subject instanceof Group)) throw new Exception("只能在群内发送modify指令");
        final String streamSong = song;
        Optional<Map.Entry<String, SongInfo>> optional = PhigrosUser.info.entrySet().stream().filter(entry -> entry.getValue().name.equals(streamSong)).findFirst();
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
        sender.myUser.modifySong(song,level,s,a,fc);
        sender.sendMessage(String.format("%s %s %d %.2f %b",song,levelString,s,a,fc));
    }
}