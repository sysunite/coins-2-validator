package com.sysunite.coinsweb.report;

import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class NullTrueFalseMethod implements TemplateMethodModelEx {

  private static final Logger log = LoggerFactory.getLogger(NullTrueFalseMethod.class);

  public TemplateModel exec(List args) throws TemplateModelException {

    if (args.size() != 4) {
      throw new TemplateModelException("Wrong arguments for nullTrueFalseMethod method");
    }

    String nullValue = ((SimpleScalar)args.get(1)).getAsString();
    String trueValue = ((SimpleScalar)args.get(2)).getAsString();
    String falseValue = ((SimpleScalar)args.get(3)).getAsString();

    if(args.get(0) == null) {
      return new SimpleScalar(nullValue);
    }

    if (args.get(0) instanceof TemplateBooleanModel) {
      boolean value = ((TemplateBooleanModel) args.get(0)).getAsBoolean();

      if(value) {
        return new SimpleScalar(trueValue);
      } else {
        return new SimpleScalar(falseValue);
      }
    }

    return new SimpleScalar(nullValue);
  }
}