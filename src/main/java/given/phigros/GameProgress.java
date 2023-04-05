package given.phigros;

class GameProgress {
    private byte[] data;
    GameProgress(byte[] data) {
        this.data = data;
    }
    short getChallenge() {
        return (short) (data[7] * 256 + data[6]);
    }
    void setChallenge(short score) {
        if (score >= 600)
            throw new RuntimeException("score不允许超过600");
        data[6] = (byte) (score % 256);
        data[7] = (byte) (score /256);
    }
    int getGameData() {
        int index = 8;
        int sum = 0;
        int num;
        for (int i = 0; i < 5; i++) {
            num = Util.readVarShort(data, index);
            index += num < 128 ? 1 : 2;
            sum += num * Math.pow(1024, i);
        }
        return sum;
    }
    void setGameData(short MB) {
        int index = 8;
        for (int i = 0; i < 5; i++)
            index += Util.getBit(data[index], 7) ? 2 : 1;
        byte[] bytes;
        if (MB < 128)
            bytes = new byte[] {0, (byte) MB, 0, 0, 0};
        else
            bytes = new byte[] {0, (byte) (MB % 128 + 128), (byte) (MB / 128), 0, 0, 0};
        data = Util.modifyBytes(data, 8, index - 8, bytes);
    }
    byte[] getData() {
        return data;
    }
}