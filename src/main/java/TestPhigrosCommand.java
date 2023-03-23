import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestPhigrosCommand extends JCompositeCommand {
    public static final TestPhigrosCommand INSTANCE = new TestPhigrosCommand();
    private String session;
    private TestPhigrosCommand() {
        super(MyPlugin.INSTANCE,"tp");
    }
    @SubCommand
    public void bind(CommandSender sender,String session) throws Exception {
        this.session = session;
    }
    @SubCommand
    public void b19(CommandSender sender, String zipUrl) throws Exception {
        byte[] data = PhigrosCompositeCommand.INSTANCE.extractZip(zipUrl, "gameRecord");
        data = new B19(data).b19Pic();
        ExternalResource ex = ExternalResource.create(data);
        Image image = sender.getSubject().uploadImage(ex);
        sender.sendMessage(image);
        ex.close();
    }
    @SubCommand
    public void deleteFile(CommandSender sender,String objectId) throws Exception {
        if (session == null) return;
        SaveManager.deleteFile(session,objectId);
    }
    @SubCommand
    public void delete(CommandSender sender,String objectId) throws Exception {
        if (session == null) return;
        SaveManager.delete(session,objectId);
    }
    @SubCommand
    @Description("备份历史")
    public void backupHistory(CommandSender sender) {
        if (sender == null) return;
        try {
            Path dirPath = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d",sender.getUser().getId())).toPath();
            if (!Files.isDirectory(dirPath)) {
                sender.sendMessage("无备份");
                return;
            }
            StringBuilder builder = new StringBuilder();
            try (Stream<Path> stream = Files.list(dirPath)) {
                stream.forEach(path -> {
                    String filename = path.getFileName().toString();
                    builder.append(filename.substring(0,filename.length()-5));
                    builder.append('\n');
                });
            }
            builder.deleteCharAt(builder.length()-1);
            sender.sendMessage(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    @SubCommand
    @Description("恢复备份历史")
    public void restoreHistory(CommandSender sender,String time) {
        if (sender == null) return;
        try {
            Path path = MyPlugin.INSTANCE.resolveDataFile(String.format("backup/%d/%s.zip",sender.getUser().getId(),time)).toPath();
            SaveManager saveManagement = new SaveManager(sender.getUser().getId(),getUser(sender));
            saveManagement.data = Files.readAllBytes(path);
            saveManagement.uploadZip(ModifyStrategyImpl.challengeScore);
            sender.sendMessage("恢复成功");
        } catch (Exception e) {
            e.printStackTrace();
            sender.sendMessage(e.toString());
        }
    }
    private GameUser getUser(CommandSender sender) {
        GameUser user = DAO.INSTANCE.users.get(sender.getUser().getId());
        if (user == null) {
            sender.sendMessage("您尚未绑定SessionToken");
        }
        return user;
    }
}