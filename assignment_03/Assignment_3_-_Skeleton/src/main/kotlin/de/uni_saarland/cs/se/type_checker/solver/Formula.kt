package de.uni_saarland.cs.se.type_checker.solver

import cafesat.api.FormulaBuilder
import cafesat.api.Formulas
import cafesat.api.Solver


/** Wrapper class for CafeSAT propositional formulas. */
open class Formula(internal val formula: Formulas.Formula) {

  /** Returns the negation of a formula. */
  operator fun not(): Formula = Formula(formula.`unary_$bang`())

  /** Returns the disjunction of two formulas. */
  infix fun or(other: Formula) = Formula(formula.`$bar$bar`(other.formula))

  /** Returns the conjunction of two formulas. */
  infix fun and(other: Formula) = Formula(formula.`$amp$amp`(other.formula))

  /** Returns the bi-implication of two formulas. */
  infix fun iff(other: Formula) = Formula(formula.iff(other.formula))

  override fun toString(): String {
    return formula.toString()
  }

  companion object {
    /** Constant formula `true`. */
    val True = Formula(Formulas.`True$`.`MODULE$`)

    /** Constant formula `false`. */
    val False = Formula(Formulas.`False$`.`MODULE$`)
  }
}

/** Wrapper class for CafeSAT propositional variables. */
class PropVar(prefix: String) : Formula(FormulaBuilder.propVar(prefix))

/** Wrapper for CafeSAT solver interface. */
object Solver {

  /** Checks whether the given formula is satisfiable. */
  fun isSatisfiable(f: Formula): Boolean =
    Solver.solveForSatisfiability(f.formula) !is scala.`None$`
}
