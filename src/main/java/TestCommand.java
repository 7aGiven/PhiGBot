import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.OtherClientCommandSenderOnMessageSync;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

public final class TestCommand extends JRawCommand {
    public TestCommand() {
        super(MyPlugin.INSTANCE,"test");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender,@NotNull MessageChain args) {
        if (args.size() == 0) return;
        Contact contact = ((OtherClientCommandSenderOnMessageSync) sender).getFromEvent().getSubject();
        if (args.size() == 1) {
            if (args.get(0).contentEquals("help",true)) {
                if (contact instanceof Group) {
                    sender = CommandSender.of((Member) ((Group) contact).getBotAsMember());
                } else {
                    sender = CommandSender.of((User) contact,false);
                }
                CommandManager.INSTANCE.executeCommand(sender,new PlainText("/help"),true);
            } else if (args.get(0).contentEquals("read",true)) {
                Util.readLevel();
                Util.readUser();
            } else if (args.get(0).contentEquals("write",true)) {
                Util.writeUser();
            }
        } else {
            int i = 1;
            if (contact instanceof Group) {
                Group group = (Group) contact;
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
            System.out.println(sender);
            CommandManager.INSTANCE.executeCommand(sender,new PlainText(stringBuilder.toString()),true);
        }
    }
}