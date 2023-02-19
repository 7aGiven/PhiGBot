public class SongExpect {
    String id;
    int level;
    float acc;
    float expect;
    float get() {
        return expect - acc;
    }
}