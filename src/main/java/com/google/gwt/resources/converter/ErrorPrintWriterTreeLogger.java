package com.google.gwt.resources.converter;

import com.google.gwt.dev.util.log.AbstractTreeLogger;

import java.io.PrintWriter;
import java.net.URL;

public final class ErrorPrintWriterTreeLogger extends AbstractTreeLogger {

  private final String indent;

  private final PrintWriter out;
  
  private final Object mutex = new Object();

  public ErrorPrintWriterTreeLogger(PrintWriter out) {
    this(out, "");
  }

  protected ErrorPrintWriterTreeLogger(PrintWriter out, String indent) {
    this.out = out;
    this.indent = indent;
  }

  @Override
  protected AbstractTreeLogger doBranch() {
    return new ErrorPrintWriterTreeLogger(out, indent + "   ");
  }

  @Override
  protected void doCommitBranch(AbstractTreeLogger childBeingCommitted,
      Type type, String msg, Throwable caught, HelpInfo helpInfo) {
    doLog(childBeingCommitted.getBranchedIndex(), type, msg, caught, helpInfo);
  }

  @Override
  protected void doLog(int indexOfLogEntryWithinParentLogger, Type type,
      String msg, Throwable caught, HelpInfo helpInfo) {
    if (type != Type.ERROR) {
      return;
    }

    synchronized (mutex) { // ensure thread interleaving...
      out.print(indent);
      if (type.needsAttention()) {
        out.print("[");
        out.print(type.getLabel());
        out.print("] ");
      }

      out.println(msg);
      if (helpInfo != null) {
        URL url = helpInfo.getURL();
        if (url != null) {
          out.print(indent);
          out.println("For additional info see: " + url.toString());
        }
      }
      if (caught != null) {
        caught.printStackTrace(out);
      }
    }
  }
}
