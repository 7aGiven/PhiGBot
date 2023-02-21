@FunctionalInterface
public interface ModifyStrategy {
    byte[] apply(byte[] data) throws Exception;
}
