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
package se.poppanator.lime.xml;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class with static methods for buildning {@link Node} objects
 */
public class Builder
{
  /**
   * Creates a "query" node. This will automatically add the attribute
   * distict=1.
   * @param nodes
   * @return
   */
  public static Node query(ArrayList<Node> nodes)
  {
    HashMap<String, String> attr = new HashMap<String, String>();
    attr.put("distinct", "1");
    return Builder.query(nodes, attr);
  }

  /**
   * Creates a query node with attributes
   *
   * @param nodes
   * @param attr
   * @return
   */
  public static Node query(ArrayList<Node> nodes, HashMap<String,String> attr)
  {
    return new Node("query", attr, nodes);
  }

  /**
   * Creates "tables > table" node
   * @param name
   * @return
   */
  public static Node table(final String name)
  {
    return new Node("tables", new ArrayList<Node>() {{
                                add(new Node("table", name));
                              }});
  }

  /**
   * Creates a "field" node
   * @param name
   * @return
   */
  public static Node field(String name)
  {
    return new Node("field", name);
  }

  /**
   * Creates a field node. Note that the "key" field in the HashMap must be
   * set and is the name of the field.
   * @param attr
   * @return
   */
  public static Node field(HashMap<String,String> attr)
  {
    String name = null;
    if (attr.containsKey("field")) {
      name = attr.get("field");
      attr.remove("field");
    }

    return new Node("field", attr, name);
  }

  /**
   * Creates a "fields" node with `fields`.
   * @param fields
   * @return
   */
  public static Node fields(String[] fields)
  {
    ArrayList<Node> n = new ArrayList<Node>();

    for (String f : fields) {
      n.add(Builder.field(f));
    }

    return new Node("fields", n);
  }

  /**
   * Creates a "fields" node with `fields`. The `fields` ArrayList can contain
   * {@link HashMap}s as per {@link field(HashMap<String,String>)},
   * {@link Node} objects or Strings.
   * @param fields
   * @return
   */
  public static Node fields(ArrayList<Object> fields)
  {
    ArrayList<Node> n = new ArrayList<Node>();

    for (Object o : fields) {
      if (o instanceof HashMap)
        n.add(Builder.field((HashMap<String,String>) o));
      else if (o instanceof Node)
        n.add((Node) o);
      else
        n.add(Builder.field((String) o));
    }

    return new Node("fields", n);
  }

  /**
   * Creates an "exp" field with no value.
   * @param type
   * @return
   */
  public static Node exp(String type)
  {
    HashMap<String,String> a = new HashMap<String,String>();
    a.put("type", type);
    return new Node("exp", a);
  }

  /**
   * Creates an "exp field".
   * @param type
   * @param value
   * @return
   */
  public static Node exp(String type, String value)
  {
    HashMap<String,String> a = new HashMap<String,String>();
    a.put("type", type);
    return new Node("exp", a, value);
  }

  /**
   * Creates a "conditions" node with condion nodes of `conds`.
   * @param conds
   * @return
   */
  public static Node conditions(ArrayList<Node> conds)
  {
    return new Node("conditions", conds);
  }

  /**
   * Creates a "conditions" node with attributes `attr` and
   * condion nodes of `conds`.
   * @param conds
   * @return
   */
  public static Node condition(HashMap<String,String> attr,
                               ArrayList<Node> value)
  {
    return new Node("condition", attr, value);
  }
}
