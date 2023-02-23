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
import java.util.HashMap;

public class SenderFacade {
    public static HashMap<Long, MyUser> users = Util.readUser();
    private QuoteReply quoteReply;
    Contact subject;
    User user;
    MyUser myUser;
    public static SenderFacade getInstance(CommandContext context) {
        System.out.println("getInstance");
        return getInstance(context,true);
    }
    public static SenderFacade getInstance(CommandContext context,boolean b) {
        try {
            SenderFacade senderFacade = new SenderFacade();
            if (context.getOriginalMessage().contains(MessageSource.Key)) {
                senderFacade.quoteReply = new QuoteReply(context.getOriginalMessage());
            }
            CommandSender sender = context.getSender();
            senderFacade.user = sender.getUser();
            if (b) {
                senderFacade.myUser = users.get(senderFacade.user.getId());
                if (senderFacade.myUser == null) {
                    senderFacade.sendMessage("您尚未绑定SessionToken");
                    return null;
                }
            }
            if (sender instanceof OtherClientCommandSender) {
                senderFacade.subject = ((OtherClientCommandSenderOnMessageSync) sender).getFromEvent().getSubject();
            } else {
                senderFacade.subject = sender.getSubject();
            };
            return senderFacade;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void putUser(MyUser myUser) {
        users.put(user.getId(),myUser);
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
