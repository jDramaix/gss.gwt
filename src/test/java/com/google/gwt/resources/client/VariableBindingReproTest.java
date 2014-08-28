package com.google.gwt.resources.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.resources.client.ClientBundle.Source;

public class VariableBindingReproTest extends GWTTestCase {

  interface Bundle {
    public static Bundle INSTANCE = GWT.create(Bundle.class);

    @Source({"defs_first.css", "defs_second.css"})
    Css css();
  }

  interface Css extends GssResource {
    int rootTop();

    String foo();
  }

  @Override
  public String getModuleName() {
    return "com.google.gwt.resources.AutoConversion";
  }

  public void test() {
    Css css = Bundle.INSTANCE.css();

    String text = css.getText();

    assertEquals("", text);

  }

}
