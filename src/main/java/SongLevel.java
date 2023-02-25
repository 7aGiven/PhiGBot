import org.jetbrains.annotations.NotNull;

public class SongLevel implements Comparable<SongLevel>{
    String id;
    int level;
    int score;
    float acc;
    boolean fc;
    double rks;
    public SongLevel() {}
    public SongLevel(double rks) {this.rks = rks;}
    public SongLevel(String id,int level,int score,float acc,boolean fc,double rks) {
        this.id = id;
        this.level = level;
        this.score = score;
        this.acc = acc;
        this.fc = fc;
        this.rks = rks;
    }
    @Override
    public int compareTo(@NotNull SongLevel songLevel) {return Double.compare(songLevel.rks, rks);}
}