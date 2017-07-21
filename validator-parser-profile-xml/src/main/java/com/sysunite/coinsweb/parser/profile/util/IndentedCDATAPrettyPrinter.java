package com.sysunite.coinsweb.parser.profile.util;

import com.fasterxml.jackson.dataformat.xml.util.DefaultXmlPrettyPrinter;
import org.codehaus.stax2.XMLStreamWriter2;

import javax.xml.stream.XMLStreamException;
import java.util.Collections;

/**
 * @author bastbijl, Sysunite 2017
 */
public class IndentedCDATAPrettyPrinter extends DefaultXmlPrettyPrinter {

  public IndentedCDATAPrettyPrinter() {}

  protected IndentedCDATAPrettyPrinter(IndentedCDATAPrettyPrinter base) {
    _arrayIndenter = base._arrayIndenter;
    _objectIndenter = base._objectIndenter;
    _spacesInObjectEntries = base._spacesInObjectEntries;
    _nesting = base._nesting;
  }

  @Override
  public DefaultXmlPrettyPrinter createInstance() {
    return new IndentedCDATAPrettyPrinter(this);
  }

  @Override
  public void writeLeafElement(XMLStreamWriter2 sw, String nsURI, String localName, String text, boolean isCData) throws XMLStreamException {
    if(!this._objectIndenter.isInline()) {
      this._objectIndenter.writeIndentation(sw, this._nesting);
    }

    sw.writeStartElement(nsURI, localName);
    if(isCData) {
      this._objectIndenter.writeIndentation(sw, this._nesting+1);

      String body = indentText(text.trim(), this._nesting+2);
      String padding = String.join("", Collections.nCopies(this._nesting+1, INDENT));
      sw.writeCData(System.lineSeparator() + body + System.lineSeparator() + padding);

      this._objectIndenter.writeIndentation(sw, this._nesting);
    } else {
      sw.writeCharacters(text);
    }

    sw.writeEndElement();
    this._justHadStartElement = false;
  }

  public char[] prepend(char[] fragment, char[] with) {
    char[] result = new char[with.length + fragment.length];
    System.arraycopy(with, 0, result, 0, with.length);
    System.arraycopy(fragment, 0, result, with.length, fragment.length);
    return result;
  }

  public static String INDENT = "  ";
  public static String indentText(String body, int level) {
    String result = "";
    String indent = String.join("", Collections.nCopies(level, INDENT));
    String[] lines = body.split(System.lineSeparator());
    int cutoff = -1;
    boolean skipping = true;
    for(String line : lines) {
      if(skipping) {
        if(line.trim().isEmpty()) {
          continue;
        } else {
          skipping = false;
        }
      }
      int count = line.indexOf(line.trim());
      if(cutoff == -1) {
        cutoff = count;
      }
      count = Math.min(count, cutoff);
      result += indent + line.substring(count) + System.lineSeparator();
    }
    return result.substring(0, result.length()-1);
  }
}
