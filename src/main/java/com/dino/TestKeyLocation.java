package com.dino; 

import javafx.scene.input.KeyCode; 

/**
 * 測試鍵盤按鍵 KeyCode 名稱的輔助類別。
 * 執行時會過濾並印出所有名稱中包含 "SHIFT" 的 JavaFX KeyCode 常數，便於確認特定按鍵常數的命名。
 */
public class TestKeyLocation { 
    public static void main(String[] args) { 
        for (KeyCode c : KeyCode.values()) { 
            if (c.name().contains("SHIFT")) {
                System.out.println(c.name()); 
            }
        } 
    } 
}

