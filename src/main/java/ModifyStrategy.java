import java.io.IOException;

public interface ModifyStrategy {
    byte[] callback(byte[] data) throws IOException;
}
