import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;

import java.util.Arrays;

public final class MyPlugin extends JavaPlugin {
    public static final MyPlugin INSTANCE = new MyPlugin();
    private MyPlugin() {
        super(new JvmPluginDescriptionBuilder("given.PhigrosBot","0.0.2").build());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().error("启动");
        CommandManager.INSTANCE.registerCommand(PhigrosRawCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(PhigrosCompositeCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestPhigrosCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestCommand.INSTANCE,true);
        EventChannel<BotEvent> channel = GlobalEventChannel.INSTANCE
                .parentScope(INSTANCE)
                .filterIsInstance(BotEvent.class);
        //bind命令
        channel.subscribeAlways(UserMessageEvent.class,event -> {
            MessageChain chain = event.getMessage();
            for (int i = 0; i < chain.size(); i++) {
                System.out.println(i);
                System.out.println(chain.get(i).contentToString());
                event.getSubject().sendMessage(chain.get(i));
            }
            if (chain.get(0).contentToString().matches("^[a-z0-9]{25}$")) {
//                CommandSender sender = CommandSender.from(event);
                event.getSubject().sendMessage("达到绑定处");
//                PhigrosCompositeCommand.INSTANCE.bind(sender,chain.get(0).contentToString());
            }
        });
        //入群申请
        channel.subscribeAlways(MemberJoinRequestEvent.class, event->{
            event.getGroup().sendMessage("新成员来了，快去审核。");
            if (event.getMessage().matches("^1?\\d\\.\\d\\d$")) {
                float rks = Float.parseFloat(event.getMessage());
                if (rks <= 16.12) {
                    event.getGroup().sendMessage("条件匹配，审核通过");
                    event.accept();
                }
            } else {
                event.getGroup().sendMessage("匹配失败\n" + event.getMessage() + "\n" + Arrays.toString(event.getMessage().getBytes()));
            }
        });
        //离群通知
        channel.subscribeAlways(MemberLeaveEvent.class, event -> {
            String nick = event.getMember().getNick();
            event.getGroup().sendMessage(nick+"离开了。");
        });
        //戳一戳
        channel.filterIsInstance(NudgeEvent.class)
                .filter(event -> event.getTarget().getId() == event.getBot().getId() && event.getFrom() instanceof Member)
                .subscribeAlways(NudgeEvent.class, event -> {
                    CommandSender sender = CommandSender.of((Member) event.getFrom());
                    CommandManager.INSTANCE.executeCommand(sender,new PlainText("/help"),true);
                });
    }
    @Override
    public void onDisable() {
        DAO.INSTANCE.writeUser();
        super.onDisable();
    }
}