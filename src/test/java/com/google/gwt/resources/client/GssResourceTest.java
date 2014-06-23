package com.google.gwt.resources.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.resources.client.TestResources.EmptyClass;
import com.google.gwt.resources.client.TestResources.ExternalClasses;

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

  /**
   * Test that style classes mentioned as external are not obfuscated.
   */
  public void testExternalClasses() {
    ExternalClasses externalClasses = res().externalClasses();

    assertNotSame("obfuscatedClass", externalClasses.obfuscatedClass());

    assertEquals("externalClass", externalClasses.externalClass());
    assertEquals("externalClass2", externalClasses.externalClass2());
    assertEquals("unobfuscated", externalClasses.unobfuscated());
    assertEquals("unobfuscated2", externalClasses.unobfuscated2());
  }

  /**
   * Test that empty class definitions doesn't throw an exception (issue #25) and that they are
   * removed from the resulting css.
   */
  public void testEmptyClass() {
    EmptyClass emptyClass = res().emptyClass();

    assertEquals("", emptyClass.getText());
  }

  /**
   * See issue #6
   */
  public void testCssClassWithMissingInterfaceMethodThrows() {
    try {
      res().missing();
      fail("Missing exception");
    } catch (RuntimeException e) {
      // We expect this exception to be thrown
    }
  }

  public void testObfuscationScope() {
    ScopeResource res = GWT.create(ScopeResource.class);

    assertEquals(res.scopeA().foo(), res.scopeA2().foo());
    assertNotSame(res.scopeA().foo(), res.scopeB().foo());
    assertNotSame(res.scopeB().foo(), res.scopeC().foo());
    assertNotSame(res.scopeA().foo(), res.scopeC().foo());
  }

  private TestResources res() {
    return GWT.create(TestResources.class);
  }
}
