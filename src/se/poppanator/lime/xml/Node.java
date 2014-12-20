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
 * @package   se.poppanator.lime.xml
 * @version   0.1
 */
package se.poppanator.lime.xml;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import static org.w3c.dom.Node.ELEMENT_NODE;
import static org.w3c.dom.Node.TEXT_NODE;
import org.w3c.dom.NodeList;

/**
 * Class for creating Lime XML queries and deserializing Lime XML
 * responses.
 *
 * @author ponost
 */
public class Node implements Iterable
{
  /**
   * Node attributes
   */
  private HashMap<String,String> attributes;
  /**
   * Node value
   */
  private Object value = null;
  /**
   * Node name
   */
  private String name;

  /**
   * Parse `xml` into a {@link Node} object
   *
   * @throws Exception
   * @param xml
   * @return
   */
  public static Node parse(String xml) throws Exception
  {
    return new Node().parseXML(xml);
  }

  /**
   * Creates an empty object
   */
  public Node() {}

  /**
   * Creates a node with name `name`.
   * @param name
   */
  public Node(String name)
  {
    this();
    this.name = name;
  }

  /**
   * Creates a node with attributes
   *
   * @param name
   * @param attr
   */
  public Node(String name, HashMap<String,String> attr)
  {
    this(name);
    this.attributes = attr;
  }

  /**
   * Creates a node with a string value
   *
   * @param name
   * @param value
   */
  public Node(String name, String value)
  {
    this(name);
    this.value = value;
  }

  /**
   * Creates a node with children
   * @param name
   * @param children
   */
  public Node(String name, ArrayList<Node> children)
  {
    this(name);
    this.value = children;
  }

  /**
   * Creates a node with attributes and children
   * @param name
   * @param attr
   * @param children
   */
  public Node(String name, HashMap<String,String> attr,
              ArrayList<Node> children)
  {
    this(name, attr);
    this.value = children;
  }

  /**
   * Creates a node with attributes and string value
   * @param name
   * @param attr
   * @param value
   */
  public Node(String name, HashMap<String,String> attr, String value)
  {
    this(name, attr);
    this.value = value;
  }
  
  /**
   * Setter for name of the node.
   * @param name 
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Getter for the Node name
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Setter for the node attributes.
   * @param attributes 
   */
  public void setAttributes(HashMap<String,String> attributes)
  {
    this.attributes = attributes;
  }
  
  /**
   * Getter for the Node attributes
   * @return
   */
  public HashMap<String,String> getAttributes()
  {
    return attributes;
  }

  /**
   * Set a specific attribute
   * @param name
   * @param value 
   */
  public void setAttribute(String name, String value)
  {
    if (attributes == null) attributes = new HashMap<>();
    attributes.put(name, value);
  }
  
  /**
   * Get a specific attribute
   *
   * @param name
   * @return
   */
  public String getAttribute(String name)
  {
    if (attributes != null)
      return attributes.get(name);

    return null;
  }

  /**
   * Does the Node have any attributes?
   * @return
   */
  public boolean hasAttributes()
  {
    if (attributes != null)
      return attributes.size() > 0;

    return false;
  }
  
  /**
   * Setter for the node value.
   * @param value 
   */
  public void setValue(Object value)
  {
    this.value = value;
  }

  /**
   * Getter for the Node value
   * @return
   *  This can be either an {@link ArrayList} of (most likely) {@link Node}
   *  objects, or it can be a {@link String}
   */
  public Object getValue()
  {
    return value;
  }
  
  /**
   * Adds something to the value.
   * If {@link Node#value} is a String and <code>o</code> is a string 
   * <code>o</code> will be appended to {@link Node#value}. 
   * 
   * If <code>Node#value</code> is an {@link ArrayList} whatever <code>o</code> is
   * it will be added to the list.
   * 
   * If <code>Node#value</code> is a <code>String</code> and <code>o</code>
   * is not a <code>String</code> the <code>Node#value</code> will be converted
   * to an <code>ArrayList</code> and <code>o</code> will be appended to the 
   * list.
   * @param o
   * @return 
   */
  public Node add(Object o)
  {
    if (value == null) 
      value = "";

    if (o instanceof String) {
      if (value instanceof String)
        value += (String) o;
      else if (value instanceof ArrayList)
        ((ArrayList) value).add(o);
    }
    else {
      if (value instanceof String) {
        ArrayList<Object> tmp = new ArrayList<>();
        
        if (!((String)value).isEmpty())
          tmp.add(value);
        
        value = tmp;
      }
      
      ((ArrayList<Object>) value).add(o);
    }

    return this;
  }

  /**
   * Check if this node has any children
   * @return
   */
  public boolean hasChildren()
  {
    if (value instanceof ArrayList)
      return ((ArrayList<Object>) value).size() > 0;

    return false;
  }

  /**
   * Turns `xml` into a Node object
   * @throws Exception
   * @param xml
   * @return
   */
  public Node parseXML(String xml) throws Exception
  {
    if (xml == null) return null;

    try {
      if (xml.contains("<?xml")) {
        xml = xml.replaceFirst("<\\?xml.*?\\?>", "");
      }

      DocumentBuilder db;
      db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      Document dom;
      dom = db.parse(new ByteArrayInputStream(xml.getBytes()));
      parseNode(dom.getFirstChild());
    }
    catch (Exception e) {
      throw e;
    }

    return this;
  }

  /**
   * Parses a {@link org.w3c.dom.Node} object.
   * Consider internal
   *
   * @param o
   * @return
   */
  public Node parseNode(org.w3c.dom.Node o)
  {
    name = o.getNodeName();
    NamedNodeMap attr = o.getAttributes();

    if (attr.getLength() > 0) {
      domattrtoattr(attr);
    }

    NodeList ch = o.getChildNodes();

    if (ch.getLength() > 0) {
      for (int i = 0; i < ch.getLength(); i++) {
        org.w3c.dom.Node item = ch.item(i);
        
        if (item.getNodeType() == ELEMENT_NODE) {
          if (value == null) 
            value = new ArrayList<>();
          
          ((ArrayList<Object>) value).add(new Node().parseNode(item));
        }
        else if (item.getNodeType() == TEXT_NODE) {
          String v = item.getNodeValue();

          if (v != null && v.trim().length() > 0) {
            if (value == null) 
              value = new ArrayList<>();

            ((ArrayList<Object>) value).add(v);
          }
        }
      }
    }
    else {
      String v = o.getNodeValue();

      if (v != null && !v.trim().isEmpty())
        value = v;
    }

    return this;
  }

  /**
   * Turns the Node into XML
   * @return
   */
  public String toXML()
  {
    String out;

    out = "<" + name + attrtostr();

    if (value == null) {
      out += " />";
    }
    else if (value instanceof ArrayList) {
      ArrayList<Object> cc = (ArrayList<Object>) value;

      if (cc.isEmpty()) {
        out += " />";
      }
      else {
        out += ">";

        for (Object n : cc) {
          out += String.valueOf(n);
        }

        out += "</" + name + ">";
      }
    }
    else {
      if (value instanceof String) {
        String vv = (String) value;
        if (vv.trim().isEmpty())
          out += " />";
        else
          out += ">" + lazyEscapeXml(vv) + "</" + name + ">";
      }
    }

    return out;
  }

  /**
   * Like {@link toString()} or {@link toXML()} except with formatting.
   * @return
   */
  public String toHumanReadbleString()
  {
    return toHumanReadbleString(2);
  }

  /**
   * Like {@link toString()} or {@link toXML()} except with formatting.
   * @param indentWidth
   *  Indent width
   * @return
   */
  public String toHumanReadbleString(int indentWidth)
  {
    try {

      Transformer tr = TransformerFactory.newInstance().newTransformer();
      tr.setOutputProperty(OutputKeys.INDENT, "yes");
      tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount",
                           Integer.toString(indentWidth));

      Source inp = new StreamSource(new StringReader(toXML()));
      StreamResult outp = new StreamResult(new StringWriter());

      tr.transform(inp, outp);

      return outp.getWriter().toString();
    }
    catch (Exception ex) {
      return toXML();
    }
  }

  /**
   * To String converter
   * @return
   */
  @Override
  public String toString()
  {
    return toXML();
  } 

  /**
   * Escapes text for XML (really lazily).
   * @param s
   * @return 
   */
  public String lazyEscapeXml(String s) {
      return s.replaceAll("&", "&amp;")
              .replaceAll(">", "&gt;")
              .replaceAll("<", "&lt;")
              .replaceAll("\"", "&quot;")
              .replaceAll("'", "&apos;");
  }

  /**
   * Attributes to string
   * @return
   */
  private String attrtostr()
  {
    String out = "";

    if (attributes != null && attributes.size() > 0) {
      for (String s : attributes.keySet()) {
        out += " " + s + "=\"" + lazyEscapeXml(attributes.get(s)) + "\"";
      }
    }

    return out;
  }

  /**
   * DOM attributes to HashMap
   * @param a
   */
  private void domattrtoattr(NamedNodeMap a)
  {
    if (a.getLength() > 0 && attributes == null)
      attributes = new HashMap<>();

    for (int i = 0; i < a.getLength(); i++) {
      org.w3c.dom.Node n = a.item(i);
      attributes.put(n.getNodeName(), n.getNodeValue());
    }
  }

  @Override
  public Iterator<Object> iterator() {
    Iterator<Object> it = new Iterator<Object>() {

      private int index = 0;

      @Override
      public boolean hasNext() {
        if (value == null || value instanceof String)
          return false;

        ArrayList<Object> v = (ArrayList<Object>) value;
        return index < v.size() && v.get(index) != null;
      }

      @Override
      public Object next() {
        ArrayList<Object> v = (ArrayList<Object>) value;
        return v.get(index++);
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };

    return it;
  }
}
