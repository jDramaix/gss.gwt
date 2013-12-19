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

import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.dev.util.DefaultTextOutput;
import com.google.gwt.dev.util.log.PrintWriterTreeLogger;
import com.google.gwt.resources.css.GenerateCssAst;
import com.google.gwt.resources.css.ast.CssStylesheet;

import java.io.File;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class Css2Gss {

  private URL cssFile;

  public Css2Gss(String filePath) throws MalformedURLException {
    cssFile = new File(filePath).toURI().toURL();
  }

  public String toGss() throws UnableToCompleteException {
    CssStylesheet sheet = GenerateCssAst.exec(new PrintWriterTreeLogger(new PrintWriter(System
        .out)), cssFile);

    DefCollectorVisitor defCollectorVisitor = new DefCollectorVisitor();
    defCollectorVisitor.accept(sheet);


    GssGenerationVisitor gssGenerationVisitor = new GssGenerationVisitor(new DefaultTextOutput
        (false), defCollectorVisitor.getDefMapping());
    gssGenerationVisitor.accept(sheet);

    return gssGenerationVisitor.getContent();
  }

  public static void main(String... args) {
    try {
      System.err.println(new Css2Gss("//Users/julien/git/gss" +
          ".gwt/src/main/java/com/google/gwt/resources/converter/test.css").toGss());
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(-1);
    }

    System.exit(0);
  }
}
