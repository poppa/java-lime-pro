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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import se.poppanator.lime.xml.Builder;
import se.poppanator.lime.xml.Node;

/**
 * Class for converting an SQL query into a Lime XML query
 *
 * @author ponost
 */
public class Parser
{
  /**
   * SQL keywords
   */
  private static Set<String> keywords;
  /**
   * SQL operators
   */
  private static Set<String> operators;

  /**
   * Constructor
   */
  public Parser()
  {
    Parser.keywords = new HashSet<String>() {{
      add("select");  add("distinct");  add("from");
      add("where");   add("limit");     add("count");
      add("order");   add("by");        add("asc");
      add("desc");
    }};

    Parser.operators = new HashSet<String>() {{
      add("!");       add("=");         add("!=");
      add("<");       add(">");         add(">=");
      add("<=");      add("is");        add("like");
      add("and");     add("or");        add("in");
      add("not");     add("any");       add("all");
    }};
  }

  /**
   * Check if `word` is a SQL keyword
   * @param word
   * @return
   */
  public static boolean isKeyword(String word)
  {
    return keywords.contains(word);
  }

  /**
   * Check if `op` is a SQL operator
   * @param op
   * @return
   */
  public static boolean isOperator(String op)
  {
    return operators.contains(op);
  }

  /**
   * Parse `sql` into a {@link Node} object.
   * @param sql
   * @return
   * @throws Exception
   */
  public static Node sql(String sql) throws Exception
  {
    return new Parser().parse(sql);
  }

  /**
   * Parse the SQL query `sql` and turn it into a Lime XML query
   *
   * @param sql
   * @return
   * @throws java.lang.Exception
   */
  public Node parse(String sql) throws Exception
  {
    ArrayList<Token> tokens = this.tokenize(this.split(sql));
    tokens.add(new Token(null)); // Sentinel

    Token t, andor = null;
    int pos = 0;
    int sortIndex = 0;
    String sortOrder = null;
    String table = null;
    ArrayList<Object> fields = new ArrayList<>();
    ArrayList<Node> conds = new ArrayList<>();
    HashMap<String,String> qattr = new HashMap<>();
    HashMap<String,String> sort = new HashMap<>();

    while (true) {
      t = tokens.get(pos);
      if (t.isA(Token.Type.NONE))
        break;

      if (t.isA(Token.Type.COLUMN)) {
        while (t.isA(Token.Type.COLUMN)) {
          fields.add(Builder.field(t.value));
          t = tokens.get(++pos);
        }
      }
      else if (t.isA(Token.Type.LIMIT_TO)) {
        qattr.put("top", t.value);
      }
      else if (t.isA(Token.Type.LIMIT_FROM)) {
        qattr.put("first", t.value);
      }
      else if (t.isA(Token.Type.KEYWORD) && t.lveq("distinct")) {
        qattr.put("distinct", "1");
      }
      else if (t.isA(Token.Type.COUNT)) {
        qattr.put("count", t.value);
      }
      else if (t.isA(Token.Type.TABLE)) {
        table = t.value;
      }
      else if (t.isA(Token.Type.SORT_KEY)) {
        sort.put(t.value, Integer.toString(++sortIndex));
      }
      else if (t.isA(Token.Type.SORT_ORDER)) {
        sortOrder = t.value;
      }
      else if (t.isA(Token.Type.OPERATOR) && t.lveq("and", "or")) {
        andor = t;
        pos++;
        continue;
      }
      else if (t.isA(Token.Type.PREDICATE)) {
        Token op = tokens.get(++pos);
        // if the next token also is an operator, meaning we're dealing with
        // something like NOT IN
        Token op2 = tokens.get(pos + 1);

        String opval = op.value;

        if (op2.isA(Token.Type.OPERATOR)) {
          opval += " " + op2.value;
          pos++;
        }

        Token val = tokens.get(++pos);

        if (!val.isA(Token.Type.VALUE))
          throw new Exception("Expected a value Token but got " + val);

        String valval = val.value;
        if (op.lveq("like") && !valval.isEmpty()) {
          if (valval.charAt(0) == '%') {
            valval = valval.substring(1);
            opval = "%" + opval;
          }
          if (valval.charAt(valval.length()-1) == '%') {
            opval += "%";
            valval = valval.substring(0, valval.length()-1);
          }
        }

        HashMap<String,String> attr = new HashMap<>();
        attr.put("operator", opval);

        if (tokens.get(pos + 1).isA(Token.Type.TYPEHINT)) {
          val.datatype = tokens.get(pos + 1).value;
          pos++;
        }

        if (andor != null && andor.lveq("or"))
          attr.put("or", "1");

        ArrayList<Node> cn = new ArrayList<>();
        cn.add(Builder.exp("field", t.value));
        cn.add(Builder.exp(val.datatype, valval));
        conds.add(Builder.condition(attr, cn));
      }
      else if (t.isA(Token.Type.GROUP_START) || t.isA(Token.Type.GROUP_END)) {
        HashMap<String,String> attr = new HashMap<>();
        if (andor != null && andor.lveq("or"))
          attr.put("or", "1");

        ArrayList<Node> al = new ArrayList<>();
        al.add(Builder.exp(t.value));
        conds.add(Builder.condition(attr, al));
      }

      //System.out.println("Token: " + t);

      andor = null;
      pos++;
    }

    if (table == null)
      throw new Exception("No table name given in SQL query!");

    if (!qattr.containsKey("top") && qattr.containsKey("first")) {
      qattr.put("top", qattr.get("first"));
      qattr.remove("first");
    }

    ArrayList<Node> q = new ArrayList<>();
    q.add(Builder.table(table));

    if (conds.size() > 0)
      q.add(Builder.conditions(conds));

    if (fields.size() > 0) {
      if (sort.size() > 0) {
        if (sortOrder == null) sortOrder = "ASC";

        for (int i = 0; i < fields.size(); i++) {
          Node n = (Node) fields.get(i);
          if (sort.containsKey((String) n.getValue())) {
            HashMap<String,String> h = new HashMap<>();
            h.put("field", (String) n.getValue());
            h.put("sortorder", sortOrder);
            h.put("sortindex", sort.get((String) n.getValue()));

            fields.set(i, h);
          }
        }
      }

      q.add(Builder.fields(fields));
    }

    return Builder.query(q, qattr);
  }

  /**
   * Turn the list of string tokens into {@link Token} objects.
   *
   * @param s
   * @return
   * @throws Exception
   */
  private ArrayList<Token> tokenize(ArrayList<String> s) throws Exception
  {
    // Add sentinels so we don't peek or look behind out of range
    s.add(null);
    s.add(0, null);

    ArrayList<Token> tokens;
    tokens = new ArrayList<>();
    tokens.add(new Token(null)); // Add a sentinel

    int pos = 1;

    while (true) {
      String word = s.get(pos);

      if (word == null) {
        tokens.remove(0);
        return tokens;
      }

      Token t = new Token(word);
      Token p = tokens.get(pos-1);

      if (t.isA(Token.Type.NONE)) {
        if (p.isA(Token.Type.COLUMN) ||
            (p.isA(Token.Type.KEYWORD) &&
             p.lveq("select", "distinct", "count")))
        {
          t.addType(Token.Type.COLUMN);
        }
        else if (p.isA(Token.Type.KEYWORD) && p.lveq("from")) {
          t.addType(Token.Type.TABLE);
        }
        else if ((p.isA(Token.Type.KEYWORD) && p.lveq("where")) ||
                 (p.isA(Token.Type.OPERATOR) && p.lveq("and", "or")) ||
                 p.isA(Token.Type.GROUP_START))
        {
          t.addType(Token.Type.PREDICATE);
        }
        else if (p.isA(Token.Type.OPERATOR)) {
          t.setType(Token.Type.VALUE);
          t.resolveDatatype();
        }
        else if (p.isA(Token.Type.LIMIT)) {
          t.setType(Token.Type.LIMIT_FROM);
        }
        else if (p.isA(Token.Type.LIMIT_FROM)) {
          t.setType(Token.Type.LIMIT_TO);
        }
        else if (p.isA(Token.Type.BY) || p.isA(Token.Type.SORT_KEY)) {
          t.setType(Token.Type.SORT_KEY);
        }
        else {
          throw new Exception("Unresolved token type " + t + "! Previous " +
                              "token was " + p);
        }
      }

      tokens.add(t);
      pos += 1;
    }
  }

  /**
   * Turn the SQL query into a list of string tokens
   *
   * @param s
   * @return
   * @throws Exception
   */
  @SuppressWarnings("empty-statement")
  private ArrayList<String> split(String s) throws Exception
  {
    // Sentinel so we don't peek beyond the string end
    s += "\0";
    ArrayList<String> ret = new ArrayList<>();

    int pos = 0;

    while (true) {
      int start = pos;
      char c = s.charAt(pos);

      switch (c) {
        case '\0': return ret;

        case '\r':
          pos += 1;
          if (s.charAt(pos+1) == '\n') pos += 1;
          continue;

        case '\n':
          pos += 1;
          if (s.charAt(pos+1) == '\r') pos += 1;
          continue;

        case ' ':
        case '\t':
          while (s.charAt(++pos) == '\t' || s.charAt(pos) == ' ')
            ;
          continue;

        case ',':
          pos += 1;
          continue;

        case '`':
        case '\'':
        case '"':
          pos += 1;
          while (true) {
            char d = s.charAt(pos);
            if (d == '\0') {
              throw new Exception("Unterminated string literal!");
            }

            if (d == c) {
              if (s.charAt(pos-1) != '\\') {
                pos += 1;
                break;
              }
            }

            pos += 1;
          }
          break;

        case '!':
        case '<':
        case '>':
          if (s.charAt(pos+1) == '=')
            pos += 2;
          break;

        /*
        Range a..Z, 0..9 and % and :
        */
        case 'a': case 'b': case 'c': case 'd': case 'e': case 'f': case 'g':
        case 'h': case 'i': case 'j': case 'k': case 'l': case 'm': case 'n':
        case 'o': case 'p': case 'q': case 'r': case 's': case 't': case 'u':
        case 'v': case 'w': case 'x': case 'y': case 'z': case 'A': case 'B':
        case 'C': case 'D': case 'E': case 'F': case 'G': case 'H': case 'I':
        case 'J': case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
        case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V': case 'W':
        case 'X': case 'Y': case 'Z': case '0': case '1': case '2': case '3':
        case '4': case '5': case '6': case '7': case '8': case '9': case '%':
        case ':':
          pos += 1;
          step2: while (true) {
            switch (s.charAt(pos)) {
              case '\0':
                break step2;
              /*
              Range a..Z, 0..9 and % and '.'
              */
              case 'a': case 'b': case 'c': case 'd': case 'e': case 'f':
              case 'g': case 'h': case 'i': case 'j': case 'k': case 'l':
              case 'm': case 'n': case 'o': case 'p': case 'q': case 'r':
              case 's': case 't': case 'u': case 'v': case 'w': case 'x':
              case 'y': case 'z': case 'A': case 'B': case 'C': case 'D':
              case 'E': case 'F': case 'G': case 'H': case 'I': case 'J':
              case 'K': case 'L': case 'M': case 'N': case 'O': case 'P':
              case 'Q': case 'R': case 'S': case 'T': case 'U': case 'V':
              case 'W': case 'X': case 'Y': case 'Z': case '0': case '1':
              case '2': case '3': case '4': case '5': case '6': case '7':
              case '8': case '9': case '%': case '.':
                pos += 1;
                continue;
            }
            break;
          }
          break;

        default:
          pos += 1;
      }

      String tmp = s.substring(start, pos);
      ret.add(tmp);
    }
  }
}
