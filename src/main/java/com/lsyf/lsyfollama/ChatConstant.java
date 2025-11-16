package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;

public class ChatConstant {
   public  static String MY_PLUGIN_SETTING= "MY_PLUGIN_SETTING";
   public  static String MY_MODEL_SETTING= "MY_MODEL_SETTING";
   public  static String MODEL= "qwen2.5-coder:0.5b";
   public  static String API_TEST= "http://113.45.171.202:11434";
   public  static String DEV_LAN= "java";
   public  static String TXT= "txt";
   public  static String XML= "xml";
   public  static String ChatToolWindow_ID= "LinlsyfAi";
   public  static String JAVA_FILE= "java";
   public  static String PYTHON_File= "py";
   public  static String TYPE_REPAIR= "repair";
   public  static String TYPE_repari= "repair";
   public  static String TYPE_code_generation= "code generation";
   public  static String OPEN_RIGHT_PANEL= "please  open right panel first";
   public  static String DEV_ING= "function is developing";

   public static String apiUrl = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_PLUGIN_SETTING,
           API_TEST // 默认值
   );
   public static String modelSetting = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_MODEL_SETTING,
           "qwen3:0.6b" // 默认值
   );
}
