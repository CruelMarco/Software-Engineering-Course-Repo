package de.uni_saarland.cs.se.type_checker

import de.uni_saarland.cs.se.type_checker.expression.BoolTy
import de.uni_saarland.cs.se.type_checker.expression.False
import de.uni_saarland.cs.se.type_checker.expression.Identifier
import de.uni_saarland.cs.se.type_checker.expression.Num
import de.uni_saarland.cs.se.type_checker.expression.NumTy
import de.uni_saarland.cs.se.type_checker.expression.True
import de.uni_saarland.cs.se.type_checker.solver.Formula
import de.uni_saarland.cs.se.type_checker.solver.Solver
import de.uni_saarland.cs.se.type_checker.vexpression.Choice
import de.uni_saarland.cs.se.type_checker.vexpression.Const
import de.uni_saarland.cs.se.type_checker.vexpression.Id
import de.uni_saarland.cs.se.type_checker.vexpression.If
import de.uni_saarland.cs.se.type_checker.vexpression.Let
import de.uni_saarland.cs.se.type_checker.vexpression.Smaller
import de.uni_saarland.cs.se.type_checker.vexpression.VExpression
import de.uni_saarland.cs.se.type_checker.vexpression.VType

/** Variability context tracking the current presence condition. */
data class VariabilityContext(val formula: Formula) {

  /** Make equality consider logical equality of formulas. */
  override fun equals(other: Any?): Boolean =
    when (other) {
      is VariabilityContext ->
        !Solver.isSatisfiable(!(formula iff other.formula))

      else -> false
    }

  override fun toString(): String = formula.toString()

  companion object {

    /** Creates an empty variability context. */
    fun emptyContext(): VariabilityContext = VariabilityContext(Formula.True)
  }
}

/** Type alias for type context */
typealias VTypeContext = TypeContext<Identifier, VType>

/** Class for context (= variability context + type context) */
data class VContext(
  val variabilityContext: VariabilityContext,
  val typeContext: VTypeContext
)

/** Type alias for result */
typealias VTypeCheckResult = TypeCheckResult<VExpression, VType, VContext>


/** Type checker implementation for the language with variability. */
class VariabilityTypeChecker : TypeChecker<VExpression, VType, VContext> {
  companion object {

    /** Type-check a single expression.
     */
    fun checkType(
      expr: VExpression,
      context: VContext = createContext()
    ): VTypeCheckResult = VariabilityTypeChecker().checkType(expr, context)

    /** Simplify creation of variability context + type context. */
    fun createContext(
      variabilityContext: VariabilityContext = VariabilityContext.emptyContext(),
      typeContext: VTypeContext = TypeContext()
    ): VContext = VContext(variabilityContext, typeContext)
  }

  override fun checkType(
    expr: VExpression,
    context: VContext
  ): VTypeCheckResult =
    when (expr) {
      is Const -> when (val c = expr.c) {
        True -> checkTTrue(context)
        False -> checkTFalse(context)
        is Num -> checkTNum(c, context)
      }

      is Id -> checkTId(expr, context)
      is Smaller -> checkTSmaller(expr, context)
      is If -> checkTIf(expr, context)
      is Let -> checkTLet(expr, context)
      is Choice -> checkTChoice(expr, context)
    }
  
  // TODO: implement task b)
}
