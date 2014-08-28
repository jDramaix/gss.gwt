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

import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.resources.css.ExternalClassesCollector;
import com.google.gwt.resources.css.GenerateCssAst;
import com.google.gwt.resources.css.ast.CssStylesheet;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Converter from Css to Gss
 */
public class Css2Gss {
  private final URL cssFile;
  private final TreeLogger treeLogger;
  private final boolean lenient;

  private Map<String, String> defNameMapping;

  public Css2Gss(String filePath) throws MalformedURLException {
    this(new File(filePath).toURI().toURL(), false);
  }

  public Css2Gss(URL fileUrl, TreeLogger treeLogger) {
    this(fileUrl, treeLogger, false);
  }

  public Css2Gss(URL fileUrl, TreeLogger treeLogger, boolean lenient) {
    cssFile = fileUrl;
    this.treeLogger = treeLogger;
    this.lenient = lenient;
  }

  public Css2Gss(URL resource, boolean lenient) {
    this(resource, new PrintWriterTreeLogger(new PrintWriter(System.out)), lenient);
  }

  public String toGss() throws UnableToCompleteException {
      CssStylesheet sheet = GenerateCssAst.exec(treeLogger, cssFile);

      DefCollectorVisitor defCollectorVisitor = new DefCollectorVisitor(lenient, treeLogger);
      defCollectorVisitor.accept(sheet);
      defNameMapping = defCollectorVisitor.getDefMapping();

      ExternalClassesCollector externalClassesCollector = new ExternalClassesCollector();
      externalClassesCollector.accept(sheet);
      SortedSet<String> classes = externalClassesCollector.getClasses();
      removeWrongEntries(classes);
      removeWrongEscaping(classes);
      removePseudoClasses(classes, lenient);

      new UndefinedConstantVisitor(new HashSet<String>(defNameMapping.values()),
          lenient, treeLogger).accept(sheet);
      new ElseNodeCreator().accept(sheet);

      new AlternateAnnotationCreatorVisitor().accept(sheet);

      new FontFamilyVisitor().accept(sheet);

      GssGenerationVisitor gssGenerationVisitor = new GssGenerationVisitor(
          new DefaultTextOutput(false), defCollectorVisitor.getDefMapping(), classes,
          defCollectorVisitor.getConstantNodes(), lenient, treeLogger);
      gssGenerationVisitor.accept(sheet);

      return gssGenerationVisitor.getContent();
  }

  /**
   * GSS allows only uppercase letters and numbers for a name of the constant. The constants
   * need to be renamed in order to be compatible with GSS. This method returns a mapping
   * between the old name and the new name compatible with GSS.
   */
  public Map<String, String> getDefNameMapping() {
    return defNameMapping;
  }

  private void removePseudoClasses(SortedSet<String> classes, boolean lenient) {
    Set<String> toRemove = new HashSet<String>();

    for (String clazzName : classes) {
      if (clazzName.contains(":")) {
        if (lenient) {
          toRemove.add(clazzName);
        } else {
          throw new Css2GssConversionException(
              "One of your external statements contains a pseudo class: " + clazzName);
        }
      }
    }
    classes.removeAll(toRemove);
  }

  private void removeWrongEscaping(SortedSet<String> classes) {
    Set<String> toRemove = new HashSet<String>();
    Set<String> toAdd = new HashSet<String>();

    for (String clazzName : classes) {
      if (clazzName.contains("\\-")) {
        toRemove.add(clazzName);
        toAdd.add(clazzName.replace("\\-", "-"));
      }
    }
    classes.removeAll(toRemove);
    classes.addAll(toAdd);
  }

  private void removeWrongEntries(SortedSet<String> classes) {
    Set<String> toRemove = new HashSet<String>();
    Set<String> toAdd = new HashSet<String>();
    for (String entry : classes) {
      if(entry.contains("@external")){
        toRemove.add(entry);
        entry = entry.replace("@external", "");
        entry = entry.replace(",", "");
        entry = entry.replace("\n", "");
        entry = entry.replace("\r", "");
        toAdd.add(entry);
      }
    }
    classes.removeAll(toRemove);
    classes.addAll(toAdd);
  }

  public static void main(String... args) {
    if (args.length != 1) {
      printUsage();
      System.exit(-1);
    }

    try {
      System.out.println(new Css2Gss(args[0]).toGss());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    System.exit(0);
  }

  private static void printUsage() {
    System.err.println("Usage :");
    System.err.println("java " + Css2Gss.class.getName() + " fileNameToConvertPath");
  }
}
