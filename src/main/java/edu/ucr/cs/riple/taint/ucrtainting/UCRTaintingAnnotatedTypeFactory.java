package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPolyTainted;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;

import javax.lang.model.element.AnnotationMirror;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    private String ANNOTATED_PACKAGE_NAMES;
    private final AnnotationMirror POLY;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        POLY = AnnotationBuilder.fromClass(elements, RPolyTainted.class);
        // Loads the stub files here by side effecting subTypes and aJavaTypes
        postInit();
        // can access stub file annotations here.
        // maybe write a new procedure here to handle our handling of preprocessing unannotated codes
        System.out.println(stubTypes);
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
            if(getAnnotatedTypeFromTypeTree(node).equals(annotatedTypeMirror)) {
                System.out.println();
            }
//            if(isLibraryCode(node)) {
//                // Update annotation to polymorphic
//                if(!annotatedTypeMirror.hasAnnotation(POLY)) {
//                    annotatedTypeMirror.replaceAnnotation(POLY);
//
//                }
//            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }

        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            return super.visitNewClass(node, annotatedTypeMirror);
        }

        protected boolean isLibraryCode(ExpressionTree node) {
            if(node instanceof MethodInvocationTree) {
                MethodInvocationTree methodNode = (MethodInvocationTree) node;
            } else if(node instanceof NewClassTree) {

            }
            return false;
        }
    }
}