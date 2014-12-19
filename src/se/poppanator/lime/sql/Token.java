/**
 * This module is an interface to the web services of
 * {@link http://www.lundalogik.se/ Lundalogik}s web services for Lime PRO.
 *
 * More info can be found at the {@link https://github.com/poppa/java-lime-pro
 * Github repository}.
 *
 * @copyright 2014 Pontus Östlund
 * @author    Pontus Östlund <poppanator@gmail.com>
 * @license   http://opensource.org/licenses/GPL-2.0 GPL License 2
 * @link      https://github.com/poppa Github
 * @package   se.poppanator.lime.sql
 * @version   0.1
 */
package se.poppanator.lime.sql;

import java.util.EnumSet;

/**
 * Internal class representning a SQL Token
 */
class Token
{
  /**
   * The different types of tokens
   */
  public enum Type {
    NONE,         KEYWORD,      OPERATOR,     VALUE,        COLUMN,
    PREDICATE,    TABLE,        GROUP_START,  GROUP_END,    LIMIT,
    LIMIT_FROM,   LIMIT_TO,     COUNT,        SELECT,       ORDER,
    BY,           SORT_ORDER,   ORDER_ASC,    ORDER_DESC,   SORT_KEY,
    TYPEHINT;
  }

  /**
   * Token type
   */
  public EnumSet<Type> type = EnumSet.of(Type.NONE);

  /**
   * The string value of the token
   */
  String value = null;

  /**
   * The string value in lower case
   */
  String lcValue = null;

  /**
   * The data type of the value if the token is of type {@link Type.VALUE}.
   */
  String datatype = null;

  /**
   * Constructor
   *
   * @param v
   *  A SQL string token
   */
  public Token(String v)
  {
    if (v == null || v.isEmpty())
      return;

    String lv = v.toLowerCase();

    this.value = v;

    if (Parser.isKeyword(lv)) {
      type = EnumSet.of(Type.KEYWORD);

      if (lv.equals("limit")) {
        type.add(Type.LIMIT);
      }
      else if (lv.equals("select")) {
        type.add(Type.SELECT);
      }
      else if (lv.equals("count")) {
        type.add(Type.COUNT);
      }
      else if (lv.equals("order")) {
        type.add(Type.ORDER);
      }
      else if (lv.equals("by")) {
        type.add(Type.BY);
      }
      else if (lv.equals("asc")) {
        type.add(Type.SORT_ORDER);
        type.add(Type.ORDER_ASC);
      }
      else if (lv.equals("desc")) {
        type.add(Type.SORT_ORDER);
        type.add(Type.ORDER_DESC);
      }
    }
    else if (Parser.isOperator(lv)) {
      type = EnumSet.of(Type.OPERATOR);
    }
    else if (!lv.isEmpty() && lv.charAt(0) == ':') {
      type = EnumSet.of(Type.TYPEHINT);
      value = value.substring(1);
      lv = lv.substring(1);
    }

    if (!lv.isEmpty()) {
      // String literate
      if (lv.charAt(0) == '\'' || lv.charAt(0) == '"') {
        type = EnumSet.of(Type.VALUE);
        value = value.substring(1, value.length() - 1);
        lcValue = value.toLowerCase();
        datatype = "string";

        if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
          datatype = "date";
        }
      }
      else if (lv.charAt(0) == '(') {
        type = EnumSet.of(Type.GROUP_START);
      }
      else if (lv.charAt(0) == ')') {
        type = EnumSet.of(Type.GROUP_END);
      }
      // Quoted column
      else if (lv.charAt(0) == '`') {
        value = value.substring(1, value.length() - 1);
        lcValue = value.toLowerCase();
      }
    }

    lcValue = value.toLowerCase();
  }

  /**
   * Set the type of the token
   *
   * @param t
   * @return
   *  The object being called
   */
  Token setType(Type t)
  {
    type = EnumSet.of(t);
    return this;
  }

  /**
   * Add a token type to the token's type
   * @param t
   * @return
   *  The object being called
   */
  Token addType(Type t)
  {
    if (type.contains(Type.NONE))
      type.remove(Type.NONE);

    type.add(t);

    return this;
  }

  /**
   * Lower case value equals...
   * @param args
   *  Arguments to match the lower case value against
   * @return
   *  True if lower case value matches any of the arguments
   */
  public boolean lveq(String ... args)
  {
    for (String arg : args) {
      if (lcValue.equals(arg)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Resolve the data type of a value token.
   * @return
   *  The object being called
   */
  public Token resolveDatatype()
  {
    if (datatype == null && value != null) {
      if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}"))
        datatype = "date";
      else
        datatype = "numeric";
    }

    return this;
  }

  /**
   * Check if the token is of type `what`
   * @param what
   * @return
   */
  public boolean isA(Type what)
  {
    return type.contains(what);
  }

  /**
   * Cast to string.
   * @return
   *  A string representation of the object. Only for debugging purposes.
   */
  @Override
  public String toString()
  {
    return "Token(\"" + value + "\", " + type + ")";
  }
}
