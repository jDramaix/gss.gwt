gss.gwt
=======

Google Closure Stylesheet support for GWT

Define a gss file

```css
/* Constants*/
@def MY_GRAY #555;
@def PADDING_RIGHT 50px;
@def PADDING_LEFT 50px;

/*mixin */
@defmixin size(WIDTH, HEIGHT) {
  width: WIDTH;
  height: HEIGHT;
}

@defmixin gradient(START_COLOR, END_COLOR) {
  background-color: START_COLOR; /* fallback color if gradients are not supported */
  /* @alternate */ background-image: -moz-linear-gradient(left, START_COLOR 0%, END_COLOR 100%);
  /* @alternate */ background-image: -webkit-linear-gradient(left, START_COLOR 0%, END_COLOR 100%);
  /* @alternate */ background-image: -o-linear-gradient(left, START_COLOR 0%, END_COLOR 100%);
  /* @alternate */ background-image: -ms-linear-gradient(left, START_COLOR 0%, END_COLOR 100%);
  background-image: linear-gradient(left, START_COLOR 0%, END_COLOR 100%);
}

/* CSS3 support */
@-moz-keyframes slidein {
    from {
        margin-left: 100%;
        width: 600px;
    }

    to {
        margin-left: 0%;
        width: 200px;
    }
}

@-webkit-keyframes slidein {
    from {
        margin-left: 100%;
        width: 600px;
    }

    to {
        margin-left: 0%;
        width: 200px;
    }
}

.animate {
    display: block;
    border: 1px solid black;
    border-radius: 5px;
    font-family:"Times New Roman",Georgia,Serif;
    width: 200px;
    -moz-animation-duration: 3s;
    -moz-animation-name: slidein;
    -webkit-animation-duration: 3s;
    -webkit-animation-name: slidein;
}

.class-name,  span[data-text^='styled'] {
    display: block;
    @mixin size(add(PADDING_RIGHT, 150px, PADDING_LEFT), 50px);
    padding-right: PADDING_RIGHT;
    margin-bottom:5px;
    color: MY_GRAY;
    @mixin gradient(#cc0000, #f07575);
}

```

Like with the `CssResource`, define an interface extending `com.google.gwt.resources.client.GssResource`

```java
public interface ApplicationStyle extends GssResource {

    String MY_GRAY();

    String PADDING_RIGHT();

    int PADDING_LEFT();

    @ClassName("class-name")
    String className2();

    String animate();
  }
```

and include it in a `ClientBundle`

```java
public interface ApplicationResources extends ClientBundle {
  @Source("application.gss")
  public ApplicationStyle style();
}
```

and you are now ready to use in your code :

```java
ApplicationResources applicationResources = GWT.create(ApplicationResources.class);
ApplicationStyle style = applicationResources.style();
style.ensureInjected();

// ...
Label l = new Label("My animated label");
l.addStyleName(style.animate());
```

Dependency
----------
- Google closure stylesheet built from source : https://code.google.com/p/closure-stylesheets/wiki/BuildingFromSource
- Google web toolkit built from trunk with this change : https://gwt-review.googlesource.com/#/c/4950/
- Google Guava 12+

