package com.dino;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    private static final Map<String, Image> imageCache = new HashMap<>();

    public static Image getImage(String fileName) {
        if (!imageCache.containsKey(fileName)) {
            try {
                // 改用 getResource 確保打包 jar 後仍能正確讀取資源
                String path = "/com/dino/assets/" + fileName;
                String url = ResourceManager.class.getResource(path).toExternalForm();
                imageCache.put(fileName, new Image(url));
            } catch (Exception e) {
                System.err.println("找不到圖片資源: " + fileName);
                // 若找不到圖片，可以考慮回傳預設的錯誤圖片以防崩潰，這裡先印出錯誤
            }
        }
        return imageCache.get(fileName);
    }
}