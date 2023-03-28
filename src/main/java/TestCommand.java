import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

public final class TestCommand extends JSimpleCommand {
    public static final TestCommand INSTANCE = new TestCommand();
    private TestCommand() {
        super(MyPlugin.INSTANCE,"test");
    }

    @Handler
    public void onCommand(@NotNull CommandContext context,Member member,String... args) {
        if (args.length == 0) return;
        CommandSender sender = CommandSender.of(member);
        StringBuilder stringBuilder = new StringBuilder();
        for (String arg : args) {
            String s = arg;
            if (s.contains(" ")) {
                s = "\"" + s + "\"";
            }
            stringBuilder.append(s);
            stringBuilder.append(" ");
        }
        MessageChain chain = context.getOriginalMessage().get(MessageSource.Key).plus(stringBuilder.toString());
        System.out.println(sender);
        Throwable e = CommandManager.INSTANCE.executeCommand(sender,chain,false).getException();
        if (e != null) {
            MyPlugin.INSTANCE.getLogger().warning(e);
            sender.sendMessage(e.toString());
        }
    }
}