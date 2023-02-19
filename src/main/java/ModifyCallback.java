import java.io.IOException;

public interface ModifyCallback {
    public byte[] callback(byte[] data) throws IOException;
}
