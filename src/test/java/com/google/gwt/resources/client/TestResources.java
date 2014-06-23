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

  public SomeGssResource mixin();

  public SomeGssResource add();

  public SomeGssResource eval();

  public SomeGssResource resourceUrl();

  public SomeGssResource missing();

  public SpriteGssResource sprite();

  public ExternalClasses externalClasses();

  public EmptyClass emptyClass();

  public ImageResource someImageResource();

  @Source("bananaguitar.ani")
  public DataResource someDataResource();
}
