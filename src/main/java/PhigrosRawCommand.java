import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import org.jetbrains.annotations.NotNull;

public class PhigrosRawCommand extends JRawCommand {
    public static final PhigrosRawCommand INSTANCE = new PhigrosRawCommand();
    public PhigrosRawCommand() {
        super(MyPlugin.INSTANCE,"p");
        setUsage(PhigrosCompositeCommand.INSTANCE.getUsage().replace("Impl",""));
        setDescription(PhigrosCompositeCommand.INSTANCE.getDescription());
    }
    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        CommandSender sender = context.getSender();
        if (sender instanceof OtherClientCommandSenderOnMessageSync) {
            User user = ((OtherClientCommandSenderOnMessageSync) sender).getFromEvent().getSender();
            if (user instanceof Member) {
                sender = CommandSender.of((Member) user);
            } else {
                sender = CommandSender.of(user,false);
            }
        }
        String command = context.getOriginalMessage().get(1).contentToString().replaceFirst("/p","/pImpl");
        MessageChain chain = context.getOriginalMessage().get(MessageSource.Key).plus(command);
        Throwable e = CommandManager.INSTANCE.executeCommand(sender,chain,false).getException();
        if (e != null) {
            MyPlugin.INSTANCE.getLogger().warning(e);
            sender.getSubject().sendMessage(e.toString());
        }
    }
}
