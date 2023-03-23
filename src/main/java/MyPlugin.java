import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

import java.util.regex.Pattern;

public final class MyPlugin extends JavaPlugin {
    public static final MyPlugin INSTANCE = new MyPlugin();
    private MyPlugin() {
        super(new JvmPluginDescriptionBuilder("given.PhigrosBot","0.0.3").build());
    }
    @Override
    public void onEnable() {
        getLogger().error("启动");
        CommandManager.INSTANCE.registerCommand(PhigrosCompositeCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestPhigrosCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestCommand.INSTANCE,true);
        EventChannel<BotEvent> channel = GlobalEventChannel.INSTANCE
                .parentScope(INSTANCE)
                .filterIsInstance(BotEvent.class);
        //命令转接
        channel.subscribeAlways(MessageSyncEvent.class,event -> {
            CommandSender sender = null;
            if (event instanceof GroupMessageSyncEvent) {
                sender = CommandSender.of((Member) event.getSender());
            } else if (event instanceof GroupTempMessageSyncEvent) {
                sender = CommandSender.of((NormalMember) event.getSender());
            }
            if (sender == null) return;
            final var exception = CommandManager.INSTANCE.executeCommand(sender,event.getMessage(),false).getException();
            if (exception == null) return;
            exception.printStackTrace();
            event.getSender().sendMessage(exception.toString());
        });
        //bind命令
        channel.subscribeAlways(UserMessageEvent.class,event -> {
            MessageChain chain = event.getMessage();
            if (chain.size() != 2) return;
            String session = chain.get(1).contentToString();
            if (session.matches("[a-z0-9]{25}")) {
                event.getSubject().sendMessage("匹配成功");
                try {
                    PhigrosCompositeCommand.INSTANCE.bind(event.getSubject(),session);
                } catch (Exception e) {
                    event.getSubject().sendMessage(e.toString());
                }
            }
        });
        //入群申请
        final var pattern = Pattern.compile("^(?:问题：rks多少？\n答案：)(1?\\d\\.\\d\\d?)$");
        channel.subscribeAlways(MemberJoinRequestEvent.class, event->{
            final var matcher = pattern.matcher(event.getMessage());
            if (matcher.matches() && Float.parseFloat(matcher.group(1)) <= 16.12) {
                event.accept();
            } else event.getGroup().sendMessage("新成员来了，快去审核。");
        });
        //离群通知
        channel.subscribeAlways(MemberLeaveEvent.class, event -> {
            if (event.getGroup().getId() == 282781491 || event.getGroup().getId() == 1047497524) {
                String nick = event.getMember().getNick();
                event.getGroup().sendMessage(nick+"离开了。");
            }
        });
        //戳一戳
        channel.filterIsInstance(NudgeEvent.class)
                .filter(event -> event.getTarget().getId() == event.getBot().getId())
                .subscribeAlways(NudgeEvent.class, event -> {
                    if (!(event.getSubject() instanceof Group)) return;
                    Member member;
                    if (event.getFrom() instanceof Bot) {
                        member = ((Group) event.getSubject()).getBotAsMember();
                    } else {
                        member = (Member) event.getFrom();
                    }
                    CommandManager.INSTANCE.executeCommand(CommandSender.of(member),new PlainText("/help"),true);
                });
        channel.subscribeAlways(GroupMessagePostSendEvent.class,event -> {
            if (event.getMessage().contentToString().startsWith("◆ /help     # 查看指令帮助\n◆ /")) {
                event.getReceipt().recallIn(60000);
            }
        });
    }
    @Override
    public void onDisable() {
        DAO.INSTANCE.writeUser();
    }
}