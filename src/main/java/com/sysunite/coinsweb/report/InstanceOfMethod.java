package com.sysunite.coinsweb.report;

import freemarker.ext.beans.StringModel;
import freemarker.template.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author bastbijl, Sysunite 2017
 */
public class InstanceOfMethod implements TemplateMethodModelEx {
  private static final Logger log = LoggerFactory.getLogger(InstanceOfMethod.class);

  public TemplateModel exec(List args) throws TemplateModelException {
    if (args.size() != 2) {
      throw new TemplateModelException("Wrong arguments for instanceOf method");
    }

    String classNameArg = ((SimpleScalar)args.get(1)).getAsString();

    if (args.get(0) instanceof StringModel) {
      Object object = ((StringModel) args.get(0)).getWrappedObject();

      if(object.getClass().getSimpleName().equals(classNameArg)) {
        return TemplateBooleanModel.TRUE;
      }
    }

    return TemplateBooleanModel.FALSE;
  }
}