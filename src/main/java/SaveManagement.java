import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class SaveManagement {
    private static final String baseUrl = "https://phigrosserver.pigeongames.cn/1.1";
    public static final HttpClient client = HttpClient.newHttpClient();
    private static final HttpRequest.Builder globalRequest = HttpRequest.newBuilder().header("X-LC-Id","rAK3FfdieFob2Nn8Am").header("X-LC-Key","Qr9AEqtuoSVS3zeD6iVbM4ZC0AtkJcQ89tywVyi0").header("User-Agent","LeanCloud-CSharp-SDK/1.0.3").header("Accept","application/json");
    private static final HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static final String fileTokens = baseUrl + "/fileTokens";
    private static final String fileCallback = baseUrl + "/fileCallback";
    private static final String save = baseUrl + "/classes/_GameSave";
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final MessageDigest md5;
    public final SaveModel saveModel;
    private long id;
    private final MyUser user;
    public byte[] data;

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public SaveManagement(long id,MyUser user) throws Exception {
        this.id = id;
        this.user = user;
        HttpRequest request = globalRequest.copy().header("X-LC-Session",user.session).uri(new URI(save)).build();
        String response = client.send(request,handler).body();
        System.out.println(response);
        JSONObject json = SaveManagement.save(user.session);
        SaveModel saveModel = new SaveModel();
        saveModel.summary = json.getString("summary");
        saveModel.objectId = json.getString("objectId");
        saveModel.userObjectId = json.getJSONObject("user").getString("objectId");
        json = json.getJSONObject("gameFile");
        saveModel.gameObjectId = json.getString("objectId");
        saveModel.updatedTime = json.getString("updatedAt");
        saveModel.checksum = json.getJSONObject("metaData").getString("_checksum");
        user.zipUrl = json.getString("url");
        this.saveModel = saveModel;
    }
    public static String getZipUrl(String session) throws Exception {
        return save(session).getJSONObject("gameFile").getString("url");
    }
    public static String update(MyUser user) throws Exception {
        JSONObject json = save(user.session);
        user.zipUrl =json.getJSONObject("gameFile").getString("url");
        System.out.println(user.zipUrl);
        return json.getString("summary");
    }
    private static JSONObject save(String session) throws Exception {
        HttpRequest request = globalRequest.copy().header("X-LC-Session",session).uri(new URI(save)).build();
        String response = client.send(request,handler).body();
        System.out.println(response);
        JSONArray array = JSON.parseObject(response).getJSONArray("results");
        if (array.size() != 1) {
            throw new Exception("存档有误，请修复存档");
        }
        return array.getJSONObject(0);
    }
    public static void delete(String session, String objectId) throws Exception {
        HttpRequest.Builder builder = globalRequest.copy();
        builder.DELETE();
        builder.uri(new URI(baseUrl + "/classes/_GameSave/" + objectId));
        builder.header("X-LC-Session",session);
        HttpResponse<String> res = client.send(builder.build(),handler);
        System.out.println(res.body());
    }
    public static void deleteFile(String session, String objectId) throws Exception {
        HttpRequest.Builder builder = globalRequest.copy();
        builder.DELETE();
        builder.uri(new URI(baseUrl + "/files/" + objectId));
        builder.header("X-LC-Session",session);
        HttpResponse<String> res = client.send(builder.build(),handler);
        System.out.println(res.body());
    }
    public void modify(String type, ModifyStrategy callback) throws Exception {
        md5.reset();
        if (!md5(data).equals(saveModel.checksum)) throw new Exception("文件校验不一致");
        Path path = MyPlugin.INSTANCE.resolveDataPath(String.format("backup/%d",id));
        if (!Files.isDirectory(path)) {
            Files.createDirectory(path);
        }
        path = MyPlugin.INSTANCE.resolveDataPath(String.format("backup/%d/%s.zip",id,saveModel.updatedTime));
        Files.write(path,data,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        HttpRequest request = HttpRequest.newBuilder(new URI(user.zipUrl)).build();
        byte[] data = client.send(request,HttpResponse.BodyHandlers.ofByteArray()).body();
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data)) {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available())) {
                try (ZipOutputStream zipWriter = new ZipOutputStream(outputStream)) {
                    try (ZipInputStream zipReader = new ZipInputStream(inputStream)) {
                        while (true) {
                            ZipEntry entry = zipReader.getNextEntry();
                            if (entry == null) {
                                break;
                            }
                            ZipEntry dEntry = new ZipEntry(entry);
                            dEntry.setCompressedSize(-1);
                            zipWriter.putNextEntry(dEntry);
                            if (entry.getName().equals(type)) {
                                zipReader.read();
                                data = zipReader.readAllBytes();
                                data = decrypt(data);
                                data = callback.apply(data);
                                data = encrypt(data);
                                zipWriter.write(1);
                            } else {
                                data = zipReader.readAllBytes();
                            }
                            zipWriter.write(data);
                        }
                        zipReader.closeEntry();
                    }
                }
                data = outputStream.toByteArray();
            }
        }
        this.data = data;
    }
    public void uploadZip(short score) throws Exception {
        String response;
        HttpRequest.Builder template = globalRequest.copy().header("X-LC-Session",user.session);

        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(saveModel.summary));
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        if (score == 0) {
            byteBuffer.position(1);
            score = byteBuffer.getShort();
            score++;
            if (score % 100 > 48) {
                score = (short) (score / 100 * 100 + 3);
            }
        }
        byteBuffer.position(1);
        byteBuffer.putShort(score);
        saveModel.summary = Base64.getEncoder().encodeToString(byteBuffer.array());
        System.out.println(saveModel.summary);



        md5.reset();
        HttpRequest.Builder builder = template.copy();
        builder.uri(new URI(fileTokens));
        builder.POST(HttpRequest.BodyPublishers.ofString(String.format("{\"name\":\".save\",\"__type\":\"File\",\"ACL\":{\"%s\":{\"read\":true,\"write\":true}},\"prefix\":\"gamesaves\",\"metaData\":{\"size\":%d,\"_checksum\":\"%s\",\"prefix\":\"gamesaves\"}}",saveModel.userObjectId,data.length, md5(data))));
        response = client.send(builder.build(),handler).body();
        String tokenKey = Base64.getEncoder().encodeToString(JSON.parseObject(response).getString("key").getBytes());
        String newGameObjectId = JSON.parseObject(response).getString("objectId");
        String authorization = "UpToken "+JSON.parseObject(response).getString("token");
        System.out.println(response);



        builder = HttpRequest.newBuilder(new URI(String.format("https://upload.qiniup.com/buckets/rAK3Ffdi/objects/%s/uploads", tokenKey)));
        builder.header("Authorization",authorization);
        builder.POST(HttpRequest.BodyPublishers.noBody());
        response = client.send(builder.build(),handler).body();
        String uploadId = JSON.parseObject(response).getString("uploadId");
        System.out.println(response);



        builder = HttpRequest.newBuilder(new URI(String.format("https://upload.qiniup.com/buckets/rAK3Ffdi/objects/%s/uploads/%s/1",tokenKey,uploadId)));
        builder.header("Authorization",authorization);
        builder.header("Content-Type","application/octet-stream");
        builder.PUT(HttpRequest.BodyPublishers.ofByteArray(data));
        response = client.send(builder.build(),handler).body();
        String etag = JSON.parseObject(response).getString("etag");
        System.out.println(response);



        builder = HttpRequest.newBuilder(new URI(String.format("https://upload.qiniup.com/buckets/rAK3Ffdi/objects/%s/uploads/%s",tokenKey,uploadId)));
        builder.header("Authorization",authorization);
        builder.header("Content-Type","application/json");
        builder.POST(HttpRequest.BodyPublishers.ofString(String.format("{\"parts\":[{\"partNumber\":1,\"etag\":\"%s\"}]}",etag)));
        client.send(builder.build(),handler);



        builder = template.copy();
        builder.uri(new URI(fileCallback));
        builder.header("Content-Type","application/json");
        builder.POST(HttpRequest.BodyPublishers.ofString(String.format("{\"result\":true,\"token\":\"%s\"}",tokenKey)));
        client.send(builder.build(),HttpResponse.BodyHandlers.discarding());



        builder = template.copy();
        builder.uri(new URI(String.format(baseUrl + "/classes/_GameSave/%s?",saveModel.objectId)));
        builder.header("Content-Type","application/json");
        builder.PUT(HttpRequest.BodyPublishers.ofString(String.format("{\"summary\":\"%s\",\"modifiedAt\":{\"__type\":\"Date\",\"iso\":\"%sZ\"},\"gameFile\":{\"__type\":\"Pointer\",\"className\":\"_File\",\"objectId\":\"%s\"},\"ACL\":{\"%s\":{\"read\":true,\"write\":true}},\"user\":{\"__type\":\"Pointer\",\"className\":\"_User\",\"objectId\":\"%s\"}}",saveModel.summary,format.format(new Date()),newGameObjectId,saveModel.userObjectId,saveModel.userObjectId)));
        response = client.send(builder.build(),HttpResponse.BodyHandlers.ofString()).body();
        System.out.println(response);



        builder = template.copy();
        builder.uri(new URI(String.format(baseUrl + "/files/%s",saveModel.gameObjectId)));
        builder.DELETE();
        response = client.send(builder.build(),HttpResponse.BodyHandlers.ofString()).body();
        System.out.println(response);

    }
    public static byte[] decrypt(byte[] data) throws Exception{
        byte[] key = new byte[] {-24,-106,-102,-46,-91,64,37,-101,-105,-111,-112,-117,-120,-26,-65,3,30,109,33,-107,110,-6,-42,-118,80,-35,85,-42,122,-80,-110,75};
        byte[] iv = new byte[] {42,79,-16,-118,-56,13,99,7,0,87,-59,-107,24,-56,50,83};
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key,"AES"),new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }

    public static byte[] encrypt(byte[] data) throws Exception {
        byte[] key = new byte[] {-24,-106,-102,-46,-91,64,37,-101,-105,-111,-112,-117,-120,-26,-65,3,30,109,33,-107,110,-6,-42,-118,80,-35,85,-42,122,-80,-110,75};
        byte[] iv = new byte[] {42,79,-16,-118,-56,13,99,7,0,87,-59,-107,24,-56,50,83};
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key,"AES"),new IvParameterSpec(iv));
        return cipher.doFinal(data);
    }
    private static String md5(byte[] data) {
        md5.reset();
        data = md5.digest(data);
        StringBuilder builder = new StringBuilder();
        for (byte b:data) {
            builder.append(Character.forDigit(b>>4&15,16));
            builder.append(Character.forDigit(b&15,16));
        }
        return builder.toString();
    }
}