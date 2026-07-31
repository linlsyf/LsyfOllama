package com.lsyf.lsyfollama.ui.actions;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;

import javax.swing.*;

public class MyPluginCheckinHandlerFactory extends CheckinHandlerFactory {
    @Override
    public CheckinHandler createHandler( CheckinProjectPanel panel,
                                        CommitContext commitContext) {
        return new CheckinHandler() {
            private boolean myEnabled = false;


            @Override
            public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {

                JCheckBox cb = new JCheckBox("我的检查项", false);
                return new RefreshableOnComponent() {
                    public JComponent getComponent() {
                        return cb;
                    }

                    public void refresh() { /* 可选实现 */ }

                    @Override
                    public void saveState() {

                    }

                    @Override
                    public void restoreState() {

                    }
                };

            }
            @Override
            public ReturnResult beforeCheckin() {
                if (!myEnabled) {
                    return ReturnResult.COMMIT;
                }
                // 这里写你的检查逻辑
//                boolean ok = doMyCheck();
                boolean ok = true;

                if (!ok) {
                    // 返回 CANCEL 可阻止提交
                    return ReturnResult.CANCEL;
                }
                return ReturnResult.COMMIT;
            }
        };
    }
}