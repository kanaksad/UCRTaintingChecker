package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPolyTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;
import org.checkerframework.checker.initialization.qual.Initialized;
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
    public final AnnotationMirror POLY;
    public final AnnotationMirror UNTAINT;
    public final AnnotationMirror TAINT;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        // Loads the stub files here by side effecting subTypes and aJavaTypes
        postInit();
        POLY = AnnotationBuilder.fromClass(elements, RPolyTainted.class);
        UNTAINT = AnnotationBuilder.fromClass(elements, RUntainted.class);
        TAINT = AnnotationBuilder.fromClass(elements, RTainted.class);
        // can access stub file annotations here.
        // maybe write a new procedure here to handle our handling of preprocessing unannotated codes
        System.out.println("After PostInit");
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
            boolean isAnyArgumentTainted=false;
            for(ExpressionTree eTree:node.getArguments()) {
                if(getAnnotatedTypeFromTypeTree(eTree).hasAnnotation(TAINT)) {
                    isAnyArgumentTainted=true;
                    break;
                }
            }
            if(isAnyArgumentTainted==true) {
                annotatedTypeMirror.replaceAnnotation(TAINT);
            } else {
                annotatedTypeMirror.replaceAnnotation(UNTAINT);
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
//            boolean isAnyMemberTainted=false;
//            for (ExpressionTree member : node.getArguments()) {
//                if(getAnnotatedType(member).hasEffectiveAnnotation(Initialized.class)) {
//                    if(getAnnotatedTypeFromTypeTree(member).hasAnnotation(TAINT)) {
//                        isAnyMemberTainted=true;
//                    }
//                }
//            }
//            if(isAnyMemberTainted==true) {
//                annotatedTypeMirror.replaceAnnotation(TAINT);
//            } else {
//                annotatedTypeMirror.replaceAnnotation(UNTAINT);
//            }
            return super.visitNewClass(node, annotatedTypeMirror);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror p) {
            return super.visitMethod(node, p);
        }

    }
}