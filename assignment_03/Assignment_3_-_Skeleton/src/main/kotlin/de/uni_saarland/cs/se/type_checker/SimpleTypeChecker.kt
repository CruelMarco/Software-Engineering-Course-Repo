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
}
