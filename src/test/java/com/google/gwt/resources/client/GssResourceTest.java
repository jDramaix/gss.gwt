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

  public void testGetResourceUrl_imageResource() {
    String text = res().getResourceUrl_image().getText();

    assertTrue(text.contains("{background-image:url(" + res().someImageResource().getSafeUri().asString() + ")}"));
  }

  public void testData() {
    String text = res().getResourceUrl_data().getText();

    assertTrue(text.contains("{cursor:url(" + res().someDataResource().getSafeUri().asString() + ")}"));
  }

  private TestResources res() {
    return GWT.create(TestResources.class);
  }
}
