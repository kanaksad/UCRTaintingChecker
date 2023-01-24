package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import org.checkerframework.common.basetype.BaseAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.*;

public class UCRTaintingAnnotatedTypeFactory extends BaseAnnotatedTypeFactory {
  private final boolean ENABLE_CUSTOM_CHECK;
  private final String ANNOTATED_PACKAGE_NAMES;
  private final List<String> ANNOTATED_PACKAGE_NAMES_LIST;
  private final AnnotationMirror RUNTAINTED;
  private final AnnotationMirror RTAINTED;

  public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker);
    ENABLE_CUSTOM_CHECK = checker.getBooleanOption(UCRTaintingChecker.ENABLE_CUSTOM_CHECKER, true);

    ANNOTATED_PACKAGE_NAMES = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
    if (ANNOTATED_PACKAGE_NAMES == null) {
      if (checker.hasOption(UCRTaintingChecker.ANNOTATED_PACKAGES)) {
        throw new UserError(
            "The value for the argument -AannotatedPackages"
                + " is null. Please pass this argument in the checker config, refer checker manual");
      } else {
        throw new UserError(
            "Cannot find this argument -AannotatedPackages"
                + " Please pass this argument in the checker config, refer checker manual");
      }
    } else {
      ANNOTATED_PACKAGE_NAMES_LIST = Arrays.asList(ANNOTATED_PACKAGE_NAMES.split(","));
    }
    postInit();
    RUNTAINTED = AnnotationBuilder.fromClass(elements, RUntainted.class);
    RTAINTED = AnnotationBuilder.fromClass(elements, RTainted.class);
  }

  @Override
  protected TreeAnnotator createTreeAnnotator() {
    return new ListTreeAnnotator(super.createTreeAnnotator(), new UCRTaintingTreeAnnotator(this));
  }

  private class UCRTaintingTreeAnnotator extends TreeAnnotator {

    /**
     * UCRTaintingTreeAnnotator
     *
     * @param atypeFactory the type factory
     */
    protected UCRTaintingTreeAnnotator(AnnotatedTypeFactory atypeFactory) {
      super(atypeFactory);
    }

    @Override
    public Void visitMethodInvocation(
        MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      if (ENABLE_CUSTOM_CHECK) {
        if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
          if (hasTaintedArgument(node) || hasTaintedReceiver(node)) {
            annotatedTypeMirror.replaceAnnotation(RTAINTED);
          } else {
            annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
          }
        }
      }
      return super.visitMethodInvocation(node, annotatedTypeMirror);
    }

    @Override
    public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      if (ENABLE_CUSTOM_CHECK) {
        if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
          if (hasTaintedArgument(node) || hasTaintedReceiver(node)) {
            annotatedTypeMirror.replaceAnnotation(RTAINTED);
          } else {
            annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
          }
        }
      }
      return super.visitNewClass(node, annotatedTypeMirror);
    }

    private boolean hasTaintedArgument(ExpressionTree node) {
      List<? extends ExpressionTree> argumentsList = null;
      if (node instanceof MethodInvocationTree) {
        argumentsList = ((MethodInvocationTree) node).getArguments();
      } else if (node instanceof NewClassTree) {
        argumentsList = ((NewClassTree) node).getArguments();
      }
      if (argumentsList != null) {
        for (ExpressionTree eTree : argumentsList) {
          try {
            if (getAnnotatedType(eTree).hasAnnotation(RTAINTED)) {
              return true;
            }
          } catch (BugInCF bug) {
            // TODO:: take care of errors
          }
        }
      }
      return false;
    }

    private boolean hasTaintedReceiver(ExpressionTree node) {
      if (node != null) {
        ExpressionTree receiverTree = TreeUtils.getReceiverTree(node);
        if (receiverTree != null) {
          Element element = TreeUtils.elementFromTree(node);
          if (element != null) {
            Set<Modifier> modifiers = element.getModifiers();
            if (modifiers != null
                && !modifiers.contains(Modifier.STATIC)
                && getAnnotatedType(receiverTree).hasAnnotation(RTAINTED)) {
              return true;
            }
          }
        }
      }
      return false;
    }

    private boolean hasAnnotatedPackage(ExpressionTree node) {
      if (node != null) {
        ExpressionTree receiverTree = TreeUtils.getReceiverTree(node);
        if (receiverTree != null) {
          String packageName = "";
          try {
            packageName = ElementUtils.getType(TreeUtils.elementFromTree(receiverTree)).toString();
            if (!packageName.equals("")) {
              packageName = packageName.substring(0, packageName.lastIndexOf("."));
            }
          } catch (Exception | Error e) {
            // TODO:: take care of exceptions or errors
          }
          if (isAnnotatedPackage(packageName)) {
            return true;
          }
        }
      }
      return false;
    }

    private boolean isAnnotatedPackage(String packageName) {
      for (String annotatedPackageName : ANNOTATED_PACKAGE_NAMES_LIST) {
        if (packageName.equals(annotatedPackageName)) {
          return true;
        }
      }
      return false;
    }

    private boolean isPresentInStub(ExpressionTree node) {
      if (node != null) {
        Element elem = TreeUtils.elementFromTree(node);
        if (elem != null && isFromStubFile(elem)) {
          return true;
        }
      }
      return false;
    }
  }
}
