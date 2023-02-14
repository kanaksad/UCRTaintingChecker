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
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import java.util.Arrays;
import java.util.List;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    private String ANNOTATED_PACKAGE_NAMES;
    private List<String> ANNOTATED_PACKAGE_NAMES_LIST;
    public final AnnotationMirror RPOLY;
    public final AnnotationMirror RUNTAINT;
    public final AnnotationMirror RTAINT;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        ANNOTATED_PACKAGE_NAMES_LIST= Arrays.asList(ANNOTATED_PACKAGE_NAMES.split(","));
        // Loads the stub files here by side effecting subTypes and aJavaTypes
        postInit();
        RPOLY = AnnotationBuilder.fromClass(elements, RPolyTainted.class);
        RUNTAINT = AnnotationBuilder.fromClass(elements, RUntainted.class);
        RTAINT = AnnotationBuilder.fromClass(elements, RTainted.class);
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
            boolean isAnnotatedPackage=false;
            if(node!=null) {
                ExpressionTree receiverTree= TreeUtils.getReceiverTree(node);
                if(receiverTree!=null) {
                    String packageName=ElementUtils.getType(TreeUtils.elementFromTree(receiverTree)).toString();
                    System.out.println("visitMethodInvocation Node Package : "+ packageName);
                    if(isAnnotatedPackage(packageName)) {
                        System.out.println("Found Annotated Package: "+packageName+"  Skipping it");
                        isAnnotatedPackage=true;
                    }
                }
            }
            if(!isAnnotatedPackage) {
                if(containsTaintedArgument(node)) {
                    annotatedTypeMirror.replaceAnnotation(RTAINT);
                } else {
                    annotatedTypeMirror.replaceAnnotation(RUNTAINT);
                }
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
//            boolean isAnyMemberTainted=false;
//            for (ExpressionTree member : node.getArguments()) {
//                if(getAnnotatedType(member).hasEffectiveAnnotation(Initialized.class)) {
//                    if(getAnnotatedTypeFromTypeTree(member).hasAnnotation(RTAINT)) {
//                        isAnyMemberTainted=true;
//                    }
//                }
//            }
//            if(isAnyMemberTainted==true) {
//                annotatedTypeMirror.replaceAnnotation(RTAINT);
//            } else {
//                annotatedTypeMirror.replaceAnnotation(RUNTAINT);
//            }
            return super.visitNewClass(node, annotatedTypeMirror);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror p) {
            return super.visitMethod(node, p);
        }

        public boolean isAnnotatedPackage(String packageName) {
            for(String annotatedPackageName:ANNOTATED_PACKAGE_NAMES_LIST) {
                if(packageName.startsWith(annotatedPackageName)) {
                    return true;
                }
            }
            return false;
        }
        public boolean containsTaintedArgument(MethodInvocationTree node) {
            for(ExpressionTree eTree:node.getArguments()) {
                if(getAnnotatedTypeFromTypeTree(eTree).hasAnnotation(RTAINT)) {
                    return true;
                }
            }
            return false;
        }
    }
}