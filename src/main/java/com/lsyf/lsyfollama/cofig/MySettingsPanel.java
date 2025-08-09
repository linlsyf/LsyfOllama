package com.lsyf.lsyfollama.cofig;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import com.lsyf.lsyfollama.ChatConstant;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class MySettingsPanel implements Configurable {
    private JPanel mainPanel;

    private JTextField inputField;
    private JTextField modelField;


    @Override
    public @Nullable JComponent createComponent() {
        mainPanel= new JPanel(new BorderLayout());

        inputField = new JTextField();

        modelField = new JTextField();
        mainPanel.add(inputField, BorderLayout.CENTER);
        mainPanel.add(modelField, BorderLayout.SOUTH);
        return mainPanel;
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
        String  IP= inputField.getText();
        String  MODE= modelField.getText();
        PropertiesComponent.getInstance().setValue(ChatConstant.MY_PLUGIN_SETTING, IP);
        PropertiesComponent.getInstance().setValue(ChatConstant.MY_MODEL_SETTING, MODE);


        ChatConstant.apiUrl =IP;
        ChatConstant.modelSetting =MODE;
    }

    @Override
    public void reset() {
        // 重置为默认值或加载已保存的配置
        inputField.setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_PLUGIN_SETTING, "输入api地址端口"));
        modelField.setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_MODEL_SETTING, "输入模型"));
        ChatConstant.apiUrl ="";
        ChatConstant.modelSetting ="";
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