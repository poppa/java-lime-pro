/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.poppanator.lime.test;

import java.util.logging.Level;
import java.util.logging.Logger;
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
      "       idsostype, `select`, soscategory, soscategory.sosbusinessarea,\n" +
      "       webcompany, webperson, web, department, name\n" +
      "FROM   sostype\n" +
      "WHERE  active = 1 AND\n" +
      "       descriptive = 'TVK - Kundservice' AND\n" +
      "       soscategory.sosbusinessarea = '2701':numeric OR\n" +
      "       (web = 1 AND (\n" +
      "        webperson != 0 OR webcompany != 0))\n" +
      "ORDER BY department, name DESC " +
      "LIMIT  10, 10";

    Parser parser = new Parser();
    try {
      Node query = parser.parse(s);
      System.out.println("Node: " + query);
    }
    catch (Exception ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
