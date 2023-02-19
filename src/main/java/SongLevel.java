import org.jetbrains.annotations.NotNull;

public class SongLevel implements Comparable<SongLevel>{
    String id;
    int level;
    int score;
    float acc;
    boolean fc;
    double rks;

    @Override
    public int compareTo(@NotNull SongLevel songLevel) {
        if (rks > songLevel.rks) {
            return 1;
        } else if (rks < songLevel.rks) {
            return -1;
        } else {
            return 0;
        }
    }
}