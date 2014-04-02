package com.google.gwt.resources.client;

public interface TestResources extends ClientBundle {
  public interface Styles extends GssResource {
    String someClass();
  }

  @Source("sample.gss")
  public Styles style();
}
