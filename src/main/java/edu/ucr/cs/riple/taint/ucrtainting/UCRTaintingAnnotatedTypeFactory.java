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
import org.checkerframework.javacutil.TreeUtils;

import javax.lang.model.element.AnnotationMirror;
import java.util.List;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
    private String ANNOTATED_PACKAGE_NAMES;
    public final AnnotationMirror RPOLYTAINTED;
    public final AnnotationMirror RUNTAINTED;
    public final AnnotationMirror RTAINTED;
    public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
        super(checker);
        ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
        // Loads the stub files here by side effecting subTypes and aJavaTypes
        postInit();
        RPOLYTAINTED = AnnotationBuilder.fromClass(elements, RPolyTainted.class);
        RUNTAINTED = AnnotationBuilder.fromClass(elements, RUntainted.class);
        RTAINTED = AnnotationBuilder.fromClass(elements, RTainted.class);
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
            if(isReceiverOrArgumentTainted(node)) {
                annotatedTypeMirror.replaceAnnotation(RTAINTED);
            } else {
                annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
            }
            return super.visitMethodInvocation(node, annotatedTypeMirror);
        }
        @Override
        public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
            if(isReceiverOrArgumentTainted(node)) {
                annotatedTypeMirror.replaceAnnotation(RTAINTED);
            }
            else {
                annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
            }
            return super.visitNewClass(node, annotatedTypeMirror);
        }

        private boolean isReceiverOrArgumentTainted(ExpressionTree node) {
            if (node instanceof NewClassTree || node instanceof MethodInvocationTree) {
                boolean isAnyArgumentTainted=false;
                boolean isReceiverTainted = false;
                // Is the receiver tainted
                ExpressionTree receiver = TreeUtils.getReceiverTree(node);
                if(receiver != null && getAnnotatedType(receiver).hasAnnotation(RTAINTED)) {
                    isReceiverTainted = true;
                }
                // Is any of the arguments tainted
                List<? extends ExpressionTree> arguments = null;
                if (node instanceof NewClassTree) {
                    NewClassTree classTree = (NewClassTree) node;
                    arguments = classTree.getArguments();
                } else if (node instanceof MethodInvocationTree) {
                    MethodInvocationTree methodInvocationTree = (MethodInvocationTree) node;
                    arguments = methodInvocationTree.getArguments();
                }
                if(arguments != null) {
                    for (ExpressionTree eTree : arguments) {
                        if (getAnnotatedTypeFromTypeTree(eTree).hasAnnotation(RTAINTED)) {
                            isAnyArgumentTainted = true;
                            break;
                        }
                    }
                    if (isAnyArgumentTainted || isReceiverTainted) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}