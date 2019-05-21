package com.lm;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.lm.utils.Utils;
import org.jetbrains.annotations.NotNull;

public class UploadAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getData(LangDataKeys.PROJECT);
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        if (project == null) {
            Utils.showErrorDialog("Project == null");
            return;
        }
        if (psiFile == null) {
            Utils.showErrorDialog("select file == null");
            return;
        }
        UploadConfigDialog dialog = new UploadConfigDialog(project, psiFile);
        dialog.pack();
        dialog.setVisible(true);
    }
}
