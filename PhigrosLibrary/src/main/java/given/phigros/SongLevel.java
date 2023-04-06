package given.phigros;

public class SongLevel implements Comparable<SongLevel>{
    public String id;
    public int level;
    public int score;
    public float acc;
    public boolean fc;
    public float difficulty;
    public float rks;
    @Override
    public int compareTo(SongLevel songLevel) {return Double.compare(songLevel.rks, rks);}
}