package com.dino;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * 資源管理員類別，負責集中載入與快取圖片資源，避免重複讀取磁碟以提升效能。
 */
public class ResourceManager {
    // 快取圖片的 Map，Key 為檔名，Value 為 JavaFX Image 物件
    private static final Map<String, Image> imageCache = new HashMap<>();

    /**
     * 獲取指定的圖片資源。若快取中不存在則嘗試從 assets 資料夾載入並存入快取。
     * @param fileName 圖片檔名（包含副檔名，如 dino_run1.png）
     * @return 載入成功的 Image 物件，若失敗則可能回傳 null
     */
    public static Image getImage(String fileName) {
        if (!imageCache.containsKey(fileName)) {
            try {
                // 使用類別載入器路徑，以確保打包成 jar 後仍能正確定位資源檔
                String path = "/com/dino/assets/" + fileName;
                String url = ResourceManager.class.getResource(path).toExternalForm();
                imageCache.put(fileName, new Image(url));
            } catch (Exception e) {
                System.err.println("找不到圖片資源: " + fileName);
                // 若找不到圖片，此處輸出錯誤紀錄以利除錯
            }
        }
        return imageCache.get(fileName);
    }
}