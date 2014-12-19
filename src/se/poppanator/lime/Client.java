/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package se.poppanator.lime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import se.poppanator.lime.sql.Parser;
import se.poppanator.lime.ws.DataService;
import se.poppanator.lime.ws.IDataService;
import se.poppanator.lime.xml.Node;

/**
 *
 * @author ponost
 */
public class Client
{
  protected static boolean DEBUG = false;
  protected static URL WSDL_URL = null;

  public static void setDebug(boolean doDebug)
  {
    DEBUG = doDebug;
  }

  public static void setWsdlUrl(String url) throws MalformedURLException
  {
    setWsdlUrl(new URL(url));
  }

  public static void setWsdlUrl(URL url)
  {
    WSDL_URL = url;
  }

  public static URL getWsdlUrl()
  {
    return WSDL_URL;
  }

  public static boolean getDebug()
  {
    return DEBUG;
  }

  void Client() {}

  protected DataService getDataService()
  {
    return new DataService(WSDL_URL);
  }

  public IDataService getSoapClient()
  {
    return getDataService().getBasicHttpBindingIDataService();
  }

  public ArrayList<HashMap<String,String>> sqlQuery(String query)
  throws Exception
  {
    Node n = Parser.sql(query);
    if (DEBUG)
      System.out.println("> sql2xml: " + n.toHumanReadbleString());

    n = query(n);

    if (DEBUG)
      System.out.println("< result: " + n.toHumanReadbleString());

    ArrayList<HashMap<String,String>> ret;
    ret = new ArrayList<HashMap<String, String>>();

    for (Object nn : n) {
      ret.add(((Node)nn).getAttributes());
    }

    return ret;
  }

  public Node query(Node limeQuery) throws Exception
  {
    return Node.parse(query(limeQuery.toString()));
  }

  public String query(String xml) throws Exception
  {
    return getSoapClient().getXmlQueryData(xml);
  }

}
