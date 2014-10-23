package de.tudarmstadt.stg.monto;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;

import de.tudarmstadt.stg.monto.token.Token;

public class TokenColorer implements ITokenColorer {
	
	@Override
	public IRegion calculateDamageExtent(IRegion seed, IParseController ctlr) {
		return seed;
	}
	
	@Override
	public TextAttribute getColoring(final IParseController controller, final Object tokenObj) {
		Token token = (Token) tokenObj;
		Color tokenColor = getColor(token);
		Font tokenFont = getFont(token);
		return new TextAttribute(tokenColor, null, 0, tokenFont);
	}

	public static Font getFont(Token token) {
		return null;
	}

	public static Color getColor(Token token) {
		ITheme theme = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme();
		ColorRegistry colorRegistry = theme.getColorRegistry();
		String key = String.format("de.tudarmstadt.stg.monto.category.%s",token.getCategory().toString().toLowerCase());
		return colorRegistry.get(key);		
	}
}
