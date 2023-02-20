import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.MemberJoinRequestEvent;

public final class MyPlugin extends JavaPlugin {
    public static final MyPlugin INSTANCE = new MyPlugin();
    private Listener<MemberJoinRequestEvent> joinlistener;
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
        //入群申请
        joinlistener = GlobalEventChannel.INSTANCE.filterIsInstance(MemberJoinRequestEvent.class).subscribeAlways(MemberJoinRequestEvent.class, event->{
            event.getGroup().sendMessage("新成员来了，快去审核。");
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        joinlistener.complete();
        Util.writeUser();
    }
}