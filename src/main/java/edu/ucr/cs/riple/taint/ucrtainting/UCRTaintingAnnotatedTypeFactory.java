package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    private String ANNOTATED_PACKAGE_NAMES;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        postInit();
    }

    @Override
    protected TreeAnnotator createTreeAnnotator() {
        return new ListTreeAnnotator(super.createTreeAnnotator(),
                new UCRTaintingTreeAnnotator(this));
    }

    private class UCRTaintingTreeAnnotator extends TreeAnnotator {

        /**
         * Create a new TreeAnnotator.
         *
         * @param atypeFactory the type factory
         */
        protected UCRTaintingTreeAnnotator(AnnotatedTypeFactory atypeFactory) {
            super(atypeFactory);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
    }
}