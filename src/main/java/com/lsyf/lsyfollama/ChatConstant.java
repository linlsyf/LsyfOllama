package com.lsyf.lsyfollama;

import com.intellij.ide.util.PropertiesComponent;

public class ChatConstant {
   public  static String MY_PLUGIN_SETTING= "MY_PLUGIN_SETTING";
   public  static String MY_MODEL_SETTING= "MY_MODEL_SETTING";
   public  static String MY_COMMIT_RECEIPT_SETTING= "MY_COMMIT_RECEIPT_SETTING";
   public  static String MY_COMMIT_REJECT_SETTING = "MY_COMMIT_REJECT_SETTING";
   public  static String MY_COMMIT_IS_AI_SETTING = "MY_COMMIT_IS_AI_SETTING";
   public  static String MODEL= "qwen2.5-coder:0.5b";
   public  static String API_TEST= "http://www.linlsyf.cn:11434";
   public  static String DEV_LAN= "java";
   public  static String SETTING_RECEPT= "service,mapper,controller";
   public  static String SETTING_RECEJCT= "@";
   public  static String TXT= "txt";
   public  static String XML= "xml";
   public  static String ChatToolWindow_ID= "LinlsyfAi";
   public  static String JAVA_FILE= "java";
   public  static String PYTHON_File= "py";
   public  static String TYPE_REPAIR= "repair";
   public  static String TYPE_code_generation= "code generation";
   public  static String OPEN_RIGHT_PANEL= "please  open right panel first";
   public  static String DEV_ING= "function is developing";
   public  static String MY_COMMIT_RECEIPT_VALUE= "";
   public  static String MY_COMMIT_REJECT_VALUE= "";
    public static boolean isAiModeDefault = false; // 初始为编辑模式

   public static String apiUrl = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_PLUGIN_SETTING,
           API_TEST // 默认值
   );
   public static String modelSetting = PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_MODEL_SETTING,
           MODEL // 默认值
   );
   public static String myCommitReceiptValue= PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_COMMIT_REJECT_SETTING,
           MY_COMMIT_RECEIPT_VALUE // 默认值
   );
   public static String myCommitJectValue= PropertiesComponent.getInstance().getValue(
           ChatConstant.MY_COMMIT_REJECT_VALUE,
           MY_COMMIT_REJECT_VALUE // 默认值
   );
   public static boolean isAiModeSave= PropertiesComponent.getInstance().getBoolean(
           ChatConstant.MY_COMMIT_IS_AI_SETTING,
           isAiModeDefault // 默认值
   );


}
