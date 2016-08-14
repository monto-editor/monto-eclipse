package monto.eclipse.completion;

import java.net.URL;
import java.util.List;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IContentProposer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import monto.eclipse.MontoParseController;
import monto.service.completion.Completion;

public class ContentProposer implements IContentProposer {
  ImageRegistry images = new ImageRegistry(Display.getCurrent());

  private Image getImage(URL url) {
    Image img = images.get(url.toString());
    if (img == null) {
      images.put(url.toString(), ImageDescriptor.createFromURL(url));
      img = images.get(url.toString());
    }
    return img;
  }

  @Override
  public ICompletionProposal[] getContentProposals(IParseController parseController, int offset,
      ITextViewer viewer) {
    MontoParseController controller = (MontoParseController) parseController;

    List<Completion> completions = controller.getCompletions();
    if (completions == null)
      return new CompletionProposal[0];

    return completions.stream()
        .map(comp -> new CompletionProposal(comp.getReplacement(), comp.getDeleteBeginOffset(),
            comp.getDeleteLength(), comp.getReplacement().length(), getImage(comp.getIcon()),
            comp.getDescription(), null, null))
        .toArray(size -> new CompletionProposal[size]);
  }
}
