public class SongExpect {
    String name;
    int level;
    float acc;
    float expect;
    float get() {
        return expect - acc;
    }
}