package monto.eclipse;

import monto.service.outline.Outline;

public class ParseResult {

  private Outline outline;

  public ParseResult(Outline outline, String documentText) {
    this.outline = outline;
  }

  public Outline getOutline() {
    return outline;
  }
}
