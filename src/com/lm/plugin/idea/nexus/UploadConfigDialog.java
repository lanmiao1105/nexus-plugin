package com.lm.plugin.idea.nexus;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.lm.plugin.idea.nexus.utils.Utils;
import com.lm.plugin.idea.nexus.utils.io.IOUtils;
import com.lm.plugin.idea.nexus.utils.io.InputStreamWrapper;
import org.gradle.tooling.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class UploadConfigDialog extends JDialog {

    private Box contentPane;
    private JTextField tfUrl;
    private JTextField tfUserName;
    private JTextField tfPassword;
    private JTextField tfGroupId;
    private JTextField tfArtifactId;
    private JTextField tfVersion;
    private JTextField tfPackaging;
    private JTextField tfDescription;
    private JButton btnUpload;
    private JButton btnCancel;

    private Project project;
    private PsiFile psiFile;
    private Module module;

    private static final String buildFileName = "build.gradle";
    private File buildFile;
    private static final String uploadScriptFileName = "nexus_maven.gradle";
    private File uploadScriptFile;

    public UploadConfigDialog(Project project, PsiFile psiFile) {
        this.project = project;
        this.psiFile = psiFile;
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(psiFile.getVirtualFile().getPath());
        module = ModuleUtil.findModuleForFile(virtualFile, project);
        String path = module.getModuleFile().getParent().getPath();
        this.buildFile = new File(path, buildFileName);
        this.uploadScriptFile = new File(path, uploadScriptFileName);

        // 是否对话框显示时候其他窗口不能触发事件
        setModal(true);
        // 是否无标题栏
        setUndecorated(false);
        // 屏幕中居中
        setLocationRelativeTo(null);
        // 一直在上方
        setAlwaysOnTop(true);
        // 设置ContentPane
        contentPane = Box.createVerticalBox();
        setContentPane(contentPane);

        tfUrl = addInputLine("url");
        tfUserName = addInputLine("userName");
        tfPassword = addInputLine("password");
        tfGroupId = addInputLine("groupId");
        tfArtifactId = addInputLine("artifactId");
        tfVersion = addInputLine("version");
        tfPackaging = addInputLine("packaging");
        tfDescription = addInputLine("description");

        addButton();

        if (uploadScriptFile.exists()) {
            try {
                InputStreamWrapper inputStreamWrapper = IOUtils.inputStreamWrapper(uploadScriptFile);
                String line;
                while ((line = inputStreamWrapper.readUTF8Line()) != null) {
                    if (line.contains("nexus_maven.url")) {
                        tfUrl.setText(line.replaceAll("nexus_maven.url| |=|\"", ""));
                    } else if (line.contains("nexus_maven.userName")) {
                        tfUserName.setText(line.replaceAll("nexus_maven.userName| |=|\"", ""));
                    } else if (line.contains("nexus_maven.password")) {
                        tfPassword.setText(line.replaceAll("nexus_maven.password| |=|\"", ""));
                    } else if (line.contains("nexus_maven.groupId")) {
                        tfGroupId.setText(line.replaceAll("nexus_maven.groupId| |=|\"", ""));
                    } else if (line.contains("nexus_maven.artifactId")) {
                        tfArtifactId.setText(line.replaceAll("nexus_maven.artifactId| |=|\"", ""));
                    } else if (line.contains("nexus_maven.version")) {
                        tfVersion.setText(line.replaceAll("nexus_maven.version| |=|\"", ""));
                    } else if (line.contains("nexus_maven.packaging")) {
                        tfPackaging.setText(line.replaceAll("nexus_maven.packaging| |=|\"", ""));
                    } else if (line.contains("nexus_maven.description")) {
                        tfDescription.setText(line.replaceAll("nexus_maven.description| |=|\"", ""));
                        break;
                    }
                }
                inputStreamWrapper.close();
            } catch (IOException e) {
                Utils.showErrorDialog(e.getMessage());
            }
        } else {
            tfUrl.setText("http://localhost:8085/repository/maven-snapshots/");
            tfUserName.setText("admin");
            tfPassword.setText("admin123");
            tfGroupId.setText("com.example");
            tfArtifactId.setText(module.getName());
            tfVersion.setText("1.0.0-SNAPSHOT");
            tfPackaging.setText("aar");
            tfDescription.setText("description");
        }
    }


    private void addButton() {
        btnUpload = new JButton("Upload");
        btnCancel = new JButton("Cancel");

        btnUpload.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                onUploadClick();
            }
        });

        btnCancel.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(btnUpload);
        box.add(Box.createHorizontalStrut(20));
        box.add(btnCancel);
        box.add(Box.createHorizontalStrut(10));

        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(box);
        contentPane.add(Box.createVerticalStrut(10));

        getRootPane().setDefaultButton(btnUpload);
    }

    private JTextField addInputLine(String labelName) {

        JLabel label = new JLabel(labelName);
        label.setPreferredSize(new Dimension(75, 30));

        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(300, 30));

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalStrut(10));
        box.add(label);
        box.add(textField);
        box.add(Box.createHorizontalStrut(10));

        contentPane.add(Box.createVerticalStrut(10));
        contentPane.add(box);

        return textField;
    }

    private void onUploadClick() {

        if (!buildFile.exists()){
            Utils.showErrorDialog("only support gradle");
            return;
        }

        try {
            String url = tfUrl.getText();
            String userName = tfUserName.getText();
            String password = tfPassword.getText();
            String groupId = tfGroupId.getText();
            String artifactId = tfArtifactId.getText();
            String version = tfVersion.getText();
            String packaging = tfPackaging.getText();
            String description = tfDescription.getText();

            String uploadScript = Utils.buildUploadScript(url, userName, password, groupId, artifactId, version, packaging, description);

            IOUtils.outputStreamWrapper(uploadScriptFile, false)
                    .writeUTF8(uploadScript)
                    .flush()
                    .close();
            LocalFileSystem.getInstance().refresh(true);

            InputStreamWrapper inputStreamWrapper = IOUtils.inputStreamWrapper(buildFile);
            String line;
            while ((line = inputStreamWrapper.readUTF8Line()) != null) {
                if (line.contains("nexus_maven.gradle") && line.startsWith("apply from:")) break;
            }
            inputStreamWrapper.close();

            if (line == null) {
                IOUtils.outputStreamWrapper(buildFile, true)
                        .writeUTF8("\napply from: './" + uploadScriptFileName + "'")
                        .flush()
                        .close();
                LocalFileSystem.getInstance().refresh(true);
                ProjectManager.getInstance().reloadProject(project);
            }

            ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(buildFile.getParentFile()).connect();
            UploadingDialog uploadingDialog = new UploadingDialog();
            connection.newBuild()
                    .forTasks("uploadArchives")
                    .addProgressListener((ProgressListener) event -> {

                    })
                    .run(new ResultHandler<Void>() {
                        @Override
                        public void onComplete(Void result) {
                            uploadingDialog.dispose();
                        }

                        @Override
                        public void onFailure(GradleConnectionException failure) {
                            uploadingDialog.dispose();
                        }
                    });
            uploadingDialog.pack();
            uploadingDialog.setVisible(true);
            connection.close();
        } catch (Exception e) {
            Utils.showErrorDialog(e.getMessage());
        }
    }


}
