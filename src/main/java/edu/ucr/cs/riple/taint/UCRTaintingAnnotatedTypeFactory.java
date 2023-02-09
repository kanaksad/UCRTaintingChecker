package edu.ucr.cs.riple.taint;

import com.sun.source.tree.*;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingChecker;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory{

    private String ANNOTATED_PACKAGES;
    private String UNANNOTATED_SUB_PACKAGES;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        UNANNOTATED_SUB_PACKAGES = checker.getOption(UCRTaintingChecker.UNANNOTATED_SUB_PACKAGES);
        postInit();
    }

    private class UCRTaintingTreeAnnotator extends TreeAnnotator {
        protected UCRTaintingTreeAnnotator(AnnotatedTypeFactory aTypeFactory) {
            super(aTypeFactory);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(super.createTreeAnnotator(), new UCRTaintingTreeAnnotator(this));
    }

}
