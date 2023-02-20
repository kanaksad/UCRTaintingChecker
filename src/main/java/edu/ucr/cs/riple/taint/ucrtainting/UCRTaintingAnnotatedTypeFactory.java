package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPolyTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.AnnotationBuilder;
import org.checkerframework.javacutil.ElementUtils;
import org.checkerframework.javacutil.TreeUtils;
import org.checkerframework.javacutil.UserError;

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
        if(ANNOTATED_PACKAGE_NAMES==null) {
            if(checker.hasOption(UCRTaintingChecker.ANNOTATED_PACKAGES)) {
                throw new UserError(
                        "The value for the argument -AannotatedPackages"
                                + " is null. Please pass this argument in the checker config, refer checker manual");
            } else {
                throw new UserError(
                        "Cannot find this argument -AannotatedPackages"
                                + " Please pass this argument in the checker config, refer checker manual");
            }
        } else {
            ANNOTATED_PACKAGE_NAMES_LIST= Arrays.asList(ANNOTATED_PACKAGE_NAMES.split(","));
        }
        // Loads the stub files here by side effecting subTypes and aJavaTypes
        postInit();
        RPOLY = AnnotationBuilder.fromClass(elements, RPolyTainted.class);
        RUNTAINT = AnnotationBuilder.fromClass(elements, RUntainted.class);
        RTAINT = AnnotationBuilder.fromClass(elements, RTainted.class);
        // can access stub file annotations here.
        // maybe write a new procedure here to handle our handling of preprocessing unannotated codes
//        System.out.println("After PostInit");
//        System.out.println(stubTypes);
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
            if(!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
                if(hasTaintedArgument(node)) {
                    annotatedTypeMirror.replaceAnnotation(RTAINT);
                } else {
                    annotatedTypeMirror.replaceAnnotation(RUNTAINT);
                }
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            if(!hasAnnotatedPackage(node)) {
                if(hasTaintedArgument(node)) {
                    annotatedTypeMirror.replaceAnnotation(RTAINT);
                } else {
                    annotatedTypeMirror.replaceAnnotation(RUNTAINT);
                }
            }
            return super.visitNewClass(node, annotatedTypeMirror);
        }

        @Override
        public Void visitMethod(MethodTree node, AnnotatedTypeMirror p) {
            return super.visitMethod(node, p);
        }

        private boolean isAnnotatedPackage(String packageName) {
            for(String annotatedPackageName:ANNOTATED_PACKAGE_NAMES_LIST) {
                if(packageName.startsWith(annotatedPackageName)) {
                    return true;
                }
            }
            return false;
        }
        private boolean hasTaintedArgument(ExpressionTree node) {
            List<? extends ExpressionTree> argumentsList = null;
            if(node instanceof MethodInvocationTree) {
                argumentsList=((MethodInvocationTree) node).getArguments();
            } else if(node instanceof NewClassTree) {
                argumentsList=((NewClassTree) node).getArguments();
            }
            for(ExpressionTree eTree: argumentsList) {
                if(getAnnotatedTypeFromTypeTree(eTree).hasAnnotation(RTAINT)) {
                    return true;
                }
            }
            return false;
        }
        //        private boolean hasTaintedReceiver(ExpressionTree node) {
//            if(node!=null) {
//                ExpressionTree receiverTree= TreeUtils.getReceiverTree(node);
//                if(receiverTree!=null) {
//                    if(getAnnotatedType(receiverTree).hasAnnotation(RTAINT)) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
        private boolean hasAnnotatedPackage(ExpressionTree node) {
            if(node!=null) {
                ExpressionTree receiverTree= TreeUtils.getReceiverTree(node);
                if(receiverTree!=null) {
                    String packageName=ElementUtils.getType(TreeUtils.elementFromTree(receiverTree)).toString();
                    System.out.println("hasAnnotatedPackage Node Package : "+ packageName);
                    if(isAnnotatedPackage(packageName)) {
                        System.out.println("Found Annotated Package: "+packageName+"  Skipping it");
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isPresentInStub(ExpressionTree node) {
            if(node!=null) {
                if(isFromStubFile(TreeUtils.elementFromTree(node))) {
                    System.out.println("This is from stub: "+ElementUtils.getType(TreeUtils.elementFromTree(node)).toString());
                    return true;
                }
            }
            return false;
        }
    }
}