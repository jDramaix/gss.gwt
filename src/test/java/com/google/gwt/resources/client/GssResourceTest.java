package com.google.gwt.resources.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;

public class GssResourceTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.resources.GssResourceTest";
  }

  public void testSimple() {
    TestResources res = GWT.create(TestResources.class);
    String text = res.style().getText();

    assertEquals(".a-a{background-color:#0b15a1;width:120px;height:100px}", text);
  }
}
