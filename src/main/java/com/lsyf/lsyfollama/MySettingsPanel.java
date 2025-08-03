package com.lsyf.lsyfollama;


import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class MySettingsPanel implements Configurable {
    private JPanel mainPanel;

    private JTextField inputField;


    @Override
    public @Nullable JComponent createComponent() {
        inputField = new JTextField();
        return inputField;
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
        PropertiesComponent.getInstance().setValue(ChatConstant.MY_PLUGIN_SETTING, inputField.getText());
    }

    @Override
    public void reset() {
        // 重置为默认值或加载已保存的配置
        inputField.setText(PropertiesComponent.getInstance().getValue(ChatConstant.MY_PLUGIN_SETTING, "defaultValue"));
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
        return "lsyf  Settings";
    }
}