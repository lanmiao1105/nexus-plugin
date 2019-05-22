package com.lm.plugin.idea.nexus;

import javax.swing.*;
import java.awt.*;

public class UploadingDialog extends JDialog {
    private Container contentPane;
    private JProgressBar progressBar;

    public UploadingDialog() {
        // 是否对话框显示时候其他窗口不能触发事件
        setModal(true);
        // 是否无标题栏
        setUndecorated(false);
        // 屏幕中居中
        setLocationRelativeTo(null);
        // 一直在上方
        setAlwaysOnTop(true);
        // 设置ContentPane
        contentPane = new JPanel();
        setContentPane(contentPane);

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("uploading...");
        progressBar.setIndeterminate(true);

        contentPane.add(progressBar);
    }
}
