package given.phigros;

public class SongExpect implements Comparable<SongExpect> {
    public String name;
    public int level;
    public float acc;
    public float expect;
    SongExpect(String name,int level,float acc,float expect) {
        this.name = name;
        this.level = level;
        this.acc = acc;
        this.expect = expect;
    }
    @Override
    public int compareTo(SongExpect songExpect) {
        return Float.compare(expect - acc, songExpect.expect - songExpect.acc);
    }
}