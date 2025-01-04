/** Type checking without variability.
 */
package de.uni_saarland.cs.se.type_checker

import de.uni_saarland.cs.se.type_checker.expression.BoolTy
import de.uni_saarland.cs.se.type_checker.expression.Const
import de.uni_saarland.cs.se.type_checker.expression.Expression
import de.uni_saarland.cs.se.type_checker.expression.False
import de.uni_saarland.cs.se.type_checker.expression.Id
import de.uni_saarland.cs.se.type_checker.expression.Identifier
import de.uni_saarland.cs.se.type_checker.expression.If
import de.uni_saarland.cs.se.type_checker.expression.Let
import de.uni_saarland.cs.se.type_checker.expression.Num
import de.uni_saarland.cs.se.type_checker.expression.NumTy
import de.uni_saarland.cs.se.type_checker.expression.Smaller
import de.uni_saarland.cs.se.type_checker.expression.True
import de.uni_saarland.cs.se.type_checker.expression.Type
import kotlin.math.exp


/** Type alias for type context. */
typealias SimpleTypeContext = TypeContext<Identifier, Type>

/** Type alias for result. */
typealias SimpleTypeCheckResult = TypeCheckResult<Expression, Type, SimpleTypeContext>

/** Type checker implementation for the language without variability. */
class SimpleTypeChecker : TypeChecker<Expression, Type, SimpleTypeContext> {
  companion object {
    /** Type-check a single expression. */
    fun checkType(
      expr: Expression,
      context: SimpleTypeContext = TypeContext()
    ): SimpleTypeCheckResult = SimpleTypeChecker().checkType(expr, context)
  }

  override fun checkType(expr: Expression, context: SimpleTypeContext): SimpleTypeCheckResult =
    when (expr) {
      is Const -> when (expr.c) {
        True -> checkTTrue()
        False -> checkTFalse()
        is Num -> checkTNum()
      }

      is Id -> checkTId(expr, context)
      is Smaller -> checkTSmaller(expr, context)
      is If -> checkTIf(expr, context)
      is Let -> checkTLet(expr, context)
    }

  // TODO: implement task a)

  private fun checkTTrue(): SimpleTypeCheckResult = Success(BoolTy)

  private fun checkTFalse(): SimpleTypeCheckResult = Success(BoolTy)

  private fun checkTNum() : SimpleTypeCheckResult = Success(NumTy)

  private fun checkTId(expr: Id, context: SimpleTypeContext): SimpleTypeCheckResult {

    val type = context.typeForVar(expr.id)

    return if (type != null) {

      Success(type)

    } else {

      Failure(expr, context, "Undefined identifier: ${expr.id}")

    }

  }

  private fun checkTSmaller(expr: Smaller, context: SimpleTypeContext): SimpleTypeCheckResult {

    val leftHandSide = checkType(expr.lhs , context)

    val rightHandSide = checkType(expr.rhs as Expression, context)

    return when{

      leftHandSide is Success && rightHandSide is Success && leftHandSide.t == NumTy && rightHandSide.t == NumTy -> Success(BoolTy)

      leftHandSide is Failure -> leftHandSide

      rightHandSide is Failure -> rightHandSide

      else -> Failure(expr , context , "inputs to checkTSmaller are not numbers")

    }


  }

  private fun checkTIf(expr: If, context: SimpleTypeContext): SimpleTypeCheckResult {

    val con_type = checkType(expr.condition, context)

    val then_type = checkType(expr.thenExpr, context)

    val else_type = checkType(expr.elseExpr, context)

    return when {

      con_type is Success && con_type.t == BoolTy -> when {

        then_type is Success && else_type is Success && then_type.t == else_type.t -> then_type

        then_type is Failure -> then_type

        else_type is Failure -> else_type

        else -> Failure(expr , context)

      }

      con_type is Failure -> con_type

      else -> Failure(expr, context, "inputs to checkTIf are not boolean")

    }
  }
  private fun checkTLet(expr:Let, context: SimpleTypeContext): SimpleTypeCheckResult {

    


  }




}
