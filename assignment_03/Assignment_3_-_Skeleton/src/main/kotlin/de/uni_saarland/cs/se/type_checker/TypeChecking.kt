/** Some generic classes that are used by both, the simple type checker and the
 * variability-aware type checker.
 */
package de.uni_saarland.cs.se.type_checker

/** The result returned by the type checker.
 *
 * A result can be either a `Success` or a `Failure`. A `Success` object
 * contains the inferred type and a `Failure` object contains information about
 * the type error.
 *
 * @tparam ExpressionT
 *   the expressions of the language
 * @tparam TypeT
 *   the types of the language
 * @tparam ContextT
 *   the context used during type checking, i.e., type context and optionally
 *   variability context
 */
sealed class TypeCheckResult<ExpressionT, TypeT, ContextT>

/** Indicates a successful type check.
 *
 * @param t
 *   the type inferred during type checking
 */
data class Success<ExpressionT, TypeT, ContextT>(val t: TypeT) :
  TypeCheckResult<ExpressionT, TypeT, ContextT>()

/** Indicates a type error.
 *
 * @param expr
 *   the (sub) expression that is currently checked
 * @param context
 *   the current context
 * @param message
 *   a message describing the type error
 */
data class Failure<ExpressionT, TypeT, ContextT>(
  val expr: ExpressionT,
  val context: ContextT,
  val message: String = ""
) : TypeCheckResult<ExpressionT, TypeT, ContextT>() {
  override fun toString(): String =
    "Error @ $expr\n"+
    (when (message) {
      "" -> ""
      else -> "  Message: $message\n"
    }) + "  Context: $context"

  /** Ignore the message in equality checks. */
  override fun equals(other: Any?): Boolean =
    when (other) {
      is Failure<*, *, *> -> expr == other.expr && context == other.context
      else -> false
    }

  override fun hashCode(): Int {
    var result = expr.hashCode()
    result = 31 * result + context.hashCode()
    return result
  }
}


/** Type context tracking the types of identifiers.
 *
 * @tparam IdT
 *   type used for variables
 * @tparam TypeT
 *   the types of the language
 */
data class TypeContext<IdT, TypeT>(val mapping: Map<IdT, TypeT> = mapOf()) {

  /** Construct a TypeContext from a list of tuples. */
  constructor(vararg types: Pair<IdT, TypeT>) : this(types.toMap())

  /** Create an extended copy of this type context that sets the type for the
   * given variable.
   */
  fun withVar(id: IdT, value: TypeT): TypeContext<IdT, TypeT> =
    TypeContext(mapping + (id to value))

  /** Get the type for a given variable.
   */
  fun typeForVar(id: IdT): TypeT? = mapping[id]

  override fun toString(): String =
    mapping.map { (id, t) -> "($id: $t)" }.joinToString("\n")
}


/** The type checker interface.
 *
 * @tparam ExpressionT
 *   the expressions of the language
 * @tparam TypeT
 *   the types of the language
 * @tparam ContextT
 *   the context used during type checking, i.e., type context and optionally
 *   variability context
 */
interface TypeChecker<ExpressionT, TypeT, ContextT> {
  /** Determine the type of an expression given some context.
   *
   * @param expr
   *   the expression to type-check
   * @param context
   *   the context used for type checking
   */
  fun checkType(
    expr: ExpressionT,
    context: ContextT
  ): TypeCheckResult<ExpressionT, TypeT, ContextT>
}
