package com.lsyf.lsyfollama.business;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.business.genCode.CodeGen;
import com.lsyf.lsyfollama.business.genCode.CodeQuery;
import com.lsyf.lsyfollama.constant.Contant;

public class ContextMenuLogic {

  public static void consumerContextMenu(AnActionEvent e, AnAction action) {
    String actionId = ActionManager.getInstance().getId(action);
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    if (psiFile != null) {
      FileType fileType = psiFile.getFileType();
      String languageId = fileType.getName(); // 如 "JAVA", "XML"
      if (languageId.contains(ChatConstant.JAVA_FILE) || languageId.contains(ChatConstant.PYTHON_File)) {
        ChatConstant.DEV_LAN = languageId;
      }
    }

    if (Contant.genCode.equals(actionId)) {
      String actionDesc = "生成" + ChatConstant.DEV_LAN + " 代码：\n";

      CodeGen.genCode(e, action, actionDesc);
    } else if (Contant.linlsyfQuery.equals(actionId)) {
      CodeQuery.query(e, action);
    } else if (Contant.apiTest.equals(actionId)) {
//            String actionDesc="修复"+ ChatConstant.DEV_LAN+" 代码的错误：\n" ;
//            genCode(e,action,actionDesc);
      Messages.showInfoMessage(ChatConstant.OPEN_RIGHT_PANEL, ChatConstant.DEV_ING);

    }
  }


}
