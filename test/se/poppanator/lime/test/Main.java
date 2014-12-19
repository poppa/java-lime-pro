/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.poppanator.lime.test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import se.poppanator.lime.Client;
import se.poppanator.lime.sql.Parser;
import se.poppanator.lime.xml.Node;

/**
 *
 * @author ponost
 */
public class Main
{
  public static void main(String[] argv)
  {
    String s =
      "SELECT DISTINCT\n" +
      "       idsostype, descriptive, soscategory, soscategory.sosbusinessarea,\n" +
      "       webcompany, webperson, web, department, name\n" +
      "FROM   sostype\n" +
      "WHERE  active='1':numeric AND\n" +
      "       soscategory.sosbusinessarea != 2701 AND\n" +
      "       web=1 AND (webperson=1 OR webcompany=1)\n" +
      "ORDER BY descriptive, soscategory ASC\n" +
      "LIMIT  100";

    try {
      Client.setWsdlUrl("http://dad.tvdomain.local:8081/DataService/?wsdl");
      Client.setDebug(false);

      Client c = new Client();

      ArrayList<HashMap<String,String>> res = c.sqlQuery(s);

      for (HashMap<String,String> row : res) {
        System.out.println("* " + row.get("name"));
      }
    }
    catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
/*
    Parser parser = new Parser();
    try {
      Node query = parser.parse(s);
      System.out.println("Node: " + query.toHumanReadbleString(4));
    }
    catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
*/
  }
}
