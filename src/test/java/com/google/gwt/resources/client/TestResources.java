package com.google.gwt.resources.client;

public interface TestResources extends ClientBundle {
  public interface SomeGssResource extends GssResource {
    String someClass();
  }

  public interface SpriteGssResource extends GssResource {
    String someClassWithSprite();

    // define a style class having the same name than another resource in the ClientBundle
    // test possible conflict
    String someImageResource();
  }

  public interface ExternalClasses extends GssResource {
    String obfuscatedClass();

    String externalClass();

    String externalClass2();

    String unobfuscated();

    String unobfuscated2();
  }

  public interface EmptyClass extends GssResource {
    String empty();
  }

  public interface WithConstant extends GssResource {
    String constantOne();

    String classOne();
  }

  public interface ClassNameAnnotation extends GssResource {
    @ClassName("renamed-class")
    String renamedClass();

    String nonRenamedClass();
  }

  ClassNameAnnotation classNameAnnotation();

  SomeGssResource mixin();

  SomeGssResource add();

  SomeGssResource eval();

  SomeGssResource resourceUrl();

  SpriteGssResource sprite();

  ExternalClasses externalClasses();

  EmptyClass emptyClass();

  WithConstant withConstant();

  ImageResource someImageResource();

  @Source("bananaguitar.ani")
  DataResource someDataResource();
}
