java-lime-pro
=============

A Java helper module for the web services of the CRM **Lime PRO** by
[Lundalogik](https://github.com/lundalogik).

There's a [PHP version](https://github.com/poppa/php-lime-pro) of this module
as well.

This is not a full Lime PRO client but rather a helper module for building the
XML queries to send to Lime as well as parsing the result. There is however
a [sample client](https://github.com/poppa/java-lime-pro/blob/master/src/se/poppanator/lime/SampleClient.java)
using [Metro](https://metro.java.net/).

## Buildning an XML query

The easiest way to build a Lime XML query is by using the SQL to XML class. It
takes an SQL query and converts it into a [Node](https://github.com/poppa/java-lime-pro/blob/master/src/se/poppanator/lime/xml/Node.java)
object.

```java
import se.poppanator.lime.sql.Parser;
import se.poppanator.lime.xml.Node;

// ...

String sql =
  "SELECT DISTINCT\n" +
  "       idsostype, descriptive, soscategory, soscategory.sosbusinessarea,\n" +
  "       webcompany, webperson, web, department, name\n" +
  "FROM   sostype\n" +
  "WHERE  active='1':numeric AND\n" +
  "       soscategory.sosbusinessarea != 2701 AND\n" +
  "       web=1 AND (webperson=1 OR webcompany=1)\n" +
  "ORDER BY descriptive, soscategory DESC\n" +
  "LIMIT  100";

Node limeQuery = Parser.sql(sql);

String result = myWSClient.query(limeQuery.toString());
```

The SQL query above will result in an XML document like this:

```xml
<query top="100" distinct="1">
  <tables>
    <table>sostype</table>
  </tables>
  <conditions>
    <condition operator="=">
      <exp type="field">active</exp>
      <exp type="numeric">1</exp>
    </condition>
    <condition operator="!=">
      <exp type="field">soscategory.sosbusinessarea</exp>
      <exp type="numeric">2701</exp>
    </condition>
    <condition operator="=">
      <exp type="field">web</exp>
      <exp type="numeric">1</exp>
    </condition>
    <condition>
      <exp type="("/>
    </condition>
    <condition operator="=">
      <exp type="field">webperson</exp>
      <exp type="numeric">1</exp>
    </condition>
    <condition or="1" operator="=">
      <exp type="field">webcompany</exp>
      <exp type="numeric">1</exp>
    </condition>
    <condition>
      <exp type=")"/>
    </condition>
  </conditions>
  <fields>
    <field>idsostype</field>
    <field sortindex="1" sortorder="ASC">descriptive</field>
    <field sortindex="2" sortorder="ASC">soscategory</field>
    <field>soscategory.sosbusinessarea</field>
    <field>webcompany</field>
    <field>webperson</field>
    <field>web</field>
    <field>department</field>
    <field>name</field>
  </fields>
</query>
```