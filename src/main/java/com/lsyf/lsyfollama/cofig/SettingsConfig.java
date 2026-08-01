package com.lsyf.lsyfollama.cofig;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.lsyf.lsyfollama.ChatConstant;
import com.lsyf.lsyfollama.ui.view.SettingView;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsConfig implements Configurable {

    SettingView settingView;


    @Override
    public @Nullable JComponent createComponent() {

        settingView = new SettingView();
        settingView.init();
        // 添加按钮点击事件
        settingView.getToggleButton().addActionListener(e -> toggleState());
        return settingView.getMainPanel();
    }

    private void toggleState() {
        ChatConstant.isAiModeDefault = !ChatConstant.isAiModeDefault;

        // 可以添加更多状态切换逻辑
        settingView.getToggleText();
        PropertiesComponent.getInstance().setValue(ChatConstant.MY_COMMIT_IS_AI_SETTING, ChatConstant.isAiModeDefault);


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
        settingView.getInputField().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_PLUGIN_SETTING, "输入api地址端口"));
        settingView.getModelField().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_MODEL_SETTING, "输入模型"));
        settingView.getCommitReceiptFied().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_COMMIT_RECEIPT_SETTING, "提交总结内容关键字"));
        settingView.getCommitRejectFied().setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_COMMIT_REJECT_SETTING, "提交总结内容关键字"));
        ChatConstant.apiUrl = ChatConstant.apiUrl;
        ChatConstant.modelSetting = ChatConstant.modelSetting;
        ChatConstant.myCommitReceiptValue = ChatConstant.MY_COMMIT_RECEIPT_VALUE;
        ChatConstant.myCommitJectValue = ChatConstant.MY_COMMIT_REJECT_VALUE;
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

    public void propSetValue(String key, String value) {
        PropertiesComponent.getInstance().setValue(key, value);
    }

    public void propSetBooleanValue(String key, Boolean value) {
        PropertiesComponent.getInstance().setValue(key, value);
    }
}