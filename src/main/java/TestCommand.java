import net.mamoe.mirai.console.command.*;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;

public final class TestCommand extends JRawCommand {
    public static final TestCommand INSTANCE = new TestCommand();
    private TestCommand() {
        super(MyPlugin.INSTANCE,"test");
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        if (args.size() == 0) return;
        CommandSender sender = context.getSender();
        System.out.println(sender);
        Contact contact = sender.getSubject();
        int i = 1;
        if (sender instanceof MemberCommandSender) {
            Group group = (Group) sender.getSubject();
            SingleMessage memberMessage = args.get(0);
            Member member;
            if (memberMessage instanceof At) {
                member = group.getOrFail(((At) memberMessage).getTarget());
            } else {
                member = group.getOrFail(Long.parseLong(memberMessage.contentToString()));
            }
            sender = CommandSender.of(member);
        } else {
            i = 0;
            sender = CommandSender.of((User) contact,false);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (; i < args.size(); i++) {
            String s = args.get(i).contentToString();
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