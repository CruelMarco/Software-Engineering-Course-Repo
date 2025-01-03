/** This file implements our simple programming language with variability as
 *  defined in the lecture.
 */
package de.uni_saarland.cs.se.type_checker.vexpression

import de.uni_saarland.cs.se.type_checker.expression.Constant
import de.uni_saarland.cs.se.type_checker.expression.Num
import de.uni_saarland.cs.se.type_checker.expression.Identifier
import de.uni_saarland.cs.se.type_checker.expression.Type
import de.uni_saarland.cs.se.type_checker.solver.Formula
import de.uni_saarland.cs.se.type_checker.solver.Solver


/** Expressions for the language with variability. */
sealed interface VExpression
data class Const(val c: Constant) : VExpression
data class Id(val id: Identifier) : VExpression
data class Smaller(val lhs: VExpression, val rhs: VExpression) : VExpression
data class If(
  val condition: VExpression,
  val thenExpr: VExpression,
  val elseExpr: VExpression
) : VExpression

data class Let(
  val variable: Identifier,
  val varValue: VExpression,
  val inExpr: VExpression
) : VExpression

data class Choice(
  val presenceCondition: Formula,
  val trueChoice: VExpression,
  val falseChoice: VExpression
) : VExpression


/** VType: Types with variability.
 *
 *  VTypes map types to their presence condition.
 */
data class VType(val types: Map<Type, Formula> = mapOf()) {

  /** Construct VType from a list of tuples. */
  constructor(vararg types: Pair<Type, Formula>) : this(types.toMap())

  /** Returns the domain of this VType. */
  val dom: Set<Type> = types.keys

  /** Retrieve the formula associated with a certain type. */
  fun formulaForType(t: Type): Formula? = types[t]

  override fun toString(): String =
    types.map { (t, pc) -> "($t => $pc)" }.joinToString("\n")


  /** Make equality consider logical equality of formulas. */
  override fun equals(other: Any?): Boolean =
    when (other) {
      is VType -> {
        dom.union(other.dom)
          .all { t ->
            val formulaA = formulaForType(t) ?: Formula.False
            val formulaB = other.formulaForType(t) ?: Formula.False
            !Solver.isSatisfiable(!(formulaA iff formulaB))
          }
      }

      else -> false
    }
}

/*
 * Everything below here is only there to allow writing expressions in a nicer
 * syntax but is irrelevant for implementing the type checker.
 */

object VExpressionBuilder {

  fun _choice(presenceCondition: Formula,
              trueChoice: VExpression,
              falseChoice: VExpression
  ): VExpression = Choice(presenceCondition, trueChoice, falseChoice)

  fun _choice(
    presenceCondition: Formula,
    trueChoice: VExpression,
    falseChoice: Constant
  ): VExpression = Choice(presenceCondition, trueChoice, Const(falseChoice))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: VExpression,
    falseChoice: String
  ): VExpression = Choice(presenceCondition, trueChoice, Id(Identifier(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: VExpression,
    falseChoice: Int
  ): VExpression = Choice(presenceCondition, trueChoice, Const(Num(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Constant,
    falseChoice: VExpression
  ): VExpression = Choice(presenceCondition, Const(trueChoice), falseChoice)

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Constant,
    falseChoice: Constant
  ): VExpression = Choice(presenceCondition, Const(trueChoice), Const(falseChoice))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Constant,
    falseChoice: String
  ): VExpression = Choice(presenceCondition, Const(trueChoice), Id(Identifier(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Constant,
    falseChoice: Int
  ): VExpression = Choice(presenceCondition, Const(trueChoice), Const(Num(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: String,
    falseChoice: VExpression
  ): VExpression = Choice(presenceCondition, Id(Identifier(trueChoice)), falseChoice)

  fun _choice(
    presenceCondition: Formula,
    trueChoice: String,
    falseChoice: Constant
  ): VExpression = Choice(presenceCondition, Id(Identifier(trueChoice)), Const(falseChoice))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: String,
    falseChoice: String
  ): VExpression = Choice(presenceCondition, Id(Identifier(trueChoice)), Id(Identifier(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: String,
    falseChoice: Int
  ): VExpression = Choice(presenceCondition, Id(Identifier(trueChoice)), Const(Num(falseChoice)))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Int,
    falseChoice: VExpression
  ): VExpression = Choice(presenceCondition, Const(Num(trueChoice)), falseChoice)

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Int,
    falseChoice: Constant
  ): VExpression = Choice(presenceCondition, Const(Num(trueChoice)), Const(falseChoice))

  fun _choice(
    presenceCondition: Formula,
    trueChoice: Int,
    falseChoice: String
  ): VExpression = Choice(presenceCondition, Const(Num(trueChoice)), Id(Identifier(falseChoice)))

  fun _choice(presenceCondition: Formula, trueChoice: Int,
              falseChoice: Int
  ): VExpression = Choice(presenceCondition, Const(Num(trueChoice)), Const(Num(falseChoice)))

  infix fun VExpression._lt(expression: VExpression): Smaller = Smaller(this, expression)
  infix fun VExpression._lt(expression: Constant): Smaller = Smaller(this, Const(expression))
  infix fun VExpression._lt(expression: String): Smaller = Smaller(this, Id(Identifier(expression)))
  infix fun VExpression._lt(expression: Int): Smaller = Smaller(this, Const(Num(expression)))
  infix fun Constant._lt(expression: VExpression): Smaller = Smaller(Const(this), expression)
  infix fun Constant._lt(expression: Constant): Smaller = Smaller(Const(this), Const(expression))
  infix fun Constant._lt(expression: String): Smaller = Smaller(Const(this), Id(Identifier(expression)))
  infix fun Constant._lt(expression: Int): Smaller = Smaller(Const(this), Const(Num(expression)))
  infix fun String._lt(expression: VExpression): Smaller = Smaller(Id(Identifier(this)), expression)
  infix fun String._lt(expression: Constant): Smaller = Smaller(Id(Identifier(this)), Const(expression))
  infix fun String._lt(expression: String): Smaller = Smaller(Id(Identifier(this)), Id(Identifier(expression)))
  infix fun String._lt(expression: Int): Smaller = Smaller(Id(Identifier(this)), Const(Num(expression)))
  infix fun Int._lt(expression: VExpression): Smaller = Smaller(Const(Num(this)), expression)
  infix fun Int._lt(expression: Constant): Smaller = Smaller(Const(Num(this)), Const(expression))
  infix fun Int._lt(expression: String): Smaller = Smaller(Const(Num(this)), Id(Identifier(expression)))
  infix fun Int._lt(expression: Int): Smaller = Smaller(Const(Num(this)), Const(Num(expression)))

  fun _if(cond: VExpression): IfThen = IfThen(cond)
  fun _if(cond: Constant): IfThen = IfThen(Const(cond))
  fun _if(cond: String): IfThen = IfThen(Id(Identifier(cond)))
  fun _if(cond: Int): IfThen = IfThen(Const(Num(cond)))

  fun _let(varName: Identifier): LetIs = LetIs(varName)
  fun _let(varName: String): LetIs = LetIs(Identifier(varName))

  data class IfThen(private val cond: VExpression) {
    infix fun _then(expr: () -> VExpression): IfElse = IfElse(cond, expr())
    infix fun _then(expr: Constant): IfElse = IfElse(cond, Const(expr))
    infix fun _then(expr: String): IfElse = IfElse(cond, Id(Identifier(expr)))
    infix fun _then(expr: Int): IfElse = IfElse(cond, Const(Num(expr)))
  }

  data class IfElse(private val cond: VExpression, private val thenExpr: VExpression) {
    infix fun _else(expr: () -> VExpression): If = If(cond, thenExpr, expr())
    infix fun _else(expr: Constant): If = If(cond, thenExpr, Const(expr))
    infix fun _else(expr: String): If = If(cond, thenExpr, Id(Identifier(expr)))
    infix fun _else(expr: Int): If = If(cond, thenExpr, Const(Num(expr)))
  }

  data class LetIs(private val varName: Identifier) {
    infix fun _is(expr: () -> VExpression): LetIn = LetIn(varName, expr())
    infix fun _is(expr: Constant): LetIn = LetIn(varName, Const(expr))
    infix fun _is(expr: String): LetIn = LetIn(varName, Id(Identifier(expr)))
    infix fun _is(expr: Int): LetIn = LetIn(varName, Const(Num(expr)))
  }

  data class LetIn(private val varName: Identifier, private val varExpr: VExpression) {
    infix fun _in(expr: () -> VExpression): Let = Let(varName, varExpr, expr())
    infix fun _in(expr: Constant): Let = Let(varName, varExpr, Const(expr))
    infix fun _in(expr: String): Let = Let(varName, varExpr, Id(Identifier(expr)))
    infix fun _in(expr: Int): Let = Let(varName, varExpr, Const(Num(expr)))
  }
}
