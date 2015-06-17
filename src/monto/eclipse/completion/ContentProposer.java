package monto.eclipse.completion;

import java.util.List;

import monto.eclipse.MontoParseController;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ContentProposer implements IContentProposer {

	ISharedImages images = JavaUI.getSharedImages();
	
	@Override
	public ICompletionProposal[] getContentProposals(IParseController parseController,
			int offset, ITextViewer viewer) {
		MontoParseController controller = (MontoParseController) parseController;
		
		List<Completion> completions = controller.getCompletions();
		if(completions == null)
			return new CompletionProposal[0];
		
		return completions
			.stream()
			.map(comp -> new CompletionProposal(
					comp.getReplacement(),
					comp.getInsertionOffset(),
					0,
					comp.getReplacement().length(),
					images.getImage(comp.getIcon()),
					comp.getDescription(),
					null,
					null))
			.toArray(size -> new CompletionProposal[size]);
	}

}
