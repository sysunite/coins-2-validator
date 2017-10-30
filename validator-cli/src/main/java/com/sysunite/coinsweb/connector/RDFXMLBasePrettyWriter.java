package com.sysunite.coinsweb.connector;

import org.eclipse.rdf4j.common.xml.XMLUtil;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.XMLWriterSettings;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;

import java.io.*;
import java.util.Map;
import java.util.Stack;

/**
 * @author bastbijl, Sysunite 2017
 */
public class RDFXMLBasePrettyWriter extends RDFXMLWriter implements Closeable, Flushable {

	/*-----------*
	 * Variables *
	 *-----------*/
  private String base;

	/*
	 * We implement striped syntax by using two stacks, one for predicates and one for subjects/objects.
	 */

  /**
   * Stack for remembering the nodes (subjects/objects) of statements at each level.
   */
  private final Stack<RDFXMLBasePrettyWriter.Node> nodeStack = new Stack<>();

  /**
   * Stack for remembering the predicate of statements at each level.
   */
  private final Stack<IRI> predicateStack = new Stack<IRI>();

  /*--------------*
   * Constructors *
   *--------------*/

  /**
   * Creates a new RDFXMLPrintWriter that will write to the supplied OutputStream.
   *
   * @param out
   *        The OutputStream to write the RDF/XML document to.
   */
  public RDFXMLBasePrettyWriter(OutputStream out) {
    super(out);
  }

  /**
   * Creates a new RDFXMLPrintWriter that will write to the supplied Writer.
   *
   * @param out
   *        The Writer to write the RDF/XML document to.
   */
  public RDFXMLBasePrettyWriter(Writer out) {
    super(out);
  }

	/*---------*
	 * Methods *
	 *---------*/

	public void setBase(String base) {
	  this.base = base;
  }
  private String applyBase(String uri) {
	  if(base != null && uri.toString().startsWith(base)) {
      return uri.toString().substring(base.length());
    }
    return uri;
  }

  @Override
  protected void writeHeader()
  throws IOException
  {
    // This export format needs the RDF Schema namespace to be defined:
    setNamespace(RDFS.PREFIX, RDFS.NAMESPACE);

    try {
      // This export format needs the RDF namespace to be defined, add a
      // prefix for it if there isn't one yet.
      setNamespace(RDF.PREFIX, RDF.NAMESPACE);

      if (getWriterConfig().get(XMLWriterSettings.INCLUDE_XML_PI)) {
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
      }

      if (getWriterConfig().get(XMLWriterSettings.INCLUDE_ROOT_RDF_TAG)) {
        writeStartOfStartTag(RDF.NAMESPACE, "RDF");

        if (defaultNamespace != null) {
          writeNewLine();
          writeIndents(1);
          writer.write("xmlns=\"");
          writer.write(XMLUtil.escapeDoubleQuotedAttValue(defaultNamespace));
          writer.write("\"");
        }

        for (Map.Entry<String, String> entry : namespaceTable.entrySet()) {
          String name = entry.getKey();
          String prefix = entry.getValue();

          writeNewLine();
          writeIndents(1);
          writer.write("xmlns:");
          writer.write(prefix);
          writer.write("=\"");
          writer.write(XMLUtil.escapeDoubleQuotedAttValue(name));
          writer.write("\"");
        }

        if (base != null) {
          writeNewLine();
          writeIndent();
          writer.write("xml:base");
          writer.write("=\"");
          writer.write(base);
          writer.write("\"");
        }

        writeEndOfStartTag();
      }

      writeNewLine();
    }
    finally {
      headerWritten = true;
    }
  }

  @Override
  protected void writeIndent()
  throws IOException
  {
    writer.write("  ");
  }

  @Override
  public void flush()
  throws IOException
  {
    if (writingStarted) {
      if (!headerWritten) {
        writeHeader();
      }

      try {
        flushPendingStatements();
      }
      catch (RDFHandlerException e) {
        if (e.getCause() != null && e.getCause() instanceof IOException) {
          throw (IOException)e.getCause();
        }
        else {
          throw new IOException(e);
        }
      }

      writer.flush();
    }
  }

  @Override
  public void close()
  throws IOException
  {
    try {
      if (writingStarted) {
        endRDF();
      }
    }
    catch (RDFHandlerException e) {
      if (e.getCause() != null && e.getCause() instanceof IOException) {
        throw (IOException)e.getCause();
      }
      else {
        throw new IOException(e);
      }
    }
    finally {
      nodeStack.clear();
      predicateStack.clear();
      writer.close();
    }
  }

  @Override
  public void endRDF()
  throws RDFHandlerException
  {
    if (!writingStarted) {
      throw new RDFHandlerException("Document writing has not yet started");
    }

    try {
      if (!headerWritten) {
        writeHeader();
      }

      flushPendingStatements();

      if (getWriterConfig().get(XMLWriterSettings.INCLUDE_ROOT_RDF_TAG)) {
        writeEndTag(RDF.NAMESPACE, "RDF");
      }

      writer.flush();
    }
    catch (IOException e) {
      throw new RDFHandlerException(e);
    }
    finally {
      writingStarted = false;
      headerWritten = false;
    }
  }

  @Override
  protected void flushPendingStatements()
  throws IOException, RDFHandlerException
  {
    if (!nodeStack.isEmpty()) {
      popStacks(null);
    }
  }

  /**
   * Write out the stacks until we find subject. If subject == null, write out the entire stack
   *
   * @param newSubject
   */
  private void popStacks(Resource newSubject)
  throws IOException, RDFHandlerException
  {
    // Write start tags for the part of the stacks that are not yet
    // written
    for (int i = 0; i < nodeStack.size() - 1; i++) {
      RDFXMLBasePrettyWriter.Node node = nodeStack.get(i);

      if (!node.isWritten()) {
        if (i > 0) {
          writeIndents(i * 2 - 1);

          IRI predicate = predicateStack.get(i - 1);

          writeStartTag(predicate.getNamespace(), predicate.getLocalName());
          writeNewLine();
        }

        writeIndents(i * 2);
        writeNodeStartTag(node);
        node.setIsWritten(true);
      }
    }

    // Write tags for the top subject
    RDFXMLBasePrettyWriter.Node topNode = nodeStack.pop();

    if (predicateStack.isEmpty()) {
      // write out an empty subject
      writeIndents(nodeStack.size() * 2);
      writeNodeEmptyTag(topNode);
      writeNewLine();
    }
    else {
      IRI topPredicate = predicateStack.pop();

      if (!topNode.hasType()) {
        // we can use an abbreviated predicate
        writeIndents(nodeStack.size() * 2 - 1);
        writeAbbreviatedPredicate(topPredicate, topNode.getValue());
      }
      else {
        // we cannot use an abbreviated predicate because the type needs to
        // written out as well

        writeIndents(nodeStack.size() * 2 - 1);
        writeStartTag(topPredicate.getNamespace(), topPredicate.getLocalName());
        writeNewLine();

        // write out an empty subject
        writeIndents(nodeStack.size() * 2);
        writeNodeEmptyTag(topNode);
        writeNewLine();

        writeIndents(nodeStack.size() * 2 - 1);
        writeEndTag(topPredicate.getNamespace(), topPredicate.getLocalName());
        writeNewLine();
      }
    }

    // Write out the end tags until we find the subject
    while (!nodeStack.isEmpty()) {
      RDFXMLBasePrettyWriter.Node nextElement = nodeStack.peek();

      if (nextElement.getValue().equals(newSubject)) {
        break;
      }
      else {
        nodeStack.pop();

        // We have already written out the subject/object,
        // but we still need to close the tag
        writeIndents(predicateStack.size() + nodeStack.size());

        writeNodeEndTag(nextElement);

        if (predicateStack.size() > 0) {
          IRI nextPredicate = predicateStack.pop();

          writeIndents(predicateStack.size() + nodeStack.size());

          writeEndTag(nextPredicate.getNamespace(), nextPredicate.getLocalName());

          writeNewLine();
        }
      }
    }
  }

  @Override
  public void handleStatement(Statement st)
  throws RDFHandlerException
  {
    if (!writingStarted) {
      throw new RDFHandlerException("Document writing has not yet been started");
    }

    Resource subj = st.getSubject();
    IRI pred = st.getPredicate();
    Value obj = st.getObject();

    try {
      if (!headerWritten) {
        writeHeader();
      }

      if (!nodeStack.isEmpty() && !subj.equals(nodeStack.peek().getValue())) {
        // Different subject than we had before, empty the stack
        // until we find it
        popStacks(subj);
      }

      // Stack is either empty or contains the same subject at top

      if (nodeStack.isEmpty()) {
        // Push subject
        nodeStack.push(new RDFXMLBasePrettyWriter.Node(subj));
      }

      // Stack now contains at least one element
      RDFXMLBasePrettyWriter.Node topSubject = nodeStack.peek();

      // Check if current statement is a type statement and use a typed node
      // element is possible
      // FIXME: verify that an XML namespace-qualified name can be created
      // for the type URI
      if (pred.equals(RDF.TYPE) && obj instanceof IRI && !topSubject.hasType()
      && !topSubject.isWritten())
      {
        // Use typed node element
        topSubject.setType((IRI)obj);
      }
      else {
        if (!nodeStack.isEmpty() && pred.equals(nodeStack.peek().nextLi())) {
          pred = RDF.LI;
          nodeStack.peek().incrementNextLi();
        }

        // Push predicate and object
        predicateStack.push(pred);
        nodeStack.push(new RDFXMLBasePrettyWriter.Node(obj));
      }
    }
    catch (IOException e) {
      throw new RDFHandlerException(e);
    }
  }

  /**
   * Write out the opening tag of the subject or object of a statement up to (but not including) the end of
   * the tag. Used both in writeStartSubject and writeEmptySubject.
   */
  private void writeNodeStartOfStartTag(RDFXMLBasePrettyWriter.Node node)
  throws IOException, RDFHandlerException
  {
    Value value = node.getValue();

    if (node.hasType()) {
      // We can use abbreviated syntax
      writeStartOfStartTag(node.getType().getNamespace(), node.getType().getLocalName());
    }
    else {
      // We cannot use abbreviated syntax
      writeStartOfStartTag(RDF.NAMESPACE, "Description");
    }

    if (value instanceof IRI) {
      IRI uri = (IRI)value;
      writeAttribute(RDF.NAMESPACE, "about", applyBase(uri.toString()));
    }
    else {
      BNode bNode = (BNode)value;
      writeAttribute(RDF.NAMESPACE, "nodeID", getValidNodeId(bNode));
    }
  }

  /**
   * Write out the opening tag of the subject or object of a statement.
   */
  private void writeNodeStartTag(RDFXMLBasePrettyWriter.Node node)
  throws IOException, RDFHandlerException
  {
    writeNodeStartOfStartTag(node);
    writeEndOfStartTag();
    writeNewLine();
  }

  /**
   * Write out the closing tag for the subject or object of a statement.
   */
  private void writeNodeEndTag(RDFXMLBasePrettyWriter.Node node)
  throws IOException
  {

    if (node.getType() != null) {
      writeEndTag(node.getType().getNamespace(), node.getType().getLocalName());
    }
    else {
      writeEndTag(RDF.NAMESPACE, "Description");
    }
    writeNewLine();
  }

  /**
   * Write out an empty tag for the subject or object of a statement.
   */
  private void writeNodeEmptyTag(RDFXMLBasePrettyWriter.Node node)
  throws IOException, RDFHandlerException
  {

    writeNodeStartOfStartTag(node);
    writeEndOfEmptyTag();
  }

  /**
   * Write out an empty property element.
   */
  private void writeAbbreviatedPredicate(IRI pred, Value obj)
  throws IOException, RDFHandlerException
  {
    writeStartOfStartTag(pred.getNamespace(), pred.getLocalName());

    if (obj instanceof Resource) {
      Resource objRes = (Resource)obj;

      if (objRes instanceof IRI) {
        IRI uri = (IRI)objRes;
        writeAttribute(RDF.NAMESPACE, "resource", applyBase(uri.toString()));
      }
      else {
        BNode bNode = (BNode)objRes;
        writeAttribute(RDF.NAMESPACE, "nodeID", getValidNodeId(bNode));
      }

      writeEndOfEmptyTag();
    }
    else if (obj instanceof Literal) {
      Literal objLit = (Literal)obj;
      // datatype attribute
      IRI datatype = objLit.getDatatype();
      // Check if datatype is rdf:XMLLiteral
      boolean isXmlLiteral = datatype.equals(RDF.XMLLITERAL);

      // language attribute
      if (Literals.isLanguageLiteral(objLit)) {
        writeAttribute("xml:lang", objLit.getLanguage().get());
      }
      else {
        if (isXmlLiteral) {
          writeAttribute(RDF.NAMESPACE, "parseType", "Literal");
        }
        else {
          writeAttribute(RDF.NAMESPACE, "datatype", applyBase(datatype.toString()));
        }
      }

      writeEndOfStartTag();

      // label
      if (isXmlLiteral) {
        // Write XML literal as plain XML
        writer.write(objLit.getLabel());
      }
      else {
        writeCharacterData(objLit.getLabel());
      }

      writeEndTag(pred.getNamespace(), pred.getLocalName());
    }

    writeNewLine();
  }

  protected void writeStartTag(String namespace, String localName)
  throws IOException
  {
    writeStartOfStartTag(namespace, localName);
    writeEndOfStartTag();
  }

  /**
   * Writes <tt>n</tt> indents.
   */
  protected void writeIndents(int n)
  throws IOException
  {
    n++;
    for (int i = 0; i < n; i++) {
      writeIndent();
    }
  }

/*------------------*
 * Inner class Node *
 *------------------*/

  private static class Node {

    private int nextLiIndex = 1;

    private Resource nextLi;

    private Value value;

    // type == null means that we use <rdf:Description>
    private IRI type = null;

    private boolean isWritten = false;

    /**
     * Creates a new Node for the supplied Value.
     */
    public Node(Value value) {
      this.value = value;
    }

    Resource nextLi() {
      if (nextLi == null) {
        nextLi = SimpleValueFactory.getInstance().createIRI(RDF.NAMESPACE + "_" + nextLiIndex);
      }

      return nextLi;
    }

    public void incrementNextLi() {
      nextLiIndex++;
      nextLi = null;
    }

    public Value getValue() {
      return value;
    }

    public void setType(IRI type) {
      this.type = type;
    }

    public IRI getType() {
      return type;
    }

    public boolean hasType() {
      return type != null;
    }

    public void setIsWritten(boolean isWritten) {
      this.isWritten = isWritten;
    }

    public boolean isWritten() {
      return isWritten;
    }
  }
}
