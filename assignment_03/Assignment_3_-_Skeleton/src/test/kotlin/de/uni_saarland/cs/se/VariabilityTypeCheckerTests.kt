package de.uni_saarland.cs.se

import de.uni_saarland.cs.se.type_checker.Failure
import de.uni_saarland.cs.se.type_checker.VariabilityTypeChecker
import de.uni_saarland.cs.se.type_checker.vexpression.VExpression
import de.uni_saarland.cs.se.type_checker.Success
import de.uni_saarland.cs.se.type_checker.TypeContext
import de.uni_saarland.cs.se.type_checker.VariabilityContext
import de.uni_saarland.cs.se.type_checker.expression.BoolTy
import de.uni_saarland.cs.se.type_checker.expression.False
import de.uni_saarland.cs.se.type_checker.expression.Identifier
import de.uni_saarland.cs.se.type_checker.expression.Num
import de.uni_saarland.cs.se.type_checker.expression.NumTy
import de.uni_saarland.cs.se.type_checker.expression.True
import de.uni_saarland.cs.se.type_checker.solver.Formula
import de.uni_saarland.cs.se.type_checker.solver.PropVar
import de.uni_saarland.cs.se.type_checker.vexpression.Const
import de.uni_saarland.cs.se.type_checker.vexpression.Id
import de.uni_saarland.cs.se.type_checker.vexpression.Let
import de.uni_saarland.cs.se.type_checker.vexpression.VExpressionBuilder
import de.uni_saarland.cs.se.type_checker.vexpression.VType
import kotlin.test.Test
import kotlin.test.assertEquals

class VariabilityTypeCheckerTests {
  @Test
  fun testTrueIsBool() {
    val testExpr: VExpression = Const(True)
    val t = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Success(VType(mapOf(BoolTy to Formula.True))), t)
  }

  @Test
  fun testFalseIsBool() {
    val testExpr: VExpression = Const(False)
    val t = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Success(VType(mapOf(BoolTy to Formula.True))), t)
  }

  @Test
  fun test42IsNum() {
    val testExpr: VExpression = Const(Num(42))
    val t = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Success(VType(mapOf(NumTy to Formula.True))), t)
  }

  @Test
  fun testFailureIfVariableNotInContext() {
    val testExpr: VExpression = Id(Identifier("x"))
    val failure = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Failure(testExpr, VariabilityTypeChecker.createContext()), failure)
  }

  @Test
  fun testSmallerArgsMustBeNum() {
    val testExpr: VExpression = with(VExpressionBuilder) { "x" _lt 1 }
    val A = PropVar("A")
    val context = VariabilityTypeChecker.createContext(
      typeContext = TypeContext(Identifier("x") to VType(NumTy to A, BoolTy to !A))
    )
    val failure = VariabilityTypeChecker.checkType(testExpr, context)

    assertEquals(Failure(testExpr, context), failure)
  }

  @Test
  fun testIfConditionMustBeBool() {
    val testExpr: VExpression = with(VExpressionBuilder) {
      _if(True) _then True _else False
    }
    val t = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Success(VType(BoolTy to Formula.True)), t)
  }

  @Test
  fun testIdInLetIsNotInContext() {
    val testExpr: VExpression = with(VExpressionBuilder) {
      _let("x") _is 5 _in { "x" _lt 4 }
    }
    val context = VariabilityTypeChecker.createContext(
      typeContext = TypeContext(Identifier("x") to VType(NumTy to Formula.True))
    )
    val failure = VariabilityTypeChecker.checkType(testExpr, context)

    assertEquals(Failure(testExpr, context), failure)
  }

  @Test
  fun testChoiceExpressionType() {
    val A = PropVar("A")
    val B = PropVar("B")
    val testExpr: VExpression = with (VExpressionBuilder) {
      _choice(A or B, True, 5)
    }
    val t = VariabilityTypeChecker.checkType(testExpr)

    assertEquals(Success(VType(BoolTy to (A or B), NumTy to !(A or B))), t)
  }
}