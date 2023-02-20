import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.OtherClientCommandSender;
import net.mamoe.mirai.console.command.OtherClientCommandSenderOnMessageSync;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

public class TestPhigrosCommand extends JCompositeCommand {
    public static final TestPhigrosCommand INSTANCE = new TestPhigrosCommand();
    private TestPhigrosCommand() {
        super(MyPlugin.INSTANCE,"tp");
    }
    @SubCommand
    public void b19(CommandSender sender, String zipUrl) throws Exception {
        sender = getCommandSender(sender);
        byte[] data = MyCompositeCommand.INSTANCE.extractZip(zipUrl, "gameRecord");
        if (data == null) {
            sender.sendMessage("文件不存在");
        }
        data = SaveManagement.decrypt(data);
        new B19(data).b19Pic();
        ExternalResource ex = ExternalResource.create(MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/xx.png"));
        Image img = sender.getSubject().uploadImage(ex);
        sender.sendMessage(img);
        ex.close();
    }
    @SubCommand
    public void deleteFile(CommandSender sender,String objectId) throws Exception {
        MyUser user = getUser(sender);
        if (user == null) return;
        SaveManagement.deleteFile(user.session,objectId);
    }
    @SubCommand
    public void delete(CommandSender sender,String objectId) throws Exception {
        MyUser user = getUser(sender);
        if (user == null) return;
        SaveManagement.delete(user.session,objectId);
    }
    private MyUser getUser(CommandSender sender) {
        MyUser user = SenderFacade.users.get(sender.getUser().getId());
        if (user == null) {
            sender.sendMessage("您尚未绑定SessionToken");
        }
        return user;
    }
    private CommandSender getCommandSender(CommandSender sender) {
        if (sender instanceof OtherClientCommandSender) {
            Group group = (Group) ((OtherClientCommandSenderOnMessageSync) sender).getFromEvent().getSubject();
            sender = CommandSender.of((Member) group.getBotAsMember());
        }
        return sender;
    }
}