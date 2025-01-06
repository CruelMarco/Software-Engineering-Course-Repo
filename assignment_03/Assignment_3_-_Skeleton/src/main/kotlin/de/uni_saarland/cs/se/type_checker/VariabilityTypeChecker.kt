// Author : Mohammad Shaique Solanki, Serial Number: 7062950 //

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

  private fun checkTTrue(context: VContext): VTypeCheckResult = Success(VType(BoolTy to context.variabilityContext.formula))

  private fun checkTFalse(context: VContext): VTypeCheckResult = Success(VType(BoolTy to context.variabilityContext.formula))

  private fun checkTNum(num: Num, context: VContext): VTypeCheckResult = Success(VType(NumTy to context.variabilityContext.formula))

  private fun checkTId(expr: Id, context: VContext): VTypeCheckResult {

    val type = context.typeContext.typeForVar(expr.id)

    return if (type != null) {

      Success(type)

    } else {

      Failure(expr, context, "undefined identifier")

    }

  }

  private fun checkTSmaller(expr: Smaller, context: VContext): VTypeCheckResult {

    val leftHandSide = checkType(expr.lhs, context)

    val rightHandSide = checkType(expr.rhs, context)

    return when {
      leftHandSide is Success && rightHandSide is Success  && leftHandSide.t.dom == setOf(NumTy) && rightHandSide.t.dom == setOf(NumTy) -> Success(VType(BoolTy to Formula.True))

      leftHandSide is Failure -> leftHandSide

      rightHandSide is Failure -> rightHandSide

      else -> Failure(expr, context, "args of smaller must be numbers")

    }

  }

  private fun checkTIf(expr: If, context: VContext): VTypeCheckResult {

    val con_type = checkType(expr.condition, context)

    val then_type = checkType(expr.thenExpr, context)

    val else_type = checkType(expr.elseExpr, context)

    return when {

      con_type is Success && con_type.t.dom == setOf(BoolTy) -> when {

        then_type is Success && else_type is Success && then_type.t == else_type.t -> then_type

        then_type is Failure -> then_type

        else_type is Failure -> else_type

        else -> Failure(expr, context)
      }
      con_type is Failure -> con_type

      else -> Failure(expr, context, "inputs to checkTIf are not boolean")
    }
  }

  private fun checkTLet(expr: Let, context: VContext): VTypeCheckResult {

    if (context.typeContext.typeForVar(expr.variable) != null) {

      return Failure(expr, context, "this variable is already set")

    }
    return when (val variableTypeReturn = checkType(expr.varValue, context)) {

      is Success -> {

        val extendedContext = context.copy(

          typeContext = context.typeContext.withVar(expr.variable, variableTypeReturn.t)

        )
        checkType(expr.inExpr, extendedContext)

      }

      is Failure -> variableTypeReturn

    }

  }

  private fun checkTChoice(expr: Choice, context: VContext): VTypeCheckResult {

    val firstCondition = context.variabilityContext.formula and expr.presenceCondition

    val firstContext = context.copy(variabilityContext = VariabilityContext(firstCondition))

    val firstBranch = checkType(expr.trueChoice, firstContext)

    val secondCondition = context.variabilityContext.formula and !expr.presenceCondition

    val secondContext = context.copy(variabilityContext = VariabilityContext(secondCondition))

    val secondBranch = checkType(expr.falseChoice, secondContext)

    return when {

      firstBranch is Success && secondBranch is Success -> {

        val combinedType = VType(

          BoolTy to (expr.presenceCondition),

          NumTy to !(expr.presenceCondition)

        )

        Success(combinedType)
      }

      firstBranch is Failure -> firstBranch

      secondBranch is Failure -> secondBranch

      else -> Failure(expr, context, "all branches of choice must have similar type")

    }

  }



}
