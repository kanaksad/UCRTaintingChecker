package edu.ucr.cs.riple.taint.ucrtainting.serialization;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.com.google.common.collect.ImmutableSet;
import org.checkerframework.framework.source.SourceVisitor;
import org.checkerframework.javacutil.TreeUtils;

/** This class is used to serialize the errors and the fixes for the errors. */
public class SerializationService {

  /**
   * This method is called when a warning or error is reported by the checker and serialized the
   * error along the set of required fixes to resolve the error if exists.
   *
   * @param source the source of the error
   * @param messageKey the key of the error message
   * @param args the arguments of the error message
   * @param visitor the visitor that is visiting the source
   * @param processingEnvironment the processing environment
   */
  public void serializeError(
      Object source,
      String messageKey,
      Object[] args,
      SourceVisitor<?, ?> visitor,
      JavacProcessingEnvironment processingEnvironment) {
    // TODO: for TreeChecker instance below, use the actual API which checks if the tree is
    // @Tainted. For now, we pass tree -> true, to serialize a fix for all expressions on the right
    // hand side of the assignment.
    Set<Fix> resolvingFixes =
        checkErrorIsFixable(source, messageKey)
            ? generateFixesForError(
                (Tree) source,
                messageKey,
                tree -> true,
                visitor.getCurrentPath(),
                processingEnvironment)
            : ImmutableSet.of();
    Error error = new Error(messageKey, String.format(messageKey, args), resolvingFixes);
    // TODO: serialize the error, will be implemented in the next PR, once the format
    // is finalized.
  }

  /**
   * Generates the fixes for the given tree if exists.
   *
   * @param tree The given tree.
   * @param messageKey The key of the error message.
   * @param treeChecker The tree checker to check if a tree requires a fix.
   * @param path The path of the tree.
   * @param processingEnvironment The processing environment.
   */
  public Set<Fix> generateFixesForError(
      Tree tree,
      String messageKey,
      TreeChecker treeChecker,
      TreePath path,
      JavacProcessingEnvironment processingEnvironment) {
    if (isInheritanceViolationError(messageKey)) {
      boolean isParamOverrideError = messageKey.equals("override.param");
      // if the error is an inheritance violation error, we can generate the fix directly from the
      // symbol and does not require a visitor.
      Types types = Types.instance(processingEnvironment.getContext());
      Element treeElement = TreeUtils.elementFromTree(tree);
      if (treeElement == null) {
        return ImmutableSet.of();
      }
      Symbol.MethodSymbol executableElement =
          (Symbol.MethodSymbol)
              (isParamOverrideError ? treeElement.getEnclosingElement() : TreeUtils.elementFromTree(path.getLeaf()));
      if(executableElement == null) {
        return ImmutableSet.of();
      }
      Symbol.MethodSymbol overriddenMethod =
          Utility.getClosestOverriddenMethod(executableElement, types);
      if (!isParamOverrideError) {
        // TODO: make the fix here from the overridden method.
      } else {
        int paramIndex = executableElement.getParameters().indexOf((Symbol.VarSymbol) treeElement);
        // TODO: make the fix here from the overridden method and its parameter index.
        Symbol toBeAnnotated = overriddenMethod.getParameters().get(paramIndex);
      }
    }
    FixVisitor fixVisitor = new FixVisitor(treeChecker, path);
    Set<Fix> resolvingFixes = new HashSet<>();
    fixVisitor.visit(tree, resolvingFixes);
    return resolvingFixes;
  }

  /**
   * Checks if the error is fixable with annotation injections on the source code elements.
   *
   * @param source The source of the error.
   * @param messageKey The key of the error message.
   * @return True, if the error is fixable, false otherwise.
   */
  private static boolean checkErrorIsFixable(Object source, String messageKey) {
    if (!(source instanceof Tree)) {
      // For all cases where the source is not a tree, we return false for now.
      return false;
    }
    switch (messageKey) {
      case "override.param":
      case "override.return":
      case "assignment":
      case "return":
      case "argument":
        return true;
      default:
        // TODO: investigate if there are other cases where the error is fixable.
        // For all other cases, return false.
        return false;
    }
  }

  /**
   * Checks if the error is an inheritance violation error.
   *
   * @param messageKey The key of the error message.
   * @return True, if the error is an inheritance violation error, false otherwise.
   */
  private static boolean isInheritanceViolationError(String messageKey) {
    return messageKey.equals("override.param") || messageKey.equals("override.return");
  }
}
