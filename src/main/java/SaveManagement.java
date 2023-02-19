import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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

    static {
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    public static String getZipUrl(String session) throws Exception {
        HttpRequest.Builder builder = globalRequest.copy();
        builder.uri(new URI(save));
        builder.setHeader("X-LC-Session",session);
        HttpResponse<String> res = client.send(builder.build(),handler);
        String response = res.body();
        System.out.println(response);
        JSONObject json = JSON.parseObject(response);
        json = json.getJSONArray("results").getJSONObject(0);
        String zipUrl = json.getJSONObject("gameFile").getString("url");
        System.out.println(zipUrl);
        return zipUrl;
    }
    public static String[] update(String session) throws Exception{
        HttpRequest.Builder builder = globalRequest.copy();
        builder.uri(new URI(save));
        builder.setHeader("X-LC-Session",session);
        HttpResponse<String> res = client.send(builder.build(),handler);
        String response = res.body();
        System.out.println(response);
        JSONObject json = JSON.parseObject(response);
        JSONArray array = json.getJSONArray("results");
        json = array.getJSONObject(0);
        String zipUrl = json.getJSONObject("gameFile").getString("url");
        String summary;
        if (array.size() == 1) {
            summary = json.getString("summary");
        } else {
            summary = null;
        }
        System.out.println(zipUrl);
        System.out.println(summary);
        return new String[] {zipUrl,summary};
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
    public static byte[] modifySong(String zipUrl,String name,int level,int s,float a,boolean fc) throws Exception {
        return modify(zipUrl,"gameRecord", data -> {
            boolean exist = false;
            Score score = new Score(data);
            for (String id:score) {
                if (name.equals(id)) {
                    Song song = score.getSong();
                    if (song.get(level).score == 0) {
                        return null;
                    }
                    score.modifySong(level,s,a,fc);
                    data = score.getData();
                    exist = true;
                    break;
                }
            }
            if (!exist) {
                return null;
            }
            return data;
        });
    }
    public static byte[] addAvater(String zipUrl,String avater) throws Exception {
        return modify(zipUrl,"gameKey", data -> {
            GameKey gameKey = new GameKey(data);
            boolean exist = false;
            for (String key:gameKey) {
                if (key.equals(avater)) {
                    exist = true;
                    data = gameKey.getKey();
                    if (data[4] == 1) {
                        return null;
                    }
                    data = gameKey.modifyAvater();
                    break;
                }
            }
            if (!exist) {
                data = gameKey.addAvater(avater.getBytes());
            }
            return data;
        });
    }
    public static byte[] addCollection(String zipUrl,String collection) throws Exception {
        return modify(zipUrl,"gameKey", data -> {
            GameKey gameKey = new GameKey(data);
            boolean exist = false;
            for (String key:gameKey) {
                if (key.equals(collection)) {
                    exist = true;
                    data = gameKey.modifyCollection();
                    break;
                }
            }
            if (!exist) {
                data = gameKey.addCollection(collection.getBytes());
            }
            return data;
        });
    }
    public static byte[] challenge(String zipUrl,short score) throws Exception {
        return modify(zipUrl,"gameProgress", data -> {
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[2]);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putShort(score);
            byteBuffer.position(0);
            byteBuffer.get(data,6,2);
            return data;
        });
    }
    public static byte[] modify(String zipUrl,String type,ModifyCallback callback) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(new URI(zipUrl)).build();
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
                                data = callback.callback(data);
                                if (data == null) return null;
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

        return data;
    }
    public static void uploadZip(String session, byte[] data, short score) throws Exception {
        String response;
        HttpRequest.Builder template = globalRequest.copy().header("X-LC-Session",session);

        HttpRequest.Builder builder = template.copy();
        builder.uri(new URI(save));
        response = client.send(builder.build(),handler).body();
        System.out.println(response);
        JSONObject json = JSON.parseObject(response).getJSONArray("results").getJSONObject(0);
        String objectId = json.getString("objectId");
        String oldGameObjectId = json.getJSONObject("gameFile").getString("objectId");
        String userObjectId = json.getJSONObject("user").getString("objectId");
        String summary = json.getString("summary");
        System.out.println(summary);
        ByteBuffer byteBuffer = ByteBuffer.wrap(Base64.getDecoder().decode(summary));
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
        summary = Base64.getEncoder().encodeToString(byteBuffer.array());
        System.out.println(objectId);
        System.out.println(userObjectId);
        System.out.println(summary);



        builder = template.copy();
        builder.uri(new URI(fileTokens));
        builder.POST(HttpRequest.BodyPublishers.ofString(String.format("{\"name\":\".save\",\"__type\":\"File\",\"ACL\":{\"%s\":{\"read\":true,\"write\":true}}}",userObjectId)));
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
        builder.uri(new URI(String.format(baseUrl + "/classes/_GameSave/%s?",objectId)));
        builder.header("Content-Type","application/json");
        builder.PUT(HttpRequest.BodyPublishers.ofString(String.format("{\"summary\":\"%s\",\"modifiedAt\":{\"__type\":\"Date\",\"iso\":\"%sZ\"},\"gameFile\":{\"__type\":\"Pointer\",\"className\":\"_File\",\"objectId\":\"%s\"},\"ACL\":{\"%s\":{\"read\":true,\"write\":true}},\"user\":{\"__type\":\"Pointer\",\"className\":\"_User\",\"objectId\":\"%s\"}}",summary,format.format(new Date()),newGameObjectId,userObjectId,userObjectId)));
        response = client.send(builder.build(),HttpResponse.BodyHandlers.ofString()).body();
        System.out.println(response);



        builder = template.copy();
        builder.uri(new URI(String.format(baseUrl + "/files/%s",oldGameObjectId)));
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
}