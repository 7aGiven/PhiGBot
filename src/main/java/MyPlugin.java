import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;
import net.mamoe.mirai.event.events.MemberLeaveEvent;
import net.mamoe.mirai.event.events.NudgeEvent;
import net.mamoe.mirai.message.data.PlainText;

public final class MyPlugin extends JavaPlugin {
    public static final MyPlugin INSTANCE = new MyPlugin();
    private MyPlugin() {
        super(new JvmPluginDescriptionBuilder("given.PhigrosBot","0.0.2").build());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().error("启动");
        CommandManager.INSTANCE.registerCommand(MyCompositeCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestPhigrosCommand.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(TestCommand.INSTANCE,true);
        EventChannel<BotEvent> channel = GlobalEventChannel.INSTANCE
                .parentScope(INSTANCE)
                .filterIsInstance(BotEvent.class);
        //入群申请
        channel.subscribeAlways(MemberJoinRequestEvent.class, event->{
            event.getGroup().sendMessage("新成员来了，快去审核。");
        });
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
        Util.writeUser();
        super.onDisable();
    }
}