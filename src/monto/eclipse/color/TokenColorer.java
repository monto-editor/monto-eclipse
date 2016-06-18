package monto.eclipse.color;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import monto.service.token.Token;

public class TokenColorer implements ITokenColorer {

  Map<monto.service.token.Font, Font> fontMap = new HashMap<>();
  Map<monto.service.token.Color, Color> colorMap = new HashMap<>();
  private Color defaultColor;
  private Font defFont;

  public TokenColorer() {
    defaultColor = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
    Display.getDefault().syncExec(() -> {
      defFont = Display.getDefault().getSystemFont();
    });

  }

  @Override
  public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
    return seed;
  }

  private static int getStyle(String style) {
    switch (style) {
      case "bold":
        return SWT.BOLD;
      case "italic":
        return SWT.ITALIC;
      case "oblique":
        return SWT.ITALIC;
      default:
        return SWT.NORMAL;
    }
  }

  private Font getFont(monto.service.token.Font font) {
    return fontMap.computeIfAbsent(font, fnt -> {
      if (defFont.getFontData().length == 0)
        return defFont;
      else
        return new Font(Display.getDefault(),
            fnt.getFamily().orElse(defFont.getFontData()[0].getName()),
            fnt.getSize().orElse(defFont.getFontData()[0].getHeight()),
            fnt.getStyle().map(TokenColorer::getStyle).orElse(SWT.NORMAL));
    });
  }


  private Color getColor(monto.service.token.Color color) {
    return new Color(Display.getDefault(), color.getRed(), color.getGreen(), color.getBlue());
  }

  @Override
  public TextAttribute getColoring(final IParseController controller, final Object tokenObj) {
    Token token = (Token) tokenObj;
    if (token == null)
      return null;
    Font font = defFont; // fontMap.computeIfAbsent(token.getFont(), fnt -> getFont(fnt));
    Color color = token.getFont().getColor()
        .map(clr -> colorMap.computeIfAbsent(clr, clr1 -> getColor(clr1))).orElse(defaultColor);

    return new TextAttribute(color, null, 0, font);
  }
}
