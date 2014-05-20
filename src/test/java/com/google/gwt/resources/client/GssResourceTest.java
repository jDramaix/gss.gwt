package com.google.gwt.resources.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;

public class GssResourceTest extends GWTTestCase {
  @Override
  public String getModuleName() {
    return "com.google.gwt.resources.GssResourceTest";
  }

  public void testMixin() {
    String text = res().mixin().getText();

    assertTrue(text.contains("{width:120px;height:100px}"));
  }

  public void testAdd() {
    String text = res().add().getText();

    assertTrue(text.contains("{width:220px}"));
  }

  public void testEval() {
    String text = res().eval().getText();

    assertTrue(text.contains("{color:#fff;background-color:#f00}"));
  }

  public void testSprite() {
    String text = res().sprite().getText();

    String expected = "{height:64px;width:64px;overflow:hidden;background:url(" + res()
        .someImageResource().getSafeUri().asString() + ") -0px -0px  no-repeat}";

    assertTrue(text.contains(expected));
  }

  public void testResourceUrl() {
    String text = res().resourceUrl().getText();

    String expected = "{cursor:url(" + res().someDataResource().getSafeUri().asString() + ");"
            + "background-image:url(" + res().someImageResource().getSafeUri().asString() + ");"
            + "cursor:url(" + res().someDataResource().getSafeUri().asString() + ");"
            + "background-image:url(" + res().someImageResource().getSafeUri().asString() + ")}";
    assertTrue(text.contains(expected));
  }

  private TestResources res() {
    return GWT.create(TestResources.class);
  }
}
