package com.sysunite.coinsweb.util;

import freemarker.core.TemplateElement;
import freemarker.ext.beans.StringModel;
import freemarker.template.Template;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Courtesy https://stackoverflow.com/questions/1919189/how-can-i-introspect-a-freemarker-template-to-find-out-what-variables-it-uses
 */
public class FreemarkerUtil {

  public static List<String> referenceSet(Template template) throws TemplateModelException {
    List<String> result = new ArrayList<>();
    TemplateElement rootTreeNode = template.getRootTreeNode();
    for (int i = 0; i < rootTreeNode.getChildCount(); i++) {
      TemplateModel templateModel = rootTreeNode.getChildNodes().get(i);
      if (!(templateModel instanceof StringModel)) {
        continue;
      }
      Object wrappedObject = ((StringModel) templateModel).getWrappedObject();
      if (!"DollarVariable".equals(wrappedObject.getClass().getSimpleName())) {
        continue;
      }

      try {
        Object expression = getInternalState(wrappedObject, "expression");
        switch (expression.getClass().getSimpleName()) {
          case "Identifier":
            result.add(getInternalState(expression, "name").toString());
            break;
          case "DefaultToExpression":
            result.add(getInternalState(expression, "lho").toString());
            break;
          case "BuiltinVariable":
            break;
          default:
            throw new IllegalStateException("Unable to introspect variable");
        }
      } catch (NoSuchFieldException | IllegalAccessException e) {
        throw new TemplateModelException("Unable to reflect template model");
      }
    }
    return result;
  }

  private static Object getInternalState(Object o, String fieldName) throws NoSuchFieldException, IllegalAccessException {
    Field field = o.getClass().getDeclaredField(fieldName);
    boolean wasAccessible = field.isAccessible();
    try {
      field.setAccessible(true);
      return field.get(o);
    } finally {
      field.setAccessible(wasAccessible);
    }
  }
}
