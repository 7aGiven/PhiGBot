import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class IllustrationCallback implements Callable<BufferedImage> {
    private final String id;
    IllustrationCallback(String id) {
        this.id = id;
    }
    @Override
    public BufferedImage call() throws Exception {
        return ImageIO.read(MyPlugin.INSTANCE.resolveDataFile(String.format("illustration/%s.png",id)));
    }
}
