package com.sysunite.coinsweb.parser.config.pojo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sysunite.coinsweb.graphset.GraphVar;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * @author bastbijl, Sysunite 2017
 */
@JsonSerialize(using = GraphVarSerializer.class)
@JsonDeserialize(using = GraphVarDeserializer.class)
public class GraphVarImpl implements GraphVar, CharSequence {
  private String graphVar;
  public GraphVarImpl(String graphVar) {
    this.graphVar = graphVar;
  }

  @Override
  public int length() {
    return graphVar.length();
  }

  @Override
  public char charAt(int index) {
    return graphVar.charAt(index);
  }

  @Override
  public CharSequence subSequence(int start, int end) {
    return graphVar.subSequence(start, end);
  }

  @Override
  public String toString() {
    return this.graphVar;
  }

  @Override
  public IntStream chars() {
    return graphVar.chars();
  }

  @Override
  public IntStream codePoints() {
    return graphVar.codePoints();
  }

  @Override
  public boolean equals(Object obj) {
    if(!(obj instanceof GraphVar)) {
      return false;
    }
    if(this.graphVar == null || obj == null) {
      return false;
    }
    return this.graphVar.equals((obj).toString());
  }
  @Override
  public int hashCode() {
    return this.graphVar.hashCode();
  }
}
class GraphVarSerializer extends JsonSerializer<GraphVar> {
  @Override
  public void serialize(GraphVar value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
    jgen.writeString(value.toString());
  }
}
class GraphVarDeserializer extends JsonDeserializer<GraphVar> {
  @Override
  public GraphVar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    return new GraphVarImpl(p.getText());
  }
}
