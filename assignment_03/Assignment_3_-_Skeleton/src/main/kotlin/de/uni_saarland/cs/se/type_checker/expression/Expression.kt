/** This file implements our simple programming language as defined in the
 *  lecture.
 */
package de.uni_saarland.cs.se.type_checker.expression


/** Constants */
sealed interface Constant
data object True : Constant
data object False : Constant
data class Num(val value: Int) : Constant

/** Identifiers */
data class Identifier(val name: String) {
  override fun toString(): String = name
}

/** Types */
sealed interface Type
data object NumTy : Type
data object BoolTy : Type

/** Expressions for the language without variability. */
sealed interface Expression
data class Const(val c: Constant) : Expression
data class Id(val id: Identifier) : Expression
data class Smaller(val lhs: Expression, val rhs: Expression) : Expression
data class If(
  val condition: Expression,
  val thenExpr: Expression,
  val elseExpr: Expression
) : Expression

data class Let(
  val variable: Identifier,
  val varValue: Expression,
  val inExpr: Expression
) : Expression

/*
 * Everything below here is only there to allow writing expressions in a nicer
 * syntax but is irrelevant for implementing the type checker.
 */

object ExpressionBuilder {
  infix fun Expression._lt(expression: Expression): Smaller = Smaller(this, expression)
  infix fun Expression._lt(expression: Constant): Smaller = Smaller(this, Const(expression))
  infix fun Expression._lt(expression: String): Smaller = Smaller(this, Id(Identifier(expression)))
  infix fun Expression._lt(expression: Int): Smaller = Smaller(this, Const(Num(expression)))
  infix fun Constant._lt(expression: Expression): Smaller = Smaller(Const(this), expression)
  infix fun Constant._lt(expression: Constant): Smaller = Smaller(Const(this), Const(expression))
  infix fun Constant._lt(expression: String): Smaller = Smaller(Const(this), Id(Identifier(expression)))
  infix fun Constant._lt(expression: Int): Smaller = Smaller(Const(this), Const(Num(expression)))
  infix fun String._lt(expression: Expression): Smaller = Smaller(Id(Identifier(this)), expression)
  infix fun String._lt(expression: Constant): Smaller = Smaller(Id(Identifier(this)), Const(expression))
  infix fun String._lt(expression: String): Smaller = Smaller(Id(Identifier(this)), Id(Identifier(expression)))
  infix fun String._lt(expression: Int): Smaller = Smaller(Id(Identifier(this)), Const(Num(expression)))
  infix fun Int._lt(expression: Expression): Smaller = Smaller(Const(Num(this)), expression)
  infix fun Int._lt(expression: Constant): Smaller = Smaller(Const(Num(this)), Const(expression))
  infix fun Int._lt(expression: String): Smaller = Smaller(Const(Num(this)), Id(Identifier(expression)))
  infix fun Int._lt(expression: Int): Smaller = Smaller(Const(Num(this)), Const(Num(expression)))

  fun _if(cond: Expression): IfThen = IfThen(cond)
  fun _if(cond: Constant): IfThen = IfThen(Const(cond))
  fun _if(cond: String): IfThen = IfThen(Id(Identifier(cond)))
  fun _if(cond: Int): IfThen = IfThen(Const(Num(cond)))

  fun _let(varName: Identifier): LetIs = LetIs(varName)
  fun _let(varName: String): LetIs = LetIs(Identifier(varName))

  data class IfThen(private val cond: Expression) {
    infix fun _then(expr: () -> Expression): IfElse = IfElse(cond, expr())
    infix fun _then(expr: Constant): IfElse = IfElse(cond, Const(expr))
    infix fun _then(expr: String): IfElse = IfElse(cond, Id(Identifier(expr)))
    infix fun _then(expr: Int): IfElse = IfElse(cond, Const(Num(expr)))
  }

  data class IfElse(private val cond: Expression, private val thenExpr: Expression) {
    infix fun _else(expr: () -> Expression): If = If(cond, thenExpr, expr())
    infix fun _else(expr: Constant): If = If(cond, thenExpr, Const(expr))
    infix fun _else(expr: String): If = If(cond, thenExpr, Id(Identifier(expr)))
    infix fun _else(expr: Int): If = If(cond, thenExpr, Const(Num(expr)))
  }

  data class LetIs(private val varName: Identifier) {
    infix fun _is(expr: () -> Expression): LetIn = LetIn(varName, expr())
    infix fun _is(expr: Constant): LetIn = LetIn(varName, Const(expr))
    infix fun _is(expr: String): LetIn = LetIn(varName, Id(Identifier(expr)))
    infix fun _is(expr: Int): LetIn = LetIn(varName, Const(Num(expr)))
  }

  data class LetIn(private val varName: Identifier, private val varExpr: Expression) {
    infix fun _in(expr: () -> Expression): Let = Let(varName, varExpr, expr())
    infix fun _in(expr: Constant): Let = Let(varName, varExpr, Const(expr))
    infix fun _in(expr: String): Let = Let(varName, varExpr, Id(Identifier(expr)))
    infix fun _in(expr: Int): Let = Let(varName, varExpr, Const(Num(expr)))
  }
}
