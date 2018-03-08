package com.usiellau.conferenceol.JCWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JCConfUtils {

    public static class SubViewRect {
        public int x;
        public int y;
        public int width;
        public int height;

        public SubViewRect(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    public static List<SubViewRect> caclSubViewRect(int width, int height, int totalNum) {
        List<SubViewRect> l = new ArrayList<>();
        if (totalNum == 0) {
            return l;
        }
        int row = 0;
        int column = 0;
        if (totalNum <= 2) {
            row = 1;
            column = totalNum;
        } else if (totalNum <= 4) {
            row = 2;
            column = 2;
        } else if (totalNum <= 8) {
            row = 2;
            column = 4;
        } else if (totalNum <= 12) {
            row = 2;
            column = 6;
        } else if (totalNum <= 15) {
            row = 3;
            column = 5;
        } else if (totalNum <= 18) {
            row = 3;
            column = 6;
        } else if (totalNum <= 21) {
            row = 3;
            column = 7;
        }

        int perWidth = width / column;
        int perHeight = height / row;
        for (int i = 0; i < totalNum; i++) {
            l.add(new SubViewRect(perWidth * (i % column), perHeight * (i / column), perWidth, perHeight));
        }
        return l;
    }

    public static String qiniuRecordParam(boolean video, String bucketName, String secretKey, String accessKey, String fileName) {
        try {
            JSONObject object = new JSONObject();
            object.put("MtcConfIsVideoKey", video);
            JSONObject storageObj = new JSONObject();
            storageObj.put("Protocol", "qiniu");
            storageObj.put("BucketName", bucketName);
            storageObj.put("SecretKey", secretKey);
            storageObj.put("AccessKey", accessKey);
            storageObj.put("FileKey", fileName);
            object.put("Storage", storageObj);
            return object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "";
    }

}