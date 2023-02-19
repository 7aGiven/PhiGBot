public class Song {
    String id;
    _SongLevel EZ = new _SongLevel();
    _SongLevel HD = new _SongLevel();
    _SongLevel IN = new _SongLevel();
    _SongLevel AT = new _SongLevel();

    public _SongLevel get(int index) {
        switch (index) {
            case 0:
                return EZ;
            case 1:
                return HD;
            case 2:
                return IN;
            case 3:
                return AT;
        }
        return null;
    }
}
class _SongLevel {
    int score = 0;
    float acc;
    boolean fc;
}