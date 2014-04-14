package com.google.gwt.resources.client;

public interface TestResources extends ClientBundle {
  public interface SomeGssResource extends GssResource {
    String someClass();
  }

  public SomeGssResource mixin();

  public SomeGssResource add();

  public SomeGssResource eval();

  public SomeGssResource resourceUrl_image();

  public SomeGssResource resourceUrl_data();

  public ImageResource someImageResource();

  @Source("bananaguitar.ani")
  public DataResource someDataResource();
}
