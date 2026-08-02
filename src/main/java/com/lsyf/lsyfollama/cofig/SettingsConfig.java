package com.lsyf.lsyfollama.cofig;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.ui.view.SettingView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsConfig implements Configurable {

  SettingView settingView;
  private Thread appThread;

  @Override
  public @Nullable JComponent createComponent() {

    settingView = new SettingView();
    settingView.init();
    initViewValue();
    // 添加按钮点击事件
    settingView.getToggleButton().addActionListener(e -> toggleState());
    return settingView.getMainPanel();
  }

  private void toggleState() {
    boolean state = PropertiesComponent.getInstance().getBoolean(ChatConstant.MY_COMMIT_IS_AI_SETTING, false);

    boolean stateCurrent = !state;

    // 可以添加更多状态切换逻辑
//    initViewValue();
    PropertiesComponent.getInstance().setValue(ChatConstant.MY_COMMIT_IS_AI_SETTING, stateCurrent);
     stateCurrent = PropertiesComponent.getInstance().getBoolean(ChatConstant.MY_COMMIT_IS_AI_SETTING, false);
    String stateStr = settingView.getIsAiDesc(stateCurrent);
    settingView.getToggleButton().setText(stateStr);
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return Configurable.super.getPreferredFocusedComponent();
  }

  @Override
  public boolean isModified() {
    // 判断配置是否被修改
    return true;
//        return !inputField.getText().equals(getCurrentValue());
  }

  @Override
  public void apply() {
    // 保存配置到 PropertiesComponent
    String IP = settingView.getInputField().getText();
    String MODE = settingView.getModelField().getText();
    String commitReceipt = settingView.getCommitReceiptFied().getText();
    String commitReject = settingView.getCommitRejectFied().getText();
    PropertiesComponent.getInstance().setValue(ChatConstant.MY_PLUGIN_SETTING, IP);
    PropertiesComponent.getInstance().setValue(ChatConstant.MY_MODEL_SETTING, MODE);
    PropertiesComponent.getInstance().setValue(ChatConstant.MY_COMMIT_RECEIPT_SETTING, commitReceipt);
    PropertiesComponent.getInstance().setValue(ChatConstant.MY_COMMIT_REJECT_SETTING, commitReject);

    ChatConstant.apiUrl = IP;
    ChatConstant.modelSetting = MODE;
    ChatConstant.myCommitReceiptValue = commitReceipt;
    ChatConstant.myCommitJectValue = commitReject;
  }

  @Override
  public void reset() {
    // 重置为默认值或加载已保存的配置
    ChatConstant.apiUrl = ChatConstant.apiUrl;
    ChatConstant.modelSetting = ChatConstant.modelSetting;
    ChatConstant.myCommitReceiptValue = ChatConstant.MY_COMMIT_RECEIPT_VALUE;
    ChatConstant.myCommitJectValue = ChatConstant.MY_COMMIT_REJECT_VALUE;
    ChatConstant.isAiModeSave = ChatConstant.isAiModeDefault;
    initViewValue();

  }

  public void initViewValue() {

    settingView.getInputField().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_PLUGIN_SETTING, ChatConstant.API_TEST));
    settingView.getModelField().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_MODEL_SETTING, ChatConstant.MODEL));
    settingView.getCommitReceiptFied().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_COMMIT_RECEIPT_SETTING, ChatConstant.SETTING_RECEPT));
    settingView.getCommitRejectFied().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_COMMIT_REJECT_SETTING, ChatConstant.SETTING_RECEJCT));

    boolean state = PropertiesComponent.getInstance().getBoolean(ChatConstant.MY_COMMIT_IS_AI_SETTING, false);
    String stateStr = settingView.getIsAiDesc(state);
    settingView.getToggleButton().setText(stateStr);

  }

  @Override
  public void disposeUIResources() {
    Configurable.super.disposeUIResources();
  }

  @Override
  public void cancel() {
    Configurable.super.cancel();
  }

  @Override
  public String getDisplayName() {
    return "lsyfSettings";
  }


}