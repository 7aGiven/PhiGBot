import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.IOException;

public class SenderFacade {
    private final QuoteReply quoteReply;
    public final Contact subject;
    public final User user;
    GameUser myUser;
    public SenderFacade(CommandContext context) throws Exception {
        this(context,true);
    }
    public SenderFacade(CommandContext context,boolean b) throws Exception {
        quoteReply = new QuoteReply(context.getOriginalMessage());
        user = context.getSender().getUser();
        if (b) {
            myUser = DAO.INSTANCE.users.get(user.getId());
            if (myUser == null) throw new Exception("您尚未绑定SessionToken");
        }
        subject = context.getSender().getSubject();
    }
    public void putUser(GameUser myUser) {
        DAO.INSTANCE.users.put(user.getId(),myUser);
    }
    public void sendMessage(String message) {
        subject.sendMessage(quoteReply.plus(message));
    }
    public void sendMessage(ForwardMessage message) {
        subject.sendMessage(message);
    }
    public void sendImage(ExternalResource externalResource) throws IOException {
        subject.sendMessage(subject.uploadImage(externalResource));
        externalResource.close();
    }
}
