import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class B19 {
    static HashMap<String,SongInfo> info;
    static final String[] levels = new String[]{"EZ","HD","IN","AT"};
    private SongLevel[] b19 = new SongLevel[19];
    private SongLevel phi = new SongLevel();
    B19(byte[] data) {
        Score score = new Score(data);
        SongLevel songLevel;
        double rks;
        int num = 0;
        int index;
        for (String id:score) {
            Song song = score.getSong();
            double[] doubleLevel = info.get(id).level;
            for (int i = 0; i < 4; i++) {
                if (song.get(i).score != 0 && song.get(i).acc >= 70) {
                    songLevel = new SongLevel();
                    rks = Math.pow((song.get(i).acc - 55) / 45,2) * doubleLevel[i];
                    if (song.get(i).score == 1000000 && rks > phi.rks) {
                        songLevel.id = id;
                        songLevel.level = i;
                        songLevel.score = song.get(i).score;
                        songLevel.acc = song.get(i).acc;
                        songLevel.fc = song.get(i).fc;
                        songLevel.rks = rks;
                        phi = songLevel;
                    }
                    if (num < 19) {
                        songLevel.id = id;
                        songLevel.level = i;
                        songLevel.score = song.get(i).score;
                        songLevel.acc = song.get(i).acc;
                        songLevel.fc = song.get(i).fc;
                        songLevel.rks = rks;
                        b19[num] = songLevel;
                        num++;
                        continue;
                    }
                    index = min();
                    if (rks > b19[index].rks) {
                        songLevel.id = id;
                        songLevel.level = i;
                        songLevel.score = song.get(i).score;
                        songLevel.acc = song.get(i).acc;
                        songLevel.fc = song.get(i).fc;
                        songLevel.rks = rks;
                        b19[index] = songLevel;
                    }
                }
            }
        }
        Arrays.sort(b19);
    }
    public void b19Pic() throws Exception {
        StringBuilder builder = new StringBuilder();
        String x;
        builder.append('{');
        for (SongLevel songLevel:b19) {;
            if (songLevel.score == 0) {
                break;
            }
            x = String.format("'%s.0.Record.%s':{'s':%s,'a':%s,'c':%s},",songLevel.id,levels[songLevel.level],songLevel.score,songLevel.acc,songLevel.fc?1:0);
            builder.append(x);
        }
        x = String.format("'%s.0.Record.%s':{'s':%s,'a':%s,'c':%s},",phi.id,levels[phi.level],phi.score,phi.acc,phi.fc?1:0);
        builder.append(x);
        builder.append('}');
        python(builder.toString());
    }
    private synchronized static void python(String dict) throws Exception {
        try (FileWriter writer = new FileWriter(MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/score.dict"))) {
            writer.write(dict);
        }
        Process p = Runtime.getRuntime().exec("python3 " + MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/xx.py").getAbsolutePath());
        p.waitFor();
    }
    public ForwardMessage expectCalc(User user, byte[] data) {
        double min = b19[0].rks;
        Score score = new Score(data);
        ForwardMessageBuilder builder = new ForwardMessageBuilder(user);
        ArrayList<SongExpect> arrayList = new ArrayList<>();
        for (String id:score) {
            Song song = score.getSong();
            SongInfo songInfo = info.get(id);
            double[] doubleLevel = songInfo.level;
            for (int i = 0; i < 4; i++) {
                if (doubleLevel[i] > min) {
                    float expect = (float) (Math.sqrt(min/doubleLevel[i])*45+55);
                    if (expect > song.get(i).acc) {
                        SongExpect songExpect = new SongExpect();
                        songExpect.id = id;
                        songExpect.level = i;
                        songExpect.acc = song.get(i).acc;
                        songExpect.expect = expect;
                        arrayList.add(songExpect);
                    }
                }
            }
        }
        arrayList.sort(Comparator.comparing(SongExpect::get));
        for (SongExpect songExpect:arrayList) {
            builder.add(user,new PlainText(String.format("%s.%s\nnow：%.2f\nexpect：%.2f",info.get(songExpect.id).name,levels[songExpect.level],songExpect.acc,songExpect.expect)));
        }
        return builder.build();
    }
    private int min() {
        int index = -1;
        double min = 17;
        for (int i = 0; i < 19; i++) {
            if (b19[i].rks < min) {
                index = i;
                min = b19[i].rks;
            }
        }
        return index;
    }
    public byte[] tt() throws IOException {
        double rks = 0;
        for (int i = 0; i < 19; i++) {
            rks += b19[i].rks;
        }
        rks += phi.rks;
        rks /= 20;
        BufferedImage background = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("phi_blur.png"));
        Graphics2D g2d = background.createGraphics();
        Font defaultFont = g2d.getFont();
        g2d.setFont(defaultFont.deriveFont(30f));
        g2d.drawString("Coded by 恐怖蚊子",30,30);
        g2d.setFont(defaultFont.deriveFont(70f));
        g2d.drawString(String.format("Total RKS：%.3f",rks),3329+100,158+70);
        SongLevel songLevel;
        int width = 2048;
        int height = 170;
        int offset = 460;
        int blank = 10*2;
        int scaledHeight = height-2*blank;
        int scaledWidth = scaledHeight/9*16;
        BufferedImage rank;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 10; y++) {
                if (x == 0 && y == 0) {
                    songLevel = phi;
                } else {
                    songLevel = b19[19-10*x-y];
                }
                g2d.drawImage(ImageIO.read(MyPlugin.INSTANCE.resolveDataFile(String.format("illustration/%s.png",songLevel.id))).getScaledInstance(scaledWidth,scaledHeight,Image.SCALE_DEFAULT),x*width+blank,y*height+offset,null);
                if (songLevel.fc) {
                    if (songLevel.score == 1000000) {
                        rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("phi15phi.png"));
                    } else {
                        rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("V15FC.png"));
                    }
                } else if (songLevel.score >= 960000) {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("V15V.png"));
                } else if (songLevel.score >= 920000) {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("S15S.png"));
                } else if (songLevel.score >= 880000) {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("A15A.png"));
                } else if (songLevel.score >= 820000) {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("B15B.png"));
                } else if (songLevel.score >= 700000) {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("C15C.png"));
                } else {
                    rank = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("F15F.png"));
                }
                g2d.drawImage(rank.getScaledInstance(scaledHeight-16,scaledHeight-16,Image.SCALE_DEFAULT),x*width+blank+scaledWidth,y*height+offset,null);
            }
        }
        g2d.dispose();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(background,"png",byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}