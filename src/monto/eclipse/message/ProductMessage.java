package monto.eclipse.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import monto.eclipse.PartialFunction;


public class ProductMessage implements Message {
	
	private final LongKey versionId;
	private final LongKey productId;
	private final Source source;
	private final Product product;
	private final Language language;
	private final Contents contents;
	private final List<Dependency> invalid;
	private final List<Dependency> dependencies;
	
	public ProductMessage(LongKey versionId, LongKey productId, Source source, Product product, Language language, Contents contents, Dependency... dependencies) {
		this(versionId,productId,source,product,language,contents,new ArrayList<>(),Arrays.asList(dependencies));
	}

	public ProductMessage(LongKey versionId, LongKey productId, Source source, Product product, Language language, Contents contents, List<Dependency> invalid2, List<Dependency> dependencies) {
		this.versionId = versionId;
		this.productId= productId;
		this.source = source;
		this.product = product;
		this.language = language;
		this.contents = contents;
		this.invalid = invalid2;
		this.dependencies = dependencies;
	}
	
	public LongKey getVersionId() { return versionId; }
	public LongKey getProductId() { return productId; }
	public Source getSource() { return source; }
	public Product getProduct() { return product; }
	public Language getLanguage() { return language; }
	public Contents getContents() { return contents; }
	@Override public List<Dependency> getInvalid() { return invalid; }
	public List<Dependency> getDependencies() { return dependencies; }
	
	@Override
	public String toString() {
		return String.format("{"
				+ "  vid: %s,\n"
				+ "  pid: %s,\n"
				+ "  source: %s,\n"
				+ "  product: %s,\n"
				+ "  language: %s,\n"
				+ "  contents: %s,\n"
				+ "  dependencies: %s\n"
				+ "}", versionId, productId, source, product, language, contents, dependencies);
	}

	@Override
	public <A, E extends Throwable> A match(
			PartialFunction<VersionMessage, A, E> ver,
			PartialFunction<ProductMessage, A, E> prod) throws E {
		return prod.apply(this);
	}

}
