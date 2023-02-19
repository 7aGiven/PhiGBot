import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.OtherClientCommandSender;
import net.mamoe.mirai.console.command.OtherClientCommandSenderOnMessageSync;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.MessageSource;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;

public class Sender {
    QuoteReply quoteReply;
    Contact subject;
    User user;
    public Sender(CommandContext context) {
        if (context.getOriginalMessage().contains(MessageSource.Key)) {
            quoteReply = new QuoteReply(context.getOriginalMessage());
        }
        CommandSender sender = context.getSender();
        user = sender.getUser();
        System.out.println(user);
        if (sender instanceof OtherClientCommandSender) {
            subject = ((OtherClientCommandSenderOnMessageSync) sender).getFromEvent().getSubject();
        } else {
            subject = sender.getSubject();
        }
    }
    public void sendMessage(String message) {
        if (quoteReply == null) {
            subject.sendMessage(message);
        } else {
            subject.sendMessage(quoteReply.plus(message));
        }
    }
    public void sendMessage(ForwardMessage message) {
        subject.sendMessage(message);
    }
    public void sendImage(ExternalResource externalResource) throws IOException {
        subject.sendMessage(subject.uploadImage(externalResource));
        externalResource.close();
    }
}
