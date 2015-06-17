package monto.eclipse.message;

import java.util.List;

import monto.eclipse.PartialFunction;

public interface Message {
	public LongKey getVersionId();
	public Source getSource();
	public List<Dependency> getInvalid();
	public Language getLanguage();
	public <A,E extends Throwable> A match(PartialFunction<VersionMessage,A,E> ver, PartialFunction<ProductMessage,A,E> prod) throws E;
}
