package com.lsyf.lsyfollama.ui.actions;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import org.jetbrains.annotations.NotNull;

public class ApiEndpointVisitor extends PsiRecursiveElementWalkingVisitor {
//    private final List<PsiMethod> apiMethods = new List<PsiMethod>();

    @Override
    public void visitElement(@NotNull PsiElement element) {
//        if (element instanceof PsiMethod) {
//            Psi method = (PsiMethod) element;
//            // 检查方法上是否有我们感兴趣的注解
//            if (hasAnnotation(method, "org.springframework.web.bind.annotation.GetMapping") ||
//                    hasAnnotation(method, "org.springframework.web.bind.annotation.PostMapping") ||
//                    hasAnnotation(method, "org.springframework.web.bind.annotation.RequestMapping")) {
//                apiMethods.add(method);
//            }
//        }
        super.visitElement(element);
    }

//    private boolean hasAnnotation(PsiMethod method, String annotationFQN) {
//        return method.getAnnotation(annotationFQN) != null;
//    }

//    public List<PsiMethod> getApiMethods() {
//        return apiMethods;
//    }
}