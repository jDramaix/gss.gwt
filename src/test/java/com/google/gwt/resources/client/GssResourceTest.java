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

package com.google.gwt.resources.client;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.resources.client.TestResources.ClassNameAnnotation;
import com.google.gwt.resources.client.TestResources.EmptyClass;
import com.google.gwt.resources.client.TestResources.ExternalClasses;
import com.google.gwt.resources.client.TestResources.NonStandardAtRules;
import com.google.gwt.resources.client.TestResources.NonStandardFunctions;
import com.google.gwt.resources.client.TestResources.RuntimeConditional;
import com.google.gwt.resources.client.TestResources.SomeGssResource;
import com.google.gwt.resources.client.TestResources.TestImportCss;
import com.google.gwt.resources.client.TestResources.WithConstant;

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

  public void testObfuscationScope() {
    ScopeResource res = GWT.create(ScopeResource.class);

    assertEquals(res.scopeA().foo(), res.scopeA2().foo());
    assertNotSame(res.scopeA().foo(), res.scopeB().foo());
    assertNotSame(res.scopeB().foo(), res.scopeC().foo());
    assertNotSame(res.scopeA().foo(), res.scopeC().foo());
  }

  public void testConstant() {
    WithConstant withConstant = res().withConstant();

    assertEquals("15px", withConstant.constantOne());

    String expectedCss = "." + withConstant.classOne() + "{padding:" + withConstant.constantOne()
        + "}";
    assertEquals(expectedCss, withConstant.getText());
  }

  public void testClassNameAnnotation() {
    ClassNameAnnotation css = res().classNameAnnotation();

    String expectedCss = "." + css.renamedClass() + "{color:black}." + css.nonRenamedClass()
        + "{color:white}";
    assertEquals(expectedCss, css.getText());
  }

  public void testImportAndImportWithPrefix() {
    TestImportCss css = res().testImportCss();
    ImportResource importResource = GWT.create(ImportResource.class);
    ImportResource.ImportCss importCss = importResource.importCss();
    ImportResource.ImportWithPrefixCss importWithPrefixCss = importResource.importWithPrefixCss();

    String expectedCss = "." + css.other() + "{color:black}." + importCss.className() +
        " ." + css.other() + "{color:white}." + importWithPrefixCss.className() + " ." +
        css.other() + "{color:gray}";
    assertEquals(expectedCss, css.getText());
  }

  public void testSharedScope() {
    ScopeResource res = GWT.create(ScopeResource.class);
    TestResources res2 = res();

    // shareClassName1 is shared
    assertEquals(res.sharedParent().sharedClassName1(), res.sharedChild1().sharedClassName1());
    assertEquals(res.sharedParent().sharedClassName1(), res.sharedChild2().sharedClassName1());
    assertEquals(res.sharedParent().sharedClassName1(), res.sharedGreatChild().sharedClassName1());
    assertEquals(res.sharedParent().sharedClassName1(), res2.sharedChild3().sharedClassName1());

    // shareClassName2 is shared
    assertEquals(res.sharedParent().sharedClassName2(), res.sharedChild1().sharedClassName2());
    assertEquals(res.sharedParent().sharedClassName2(), res.sharedChild2().sharedClassName2());
    assertEquals(res.sharedParent().sharedClassName2(), res.sharedGreatChild().sharedClassName2());
    assertEquals(res.sharedParent().sharedClassName2(), res2.sharedChild3().sharedClassName2());

    // nonSharedClassName isn't shared
    assertNotSame(res.sharedChild1().nonSharedClassName(),
        res.sharedChild2().nonSharedClassName());
    assertNotSame(res.sharedChild1().nonSharedClassName(),
        res.sharedGreatChild().nonSharedClassName());
    assertNotSame(res.sharedChild1().nonSharedClassName(),
        res2.sharedChild3().nonSharedClassName());
    assertNotSame(res.sharedChild2().nonSharedClassName(),
        res.sharedGreatChild().nonSharedClassName());
    assertNotSame(res.sharedChild2().nonSharedClassName(),
        res2.sharedChild3().nonSharedClassName());
    assertNotSame(res2.sharedChild3().nonSharedClassName(),
        res.sharedGreatChild().nonSharedClassName());
  }

  public void testConstants() {
    assertEquals("15px", res().cssWithConstant().constantOne());
    assertEquals(5, res().cssWithConstant().constantTwo());
    assertEquals("black", res().cssWithConstant().CONSTANT_THREE());

    assertNotSame("white", res().cssWithConstant().conflictConstantClass());
  }

  public void testNotStrict() {
    SomeGssResource notStrict = res().notstrict();

    String expectedCss = "." + notStrict.someClass() + "{color:black}.otherNotStrictClass{" +
        "color:white}";

    assertEquals(expectedCss, notStrict.getText());
  }

  public void testRuntimeConditional() {
    RuntimeConditional runtimeConditional = res().runtimeConditional();
    String foo = runtimeConditional.foo();

    BooleanEval.FIRST = true;
    BooleanEval.SECOND = true;
    BooleanEval.THIRD = true;

    assertEquals(runtimeExpectedCss("purple", "20px", foo), runtimeConditional.getText());

    BooleanEval.FIRST = false;
    BooleanEval.SECOND = true;
    BooleanEval.THIRD = true;

    assertEquals(runtimeExpectedCss("black", null, foo), runtimeConditional.getText());

    BooleanEval.FIRST = false;
    BooleanEval.SECOND = true;
    BooleanEval.THIRD = false;

    assertEquals(runtimeExpectedCss("khaki", null, foo), runtimeConditional.getText());

    BooleanEval.FIRST = false;
    BooleanEval.SECOND = false;

    assertEquals(runtimeExpectedCss("gray", null, foo), runtimeConditional.getText());
  }

  public void testNonStandardAtRules() {
    NonStandardAtRules nonStandardAtRules = res().nonStandardAtRules();

    String css = nonStandardAtRules.getText();
    assertTrue(css.contains("@extenal"));
    assertTrue(css.contains("@-mozdocument"));
  }

  public void testNonStandardFunctions() {
    NonStandardFunctions nonStandardFunctions = res().nonStandardFunctions();

    String css = nonStandardFunctions.getText();
    assertTrue(css.contains("expression("));
    assertTrue(css.contains("progid:DXImageTransform.Microsoft.gradient("));
  }

  private String runtimeExpectedCss(String color, String padding, String foo) {
    String s =  "." + foo +  "{width:100%}" + "." + foo +  "{color:" + color + "}";

    if (padding != null) {
      s += "." + foo + "{padding:"+ padding + "}";
    }

    return s;
  }


  private TestResources res() {
    return GWT.create(TestResources.class);
  }
}
