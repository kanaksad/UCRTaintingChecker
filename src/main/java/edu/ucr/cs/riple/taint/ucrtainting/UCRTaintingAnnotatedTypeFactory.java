package edu.ucr.cs.riple.taint.ucrtainting;

import com.sun.source.tree.*;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RPossiblyValidated;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RTainted;
import edu.ucr.cs.riple.taint.ucrtainting.qual.RUntainted;
import org.checkerframework.common.accumulation.AccumulationAnnotatedTypeFactory;
import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.returnsreceiver.qual.This;
import org.checkerframework.framework.type.AnnotatedTypeFactory;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.treeannotator.ListTreeAnnotator;
import org.checkerframework.framework.type.treeannotator.TreeAnnotator;
import org.checkerframework.javacutil.*;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class UCRTaintingAnnotatedTypeFactory extends AccumulationAnnotatedTypeFactory {

  /**
   * This option enables custom handling of third party code. By default, such handling is enabled.
   */
  public final boolean ENABLE_LIBRARY_CHECK;

  public final boolean ENABLE_VALIDATION_CHECK;

  public final boolean ENABLE_SANITIZER_CHECK;
  /**
   * To respect existing annotations, leave them alone based on the provided annotated package names
   * through this option.
   */
  private final String ANNOTATED_PACKAGE_NAMES;
  /** List of annotated packages. Classes in these packages are considered to be annotated. */
  private final List<String> ANNOTATED_PACKAGE_NAMES_LIST;
  /** AnnotationMirror for {@link RUntainted}. */
  public final AnnotationMirror RUNTAINTED;
  /** AnnotationMirror for {@link RTainted}. */
  public final AnnotationMirror RTAINTED;

  public UCRTaintingAnnotatedTypeFactory(BaseTypeChecker checker) {
    super(checker, RPossiblyValidated.class, RUntainted.class, null);
    ENABLE_LIBRARY_CHECK =
        checker.getBooleanOption(UCRTaintingChecker.ENABLE_LIBRARY_CHECKER, true);
    ENABLE_VALIDATION_CHECK =
        checker.getBooleanOption(UCRTaintingChecker.ENABLE_VALIDATION_CHECKER, false);
    ENABLE_SANITIZER_CHECK =
        checker.getBooleanOption(UCRTaintingChecker.ENABLE_SANITIZATION_CHECKER, false);
    String givenAnnotatedPackages = checker.getOption(UCRTaintingChecker.ANNOTATED_PACKAGES);
    // make sure that annotated package names are always provided and issue error otherwise
    if (givenAnnotatedPackages == null) {
      if (checker.hasOption(UCRTaintingChecker.ANNOTATED_PACKAGES)) {
        throw new UserError(
            "The value for the argument -AannotatedPackages"
                + " is null. Please pass this argument in the checker config, refer checker manual");
      } else {
        throw new UserError(
            "Cannot find this argument -AannotatedPackages"
                + " Please pass this argument in the checker config, refer checker manual");
      }
    }
    ANNOTATED_PACKAGE_NAMES = givenAnnotatedPackages.equals("\"\"") ? "" : givenAnnotatedPackages;
    ANNOTATED_PACKAGE_NAMES_LIST = Arrays.asList(ANNOTATED_PACKAGE_NAMES.split(","));
    RUNTAINTED = AnnotationBuilder.fromClass(elements, RUntainted.class);
    RTAINTED = AnnotationBuilder.fromClass(elements, RTainted.class);
    postInit();
  }

  @Override
  protected TreeAnnotator createTreeAnnotator() {
    return new ListTreeAnnotator(super.createTreeAnnotator(), new UCRTaintingTreeAnnotator(this));
  }

  public AnnotationMirror rPossiblyValidatedAM(List<String> calledMethods) {
    AnnotationBuilder builder = new AnnotationBuilder(processingEnv, RPossiblyValidated.class);
    builder.setValue("value", calledMethods.toArray());
    AnnotationMirror am = builder.build();
    return am;
  }

  /**
   * Checks if any of the arguments of the node has been annotated with {@link RTainted}
   *
   * @param node to check for
   * @return true if any argument is annotated with {@link RTainted}, false otherwise
   */
  public boolean hasTaintedArgument(ExpressionTree node) {
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

  /**
   * Checks if the receiver tree has been annotated with {@link RTainted}
   *
   * @param node to check for
   * @return true if annotated with {@link RTainted}, false otherwise
   */
  public boolean hasTaintedReceiver(ExpressionTree node) {
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

  /**
   * Checks if the receiver tree is available
   *
   * @param node to check for
   * @return true if available, false otherwise
   */
  public boolean hasReceiver(ExpressionTree node) {
    if (node != null) {
      ExpressionTree receiverTree = TreeUtils.getReceiverTree(node);
      if (receiverTree != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the package for the node is present in already annotated according to provided
   * option.
   *
   * @param node to check for
   * @return true if present, false otherwise
   */
  public boolean hasAnnotatedPackage(ExpressionTree node) {
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

  /**
   * Checks if the tree is annotated in the stub files
   *
   * @param node to check for
   * @return true if annotated, false otherwise
   */
  public boolean isPresentInStub(ExpressionTree node) {
    if (node != null) {
      Element elem = TreeUtils.elementFromTree(node);
      if (elem != null && isFromStubFile(elem)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the package name matches any of the annotated packages
   *
   * @param packageName to check for
   * @return true if matches, false otherwise
   */
  public boolean isAnnotatedPackage(String packageName) {
    for (String annotatedPackageName : ANNOTATED_PACKAGE_NAMES_LIST) {
      if (packageName.startsWith(annotatedPackageName)) {
        return true;
      }
    }
    return false;
  }

  public boolean returnsThis(MethodInvocationTree tree) {
    return this.getDeclAnnotation(TreeUtils.elementFromUse(tree), This.class) != null;
  }

  /**
   * Checks if the given tree may be tainted.
   *
   * @param tree The given tree.
   * @return True if the given tree is tainted, false otherwise.
   */
  public boolean mayBeTainted(Tree tree) {
    AnnotatedTypeMirror type = getAnnotatedType(tree);
    // If type is null, we should be conservative and assume it may be tainted.
    return type == null || !type.hasAnnotation(RUNTAINTED);
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

    /**
     * Visits all method invocations and updates {@link AnnotatedTypeMirror} according to the
     * argument and receiver annotations. If any of the arguments or the receiver is {@link
     * RTainted}, the {@link AnnotatedTypeMirror} is updated to be {@link RTainted}.
     *
     * @param node the node being visited
     * @param annotatedTypeMirror annotated return type of the method invocation
     */
    @Override
    public Void visitMethodInvocation(
        MethodInvocationTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      if (ENABLE_SANITIZER_CHECK) {
        // if the code is part of provided annotated packages or is present
        // in the stub files, then we don't need any custom handling for it.
        if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
          if (!hasReceiver(node) && node.getArguments().size() == 1) {
            annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
          } else {

          }
        }
      }
      if (ENABLE_LIBRARY_CHECK) {
        // ignore local methods
        if (hasReceiver(node)) {
          // if the code is part of provided annotated packages or is present
          // in the stub files, then we don't need any custom handling for it.
          if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
            if (!hasTaintedArgument(node) && !hasTaintedReceiver(node)) {
              annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
            }
          }
        }
      }

      return super.visitMethodInvocation(node, annotatedTypeMirror);
    }

    /**
     * Visits all new class creations and updates {@link AnnotatedTypeMirror} according to the
     * argument and receiver annotations. If any of the arguments or the receiver is {@link
     * RTainted}, the {@link AnnotatedTypeMirror} is updated to be {@link RTainted}.
     *
     * @param node the node being visited
     * @param annotatedTypeMirror annotated type of the new class
     */
    @Override
    public Void visitNewClass(NewClassTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      if (ENABLE_SANITIZER_CHECK) {
        // if the code is part of provided annotated packages or is present
        // in the stub files, then we don't need any custom handling for it.
        if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
          if (!hasReceiver(node) && node.getArguments().size() == 1) {
            annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
          }
        }
      }
      if (ENABLE_LIBRARY_CHECK) {
        // if the code is part of provided annotated packages or is present
        // in the stub files, then we don't need any custom handling for it.
        if (!hasAnnotatedPackage(node) && !isPresentInStub(node)) {
          if (!hasTaintedArgument(node) && !hasTaintedReceiver(node)) {
            annotatedTypeMirror.replaceAnnotation(RUNTAINTED);
          }
        }
      }
      return super.visitNewClass(node, annotatedTypeMirror);
    }

    @Override
    public Void visitIf(IfTree node, AnnotatedTypeMirror annotatedTypeMirror) {
      return super.visitIf(node, annotatedTypeMirror);
    }
  }
}
