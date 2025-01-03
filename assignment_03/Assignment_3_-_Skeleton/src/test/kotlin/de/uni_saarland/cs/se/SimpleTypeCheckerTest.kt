package de.uni_saarland.cs.se

import de.uni_saarland.cs.se.type_checker.Failure
import de.uni_saarland.cs.se.type_checker.SimpleTypeCheckResult
import de.uni_saarland.cs.se.type_checker.SimpleTypeChecker
import de.uni_saarland.cs.se.type_checker.SimpleTypeContext
import de.uni_saarland.cs.se.type_checker.Success
import de.uni_saarland.cs.se.type_checker.expression.BoolTy
import de.uni_saarland.cs.se.type_checker.expression.Const
import de.uni_saarland.cs.se.type_checker.expression.ExpressionBuilder
import de.uni_saarland.cs.se.type_checker.expression.False
import de.uni_saarland.cs.se.type_checker.expression.Id
import de.uni_saarland.cs.se.type_checker.expression.Identifier
import de.uni_saarland.cs.se.type_checker.expression.Let
import de.uni_saarland.cs.se.type_checker.expression.Num
import de.uni_saarland.cs.se.type_checker.expression.NumTy
import de.uni_saarland.cs.se.type_checker.expression.True
import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleTypeCheckerTest {
  @Test
  fun testTrueIsBool() {
    val testExpr = Const(True)
    val t        = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Success(BoolTy), t)
  }

  @Test
  fun testFalseIsBool() {
    val testExpr = Const(False)
    val t        = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Success(BoolTy), t)
  }

  @Test
  fun test42IsNum() {
    val testExpr = Const(Num(42))
    val t        = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Success(NumTy), t)
  }

  @Test
  fun testFailureIfVariableNotInContext() {
    val testExpr = Id(Identifier("x"))
    val failure  = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Failure(testExpr, SimpleTypeContext()), failure)
  }

  @Test
  fun testSmallerArgsMustBeNum() {
    val testExpr = with(ExpressionBuilder) { True _lt 1 }
    val failure  = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Failure(testExpr, SimpleTypeContext()), failure)
  }

  @Test
  fun testIfConditionMustBeBool() {
    val testExpr = with(ExpressionBuilder) { _if(True) _then True _else False }
    val t        = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Success(BoolTy), t)
  }

  @Test
  fun testIdInLetIsNotInContext() {
    val testExpr = with(ExpressionBuilder) { _let("x") _is 5 _in { "x" _lt 4 } }
    val context  = SimpleTypeContext(Identifier("x") to NumTy)
    val failure  = SimpleTypeChecker.checkType(testExpr, context)
    assertEquals<SimpleTypeCheckResult>(Failure(testExpr, context), failure)
  }

  @Test
  fun testAllLanguageFeatures() {
    val testExpr = with(ExpressionBuilder) {
      _let("x") _is 5 _in { _if(True) _then 1 _else "x" }
    }
    val t = SimpleTypeChecker.checkType(testExpr)
    assertEquals<SimpleTypeCheckResult>(Success(NumTy), t)
  }
}