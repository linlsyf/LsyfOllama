package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;

public class ChatConstant {
   public  static String MY_PLUGIN_SETTING= "MY_PLUGIN_SETTING";
   public  static String MY_MODEL_SETTING= "MY_MODEL_SETTING";
   public  static String MODEL= "qwen2.5-coder:0.5b";
   public  static String API_TEST= "http://www.linlsyf.cn:11434";

   public static String apiUrl = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_PLUGIN_SETTING,
           "请设置ip:11434" // 默认值
   );
   public static String modelSetting = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_MODEL_SETTING,
           ChatConstant.MODEL // 默认值
   );
}
