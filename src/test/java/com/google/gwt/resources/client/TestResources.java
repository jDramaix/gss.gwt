package com.google.gwt.resources.client;

public interface TestResources extends ClientBundle {
  public interface Mixin extends GssResource {
    String someClass();
  }

  public interface Add extends GssResource {
    String someClass();
  }

  public interface Eval extends GssResource {
    String someClass();
  }

  public Mixin mixin();

  public Add add();

  public Eval eval();
}
