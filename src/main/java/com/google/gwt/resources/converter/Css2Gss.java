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

import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.resources.css.GenerateCssAst;
import com.google.gwt.resources.css.ast.CssStylesheet;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Converter from Css to Gss
 */
public class Css2Gss {

  private final URL cssFile;
  private final PrintWriter printWriter;
  private final boolean lenient;

  public Css2Gss(String filePath) throws MalformedURLException {
    cssFile = new File(filePath).toURI().toURL();
    printWriter = new PrintWriter(System.err);
    lenient = false;
  }

  public Css2Gss(URL fileUrl, PrintWriter outputPrintWiter, boolean lenient) {
    cssFile = fileUrl;
    printWriter = outputPrintWiter;
    this.lenient = lenient;
  }

  public String toGss() {
    try {
      CssStylesheet sheet = GenerateCssAst.exec(new PrintWriterTreeLogger(printWriter), cssFile);

      DefCollectorVisitor defCollectorVisitor = new DefCollectorVisitor();
      defCollectorVisitor.accept(sheet);

      new ElseNodeCreator().accept(sheet);
      new AlternateAnnotationCreatorVisitor().accept(sheet);

      GssGenerationVisitor gssGenerationVisitor = new GssGenerationVisitor(new DefaultTextOutput
          (false), defCollectorVisitor.getDefMapping(), lenient);
      gssGenerationVisitor.accept(sheet);

      return gssGenerationVisitor.getContent();
    } catch (Exception e) {
      printWriter.flush();
      throw new RuntimeException(e);
    }
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
