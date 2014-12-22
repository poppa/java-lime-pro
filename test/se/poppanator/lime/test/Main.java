/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.poppanator.lime.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.poppanator.lime.SampleClient;
import se.poppanator.lime.sql.Parser;
import se.poppanator.lime.xml.Builder;
import se.poppanator.lime.xml.Node;

/**
 *
 * @author ponost
 */
public class Main
{
  String sql =
  "SELECT DISTINCT\n" +
  "       idsostype, descriptive, soscategory, soscategory.sosbusinessarea,\n" +
  "       webcompany, webperson, web, department, name\n" +
  "FROM   sostype\n" +
  "WHERE  active='1':numeric AND\n" +
  "       soscategory.sosbusinessarea != 2701 AND\n" +
  "       web=1 AND (webperson=1 OR webcompany=1)\n" +
  "ORDER BY descriptive, soscategory ASC\n" +
  "LIMIT  100";

  public static void main(String[] argv)
  {
    Main main = new Main();
    //main.runXmlBuilder();
    main.runParser();
  }

  public void runParser()
  {
    Parser parser = new Parser();

    try {
      Node query = parser.parse(sql);
      System.out.println("Node: " + query.toHumanReadbleString(2));
    }
    catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  public void runXmlBuilder()
  {
    Node query;

    ArrayList<Node> data = new ArrayList<>();
    ArrayList<Node> conds = new ArrayList<>();

    data.add(Builder.table("sostype"));

    HashMap<String,String> opeq = new HashMap<>();
    opeq.put("operator", "=");

    conds.add(Builder.condition(opeq, new ArrayList<Node>() {{
      add(Builder.exp("field", "active"));
      add(Builder.exp("numeric", "1"));
    }}));

    conds.add(Builder.condition(opeq, new ArrayList<Node>() {{
      add(Builder.exp("field", "web"));
      add(Builder.exp("numeric", "1"));
    }}));

    data.add(Builder.conditions(conds));
    data.add(Builder.fields(new ArrayList<Object>() {{
      add("idsostype");
      add("descriptive");
      add("soscategory");
      add("soscategory.sosbusinessarea");
    }}));

    query = Builder.query(data);

    System.out.println("Node: " + query.toHumanReadbleString());
  }

  public void runXmlTest()
  {
    Node n = new Node("test");
    n.add(new Node("child", "Sweet child in time").add(new Node("child-child")));
    n.setAttribute("is-test", "yes");
    System.out.println("Node: " + n);
  }

  public void runClient()
  {
    try {
      SampleClient.setWsdlUrl("http://dad.tvdomain.local:8081/DataService/?wsdl");
      SampleClient.setDebug(false);

      SampleClient c = new SampleClient();

      ArrayList<HashMap<String,String>> res = c.sqlQuery(sql);

      if (res != null) {
        for (HashMap<String,String> row : res) {
          System.out.println("* " + row.get("name"));
        }
      }
    }
    catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
