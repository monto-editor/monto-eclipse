package de.tudarmstadt.stg.monto.color;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

public class TokenColorer implements ITokenColorer {
	
	Map<Category,Style> styleMap = new HashMap<>();
	
	public TokenColorer() {
		final ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		final ColorRegistry colorRegistry = theme.getColorRegistry();
		final FontRegistry fontRegistry = theme.getFontRegistry();
		for(Category cat : Category.values()) {
			Color color = colorRegistry.get("de.tudarmstadt.stg.monto.category."+cat.toString().toLowerCase());
			Font font = fontRegistry.get("de.tudarmstadt.stg.monto.category.font."+cat.toString().toLowerCase());
			styleMap.put(cat, new Style(color, font));
		}
	}
	
	@Override
	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}
	
	@Override
	public TextAttribute getColoring(final IParseController controller, final Object tokenObj) {
		Token token = (Token) tokenObj;
		if(token == null)
			return null;
		Style style = styleMap.get(token.getCategory());
		return new TextAttribute(style.getColor(), null, 0, style.getFont());
	}

	private static class Style {
		private Color color;
		private Font font;
		public Style(Color color, Font font) {
			this.color = color;
			this.font = font;
		}
		public Color getColor() {
			return color;
		}
		public Font getFont() {
			return font;
		}
	}
}
