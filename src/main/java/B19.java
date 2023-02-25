import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.data.ForwardMessage;
import net.mamoe.mirai.message.data.ForwardMessageBuilder;
import net.mamoe.mirai.message.data.PlainText;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class B19 {
    public static final HashMap<String,SongInfo> info = Util.readLevel();
    public static final String[] levels = new String[]{"EZ","HD","IN","AT"};
    private static final ReentrantLock lock = new ReentrantLock();
    private final SongLevel[] b19 = new SongLevel[20];
    private static final Color[] colorLevel = new Color[] {Color.GREEN,Color.BLUE,Color.RED,Color.GRAY};
    private static Font defaultFont;
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日hh时");
    B19(byte[] data) throws Exception {
        SongLevel phi = new SongLevel();
        Score score = new Score(data);
        int num = 40;
        for (String id:score) {
            Song song = score.getSong();
            double[] doubleLevel = info.get(id).level;
            for (int i = 0; i < 4; i++) {
                if (song.get(i).score != 0 && song.get(i).acc >= 70) {
                    double rks = Math.pow((song.get(i).acc - 55) / 45,2) * doubleLevel[i];
                    if (song.get(i).score == 1000000 && rks > phi.rks) {
                        phi = new SongLevel(id,i,song.get(i).score,song.get(i).acc,song.get(i).fc,rks);
                    }
                    if (num > 20) {
                        b19[40-num] = new SongLevel(id,i,song.get(i).score,song.get(i).acc,song.get(i).fc,rks);
                        num--;
                        continue;
                    }
                    num = min();
                    if (rks > b19[num].rks) {
                        b19[num] = new SongLevel(id,i,song.get(i).score,song.get(i).acc,song.get(i).fc,rks);
                    }
                }
            }
        }
        b19[19] = new SongLevel(17);
        Arrays.sort(b19);
        b19[0] = phi;
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
    public void b19Pic() throws Exception {
        StringBuilder builder = new StringBuilder();
        String x;
        builder.append('{');
        for (SongLevel songLevel:b19) {;
            if (songLevel.score == 0) break;
            x = String.format("'%s.0.Record.%s':{'s':%s,'a':%s,'c':%s},",songLevel.id,levels[songLevel.level],songLevel.score,songLevel.acc,songLevel.fc?1:0);
            builder.append(x);
        }
        builder.append('}');
        lock.lock();
        try {
            try (FileWriter writer = new FileWriter(MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/score.dict"))) {
                writer.write(builder.toString());
            }
            Process p = Runtime.getRuntime().exec("python3 " + MyPlugin.INSTANCE.resolveDataFile("../../../rks-calc-1.1.1/xx.py").getAbsolutePath());
            p.waitFor();
        } finally {
            lock.unlock();
        }
    }
    public ForwardMessage expectCalc(User user, byte[] data) {
        double min = b19[19].rks;
        Score score = new Score(data);
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
                        songExpect.name = songInfo.name;
                        songExpect.level = i;
                        songExpect.acc = song.get(i).acc;
                        songExpect.expect = expect;
                        arrayList.add(songExpect);
                    }
                }
            }
        }
        arrayList.sort(Comparator.comparing(SongExpect::get));
        ForwardMessageBuilder builder = new ForwardMessageBuilder(user);
        for (SongExpect songExpect:arrayList) {
            builder.add(user,new PlainText(String.format("%s.%s\nnow：%.2f\nexpect：%.2f",songExpect.name,levels[songExpect.level],songExpect.acc,songExpect.expect)));
        }
        return builder.build();
    }
    public byte[] tt() throws Exception {
        if (defaultFont == null) {
            defaultFont = Font.createFont(Font.TRUETYPE_FONT,MyPlugin.INSTANCE.resolveDataFile("ukai.ttc")).deriveFont(40f);
        }
        double rks = 0;
        for (int i = 0; i < 20; i++) {
            rks += b19[i].rks;
        }
        rks /= 20;
        int imageWidth = 1600;
        int imageHeight = 2300;
        int offset = 300;
        Image backgroundImage = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile("arc.png")).getScaledInstance(imageWidth,imageHeight,Image.SCALE_DEFAULT);
        BufferedImage bufferedImage;
        BufferedImage background = new BufferedImage(imageWidth,imageHeight,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = background.createGraphics();
        Composite defaultComposite = g2d.getComposite();
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,0.6f);
        g2d.drawImage(backgroundImage,0,0,null);
        g2d.setFont(defaultFont.deriveFont(70f));
        g2d.setColor(Color.BLACK);
        g2d.setComposite(alphaComposite);
        g2d.fillRect(50,50,450,140);
        g2d.fillRect(imageWidth/2+50,offset-150,400,100);
        g2d.setColor(Color.WHITE);
        g2d.setComposite(defaultComposite);
        g2d.drawString(String.format("RKS：%.3f",rks),imageWidth/2+60,offset-70);
        g2d.setFont(defaultFont.deriveFont(50f));
        g2d.drawString(format.format(new Date()),50,110);
        g2d.setFont(defaultFont);
        SongLevel songLevel;
        int illustrationWidth = 300;
        int height = 158;
        int yBlank = 20;
        int xBlank = 100;
        int[] xs = new int[] {xBlank,(imageWidth+xBlank)/2};
        String rank;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 10; y++) {
                Point point = new Point(xs[x],offset+y*(height+yBlank));
                songLevel = b19[10*x+y];
                BufferedImage illustration = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile(String.format("illustration/%s.png",songLevel.id)));
                g2d.drawImage(illustration,point.x,point.y,null);
                g2d.setColor(colorLevel[songLevel.level]);
                g2d.fillRect(point.x,point.y+height-40,90,40);
                g2d.setColor(Color.BLACK);
                g2d.setComposite(alphaComposite);
                g2d.fillRect(point.x+illustrationWidth+10,point.y,height+180,height);
                g2d.setComposite(defaultComposite);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.format("%.1f",info.get(songLevel.id).level[songLevel.level]),point.x,point.y+height-5);
                if (songLevel.fc) {
                    if (songLevel.score == 1000000) {
                        rank = "phi15phi.png";
                    } else {
                        rank = "V15FC.png";
                    }
                } else if (songLevel.score >= 960000) {
                    rank = "V15V.png";
                } else if (songLevel.score >= 920000) {
                    rank = "S15S.png";
                } else if (songLevel.score >= 880000) {
                    rank = "A15A.png";
                } else if (songLevel.score >= 820000) {
                    rank = "B15B.png";
                } else if (songLevel.score >= 700000) {
                    rank = "C15C.png";
                } else {
                    rank = "F15F.png";
                }
                bufferedImage = ImageIO.read(MyPlugin.INSTANCE.resolveDataFile(rank));
                g2d.drawImage(bufferedImage.getScaledInstance(height,height,Image.SCALE_DEFAULT),point.x+illustrationWidth,point.y,null);
                g2d.drawString(String.format(" %.3f",songLevel.rks),point.x+illustrationWidth+height,point.y+40);
                g2d.setFont(defaultFont.deriveFont(50f));
                String score = String.format("%d",songLevel.score);
                if (score.length() == 6) score = " " + score;
                g2d.drawString(score,point.x+illustrationWidth+height,point.y+height/3+45);
                g2d.setFont(defaultFont);
                g2d.drawString(String.format(" %.3f%%",songLevel.acc),point.x+illustrationWidth+height,point.y+height*2/3+40);
            }
        }
        g2d.setComposite(defaultComposite);
        g2d.setColor(Color.BLACK);
        g2d.fillRect(imageWidth/2-150,offset+10*(height+yBlank)+yBlank,300,50);
        g2d.setColor(Color.WHITE);
        g2d.drawString("绘图：恐怖蚊子",imageWidth/2-140,offset+10*(height+yBlank)+yBlank+40);
        g2d.dispose();
        byte[] data;
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ImageIO.write(background,"png",byteArrayOutputStream);
            data = byteArrayOutputStream.toByteArray();
        }
        return data;
    }
}