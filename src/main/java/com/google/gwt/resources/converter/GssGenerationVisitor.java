/*
 * Copyright 2013 Julien Dramaix.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */


package com.google.gwt.resources.converter;

import static java.lang.String.format;

import com.google.common.base.Strings;
import com.google.gwt.dev.util.TextOutput;
import com.google.gwt.resources.css.ast.Context;
import com.google.gwt.resources.css.ast.CssCompilerException;
import com.google.gwt.resources.css.ast.CssDef;
import com.google.gwt.resources.css.ast.CssEval;
import com.google.gwt.resources.css.ast.CssExternalSelectors;
import com.google.gwt.resources.css.ast.CssFontFace;
import com.google.gwt.resources.css.ast.CssIf;
import com.google.gwt.resources.css.ast.CssMediaRule;
import com.google.gwt.resources.css.ast.CssNoFlip;
import com.google.gwt.resources.css.ast.CssPageRule;
import com.google.gwt.resources.css.ast.CssProperty;
import com.google.gwt.resources.css.ast.CssProperty.DotPathValue;
import com.google.gwt.resources.css.ast.CssProperty.Value;
import com.google.gwt.resources.css.ast.CssRule;
import com.google.gwt.resources.css.ast.CssSelector;
import com.google.gwt.resources.css.ast.CssSprite;
import com.google.gwt.resources.css.ast.CssUnknownAtRule;
import com.google.gwt.resources.css.ast.CssUrl;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GssGenerationVisitor extends ExtendedCssVisitor {
  /* templates and tokens list */
  private static final String NO_FLIP = "/* @noflip */";
  private static final String GWT_SPRITE = "gwt-sprite: \"%s\"";
  private static final String OR = " || ";
  private static final String NOT = "!";
  private static final String IF = "@if (%s)";
  private static final String ELSE_IF = "@elseif (%s)";
  private static final String ELSE = "@else ";
  private static final String IS = "is(\"%s\", \"%s\")";
  private static final String EVAL = "eval('%s')";
  private static final String VALUE = "value('%s')";
  private static final String VALUE_WITH_SUFFIX = "value('%s', '%s')";
  private static final String URL = "resourceUrl(\"%s\")";
  private static final String DEF = "@def ";
  private static final String EXTERNAL = "@external";
  private static final String IMPORTANT = " !important";
  private static final Pattern UNESCAPE = Pattern.compile("\\\\");


  private final Map<String, String> defKeyMapping;

  private final TextOutput out;
  private boolean noFlip;
  private boolean newLine;
  private boolean needsOpenBrace;
  private boolean needsComma;
  private boolean inSprite;
  private boolean inUrl;

  public GssGenerationVisitor(TextOutput out, Map<String, String> defKeyMapping) {
    this.defKeyMapping = defKeyMapping;
    this.out = out;
    newLine = true;
  }

  public String getContent() {
    return out.toString();
  }

  @Override
  public void endVisit(CssFontFace x, Context ctx) {
    closeBrace();
  }


  @Override
  public void endVisit(CssMediaRule x, Context ctx) {
    out.indentOut();
    out.print("}");
    out.newlineOpt();
  }

  @Override
  public void endVisit(CssPageRule x, Context ctx) {
    out.indentOut();
    out.print("}");
    out.newlineOpt();
  }

  @Override
  public void endVisit(CssUnknownAtRule x, Context ctx) {
    out.print(x.getRule());
  }

  @Override
  public boolean visit(CssEval x, Context ctx) {
    printDef(x, EVAL, "eval");

    return false;
  }

  @Override
  public boolean visit(CssDef x, Context ctx) {
    printDef(x, null, "def");

    return false;
  }

  @Override
  public boolean visit(CssSprite x, Context ctx) {
    inSprite = true;
    return false;
  }

  @Override
  public void endVisit(CssSprite x, Context ctx) {
    needsComma = false;

    accept(x.getSelectors());
    openBrace();

    out.print(format(GWT_SPRITE, x.getResourceFunction().getPath()));
    semiColon();

    accept(x.getProperties());

    closeBrace();
    inSprite = false;
  }

  @Override
  public boolean visit(CssRule x, Context ctx) {
    if (newLine) {
      out.newlineOpt();
    }

    needsOpenBrace = true;
    needsComma = false;
    newLine = false;

    return true;
  }

  @Override
  public void endVisit(CssRule x, Context ctx) {
    // empty rule block case.
    maybePrintOpenBrace();

    closeBrace();

    newLine = true;
  }

  @Override
  public boolean visit(CssNoFlip x, Context ctx) {
    noFlip = true;
    return true;
  }

  @Override
  public void endVisit(CssNoFlip x, Context ctx) {
    noFlip = false;
  }

  @Override
  public boolean visit(CssProperty x, Context ctx) {
    maybePrintOpenBrace();

    if (noFlip) {
      out.print(NO_FLIP);
      out.print(' ');
    }

    out.print(x.getName());
    colon();

    out.print(printValuesList(x.getValues().getValues()));

    if (x.isImportant()) {
      out.print(IMPORTANT);
    }

    semiColon();

    return true;
  }


  @Override
  public boolean visit(CssElse x, Context ctx) {
    closeBrace();
    out.print(ELSE);
    openBrace();
    newLine = false;

    return true;
  }

  @Override
  public boolean visit(CssElIf x, Context ctx) {
    closeBrace();

    openConditional(ELSE_IF, x);

    return true;
  }

  @Override
  public void endVisit(CssIf x, Context ctx) {
    closeBrace();
    newLine = true;
  }

  @Override
  public boolean visit(CssIf x, Context ctx) {
    out.newline();

    openConditional(IF, x);

    return true;
  }

  private void openConditional(String template, CssIf ifOrElif) {
    String condition;

    String runtimeCondition = extractExpression(ifOrElif);

    if (runtimeCondition != null) {
      //con
      condition = format(EVAL, runtimeCondition);
    } else {
      condition = printConditionnalExpression(ifOrElif);
    }

    out.print(format(template, condition));

    openBrace();
    newLine = false;
  }

  private String extractExpression(CssIf ifOrElif) {
    String condition = ifOrElif.getExpression();

    if (condition == null) {
      return null;
    }

    if (condition.trim().startsWith("(")) {
      condition = condition.substring(1, condition.length() - 1);
    }

    return condition;
  }

  @Override
  public boolean visit(CssFontFace x, Context ctx) {
    out.print("@font-face");
    openBrace();
    return true;
  }

  @Override
  public boolean visit(CssExternalSelectors x, Context ctx) {
    out.print(EXTERNAL);
    for (String className : x.getClasses()) {
      out.print(" ");

      boolean needQuote = className.endsWith("*");

      if (needQuote) {
        out.print("'");
      }

      out.printOpt(className);

      if (needQuote) {
        out.print("'");
      }
    }
    semiColon();
    return false;
  }

  @Override
  public boolean visit(CssMediaRule x, Context ctx) {
    out.print("@media");
    boolean isFirst = true;
    for (String m : x.getMedias()) {
      if (isFirst) {
        out.print(" ");
        isFirst = false;
      } else {
        comma();
      }
      out.print(m);
    }
    spaceOpt();
    out.print("{");
    out.newlineOpt();
    out.indentIn();
    return true;
  }

  @Override
  public boolean visit(CssPageRule x, Context ctx) {
    out.print("@page");
    if (x.getPseudoPage() != null) {
      out.print(" :");
      out.print(x.getPseudoPage());
    }
    spaceOpt();
    out.print("{");
    out.newlineOpt();
    out.indentIn();
    return true;
  }

  @Override
  public boolean visit(CssSelector x, Context ctx) {
    if (needsComma) {
      comma();
    }
    if (newLine) {
      out.newline();
    }

    needsComma = true;
    // TODO add a parameter to decide if we put selectors on the same line
    newLine = true;

    out.print(unescape(x.getSelector()));

    return true;
  }

  @Override
  public boolean visit(CssUrl x, Context ctx) {
    inUrl = true;
    printDef(x, URL, "url");
    return false;
  }

  @Override
  public void endVisit(CssUrl x, Context ctx) {
    inUrl = false;
  }

  private void printDef(CssDef def, String valueTemplate, String atRule) {
    out.print(DEF);

    String name = defKeyMapping.get(def.getKey());

    if (name == null) {
      throw new CssCompilerException("unknown @" + atRule + " rule [" + def.getKey() + "]");
    }

    out.print(name);
    out.print(' ');

    String values = printValuesList(def.getValues());

    if (valueTemplate != null) {
      out.print(format(valueTemplate, values));
    } else {
      out.print(values);
    }

    semiColon();
  }

  private void closeBrace() {
    out.indentOut();
    out.print('}');
    out.newlineOpt();
  }

  private void colon() {
    out.print(':');
    spaceOpt();
  }

  private void comma() {
    out.print(',');
    spaceOpt();
  }

  private void openBrace() {
    spaceOpt();
    out.print('{');
    out.newlineOpt();
    out.indentIn();
  }

  private void semiColon() {
    out.print(';');
    out.newlineOpt();
  }

  private void spaceOpt() {
    out.printOpt(' ');
  }

  private void maybePrintOpenBrace() {
    if (needsOpenBrace) {
      openBrace();
      needsOpenBrace = false;
    }
  }

  private String printConditionnalExpression(CssIf x) {
    if (x == null || x.getExpression() != null) {
      throw new IllegalStateException();
    }

    StringBuilder builder = new StringBuilder();

    String propertyName = x.getPropertyName();

    for (String propertyValue : x.getPropertyValues()) {
      if (builder.length() != 0) {
        builder.append(OR);
      }

      if (x.isNegated()) {
        builder.append(NOT);
      }

      builder.append(format(IS, propertyName, propertyValue));
    }

    return builder.toString();
  }

  private String printValuesList(List<Value> values) {
    StringBuilder builder = new StringBuilder();

    for (Value value : values) {
      if (value.isSpaceRequired() && builder.length() != 0) {
        builder.append(' ');
      }

      String expression = value.toCss();

      if (value.isIdentValue() != null && defKeyMapping.containsKey(expression)) {
        expression = defKeyMapping.get(expression);
      } else if (value.isExpressionValue() != null) {
        expression = value.getExpression();
      } else if (value.isDotPathValue() != null) {
        DotPathValue dotPathValue = value.isDotPathValue();
        if (inUrl || inSprite) {
          expression = dotPathValue.getPath();
        } else {
          if (Strings.isNullOrEmpty(dotPathValue.getSuffix())) {
            expression = format(VALUE, dotPathValue.getPath());
          } else {
            expression = format(VALUE_WITH_SUFFIX, dotPathValue.getPath(), dotPathValue.getSuffix());
          }
        }
      }

      builder.append(unescape(expression));
    }

    return builder.toString();
  }

  private String unescape(String toEscape) {
    return UNESCAPE.matcher(toEscape).replaceAll("");
  }
}
