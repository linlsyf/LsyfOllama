package com.lsyf.lsyfollama.business;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ApiTestLogic {


    public static void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // 获取当前文件
        PsiFile psiFile = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
        if (psiFile == null) {
            Messages.showInfoMessage(project, "Please open a Java file first", "Info");
            return;
        }

        // 查找所有方法
        List<PsiMethod> methods = new List<PsiMethod>();
        PsiTreeUtil.processElements(psiFile, element -> {
            if (element instanceof PsiMethod) {
                methods.add((PsiMethod) element);
            }
            return true;
        });

        // 检查是否有Spring注解的方法
        List<PsiMethod> apiMethods = new List<PsiMethod>();
        for (PsiMethod method : methods) {
            if (isApiMethod(method)) {
                apiMethods.add(method);
            }
        }

        if (apiMethods.isEmpty()) {
            Messages.showInfoMessage(project, "No API methods found in this file", "Info");
            return;
        }

        // 显示找到的API方法
        StringBuilder message = new StringBuilder("Found API methods:\n\n");
        for (PsiMethod method : apiMethods) {
            String httpMethod = getHttpMethod(method);
            String path = getPath(method);
            message.append(method.getName()).append(": ").append(httpMethod).append(" ").append(path).append("\n");
        }

        Messages.showInfoMessage(project, message.toString(), "API Methods Found");
    }

    private boolean isApiMethod(PsiMethod method) {
        return hasAnnotation(method, "org.springframework.web.bind.annotation.GetMapping") ||
                hasAnnotation(method, "org.springframework.web.bind.annotation.PostMapping") ||
                hasAnnotation(method, "org.springframework.web.bind.annotation.PutMapping") ||
                hasAnnotation(method, "org.springframework.web.bind.annotation.DeleteMapping") ||
                hasAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping");
    }

    private boolean hasAnnotation(PsiMethod method, String annotationFQN) {
        PsiAnnotation annotation = method.getAnnotation(annotationFQN);
        return annotation != null;
    }

    private String getHttpMethod(PsiMethod method) {
        if (hasAnnotation(method, "org.springframework.web.bind.annotation.GetMapping")) return "GET";
        if (hasAnnotation(method, "org.springframework.web.bind.annotation.PostMapping")) return "POST";
        if (hasAnnotation(method, "org.springframework.web.bind.annotation.PutMapping")) return "PUT";
        if (hasAnnotation(method, "org.springframework.web.bind.annotation.DeleteMapping")) return "DELETE";

        PsiAnnotation requestMapping = method.getAnnotation(
                "org.springframework.web.bind.annotation.RequestMapping");
        if (requestMapping != null) {
            // 这里可以解析RequestMapping的method属性
            return "REQUEST";
        }

        return "UNKNOWN";
    }

    private String getPath(PsiMethod method) {
        // 简化实现 - 实际中需要从注解中提取路径值
        return "/api/" + method.getName();
    }

    public void update(@NotNull AnActionEvent e) {
        // 只在有项目且文件是Java文件时启用此操作
        Project project = e.getProject();
        PsiFile psiFile = e.getDataContext().getData(CommonDataKeys.PSI_FILE);
        e.getPresentation().setEnabledAndVisible(project != null && psiFile instanceof PsiJavaFile);
    }

}
