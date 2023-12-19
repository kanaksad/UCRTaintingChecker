package edu.ucr.cs.riple.taint.ucrtainting.serialization.visitors;

import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;
import edu.ucr.cs.riple.taint.ucrtainting.FoundRequired;
import edu.ucr.cs.riple.taint.ucrtainting.UCRTaintingAnnotatedTypeFactory;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Fix;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.Serializer;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.location.MethodLocation;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.location.MethodParameterLocation;
import edu.ucr.cs.riple.taint.ucrtainting.serialization.location.PolyMethodLocation;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import org.checkerframework.javacutil.TreeUtils;

/** Fix visitor for method return statements. */
public class MethodReturnVisitor extends SpecializedFixComputer {
  public MethodReturnVisitor(
      UCRTaintingAnnotatedTypeFactory factory, FixComputer fixComputer, Context context) {
    super(factory, fixComputer, context);
  }

  @Override
  public Set<Fix> visitMethod(MethodTree node, FoundRequired pair) {
    Element methodElement = TreeUtils.elementFromDeclaration(node);
    Serializer.log(pair);
    Serializer.log(methodElement);
    Fix onMethod = buildFixForElement(methodElement, pair);
    if (onMethod == null) {
      return Collections.emptySet();
    }
    if (!typeFactory.polyTaintInferenceEnabled()) {
      return Set.of(onMethod);
    }
    Set<Fix> ans = new HashSet<>();
    Set<Fix> onReturns = node.accept(new ReturnStatementVisitor(pair), fixComputer);
    Deque<Fix> workList = new ArrayDeque<>(onReturns);
    Set<Symbol> involvedElementsInReturnValueCreation = new HashSet<>();
    while (!workList.isEmpty()) {
      Fix fix = workList.pop();
      if (!fix.location.getKind().isLocalVariable()) {
        ans.add(fix);
      }
      Symbol involvedElement = fix.location.getTarget();
      if (involvedElementsInReturnValueCreation.contains(involvedElement)) {
        // already processed
        continue;
      }
      AssignmentScanner assignmentScanner = new AssignmentScanner(involvedElement, pair);
      involvedElementsInReturnValueCreation.add(involvedElement);
      pair.incrementDepth();
      Set<Fix> onAssignments = node.accept(assignmentScanner, fixComputer);
      pair.decrementDepth();
      workList.addAll(onAssignments);
    }
    Set<MethodParameterLocation> polymorphicAnnotations = new HashSet<>();
    Set<Fix> others = new HashSet<>();
    ans.forEach(
        fix -> {
          if (!(fix.location instanceof MethodParameterLocation)) {
            others.add(fix);
            return;
          }
          MethodParameterLocation param = (MethodParameterLocation) fix.location;
          if (param.enclosingMethod.equals(methodElement)) {
            polymorphicAnnotations.add(param);
          } else {
            others.add(fix);
          }
        });
    if (polymorphicAnnotations.isEmpty()) {
      return Set.of(onMethod);
    }
    Fix polymorphicFixOnMethod =
        new Fix(new PolyMethodLocation((MethodLocation) onMethod.location, polymorphicAnnotations))
            .toPoly();
    others.add(polymorphicFixOnMethod);
    return others;
  }

  abstract static class AccumulateScanner extends TreeScanner<Set<Fix>, FixComputer> {

    protected final FoundRequired pair;

    public AccumulateScanner(FoundRequired pair) {
      this.pair = pair;
    }

    @Override
    public Set<Fix> reduce(Set<Fix> r1, Set<Fix> r2) {
      if (r2 == null && r1 == null) {
        return Set.of();
      }
      Set<Fix> combined = new HashSet<>();
      if (r1 != null) {
        combined.addAll(r1);
      }
      if (r2 != null) {
        combined.addAll(r2);
      }
      return combined;
    }
  }

  static class AssignmentScanner extends AccumulateScanner {

    private final Symbol variable;

    public AssignmentScanner(Symbol variable, FoundRequired pair) {
      super(pair);
      this.variable = variable;
    }

    @Override
    public Set<Fix> visitAssignment(AssignmentTree node, FixComputer visitor) {
      Element element = TreeUtils.elementFromUse(node.getVariable());
      if (variable.equals(element)) {
        return node.getExpression().accept(visitor, pair);
      }
      return Set.of();
    }

    @Override
    public Set<Fix> visitVariable(VariableTree node, FixComputer visitor) {
      Element element = TreeUtils.elementFromDeclaration(node);
      if (variable.equals(element)) {
        if (node.getInitializer() == null) {
          return Set.of();
        }
        return node.getInitializer().accept(visitor, pair);
      }
      return Set.of();
    }
  }

  static class ReturnStatementVisitor extends AccumulateScanner {

    public ReturnStatementVisitor(FoundRequired pair) {
      super(pair);
    }

    @Override
    public Set<Fix> visitReturn(ReturnTree node, FixComputer visitor) {
      return node.getExpression().accept(visitor, pair);
    }
  }
}
