package de.tudarmstadt.stg.monto.message;

import java.util.List;

import de.tudarmstadt.stg.monto.Either;
import de.tudarmstadt.stg.monto.PartialFunction;

public class Messages {
	public static Either<Exception,VersionMessage> getVersionMessage(List<Message> messages) {
		if(messages == null)
			return Either.left(new IllegalArgumentException("Message list was null"));
		return messages
			.stream()
			.filter(msg -> msg instanceof VersionMessage)
			.findFirst()
			.<Either<Exception,VersionMessage>>map(msg -> Either.right((VersionMessage) msg))
			.orElse(Either.left(new IllegalArgumentException("VersionMessage missing")));
	}
	
	public static Either<Exception,ProductMessage> getProductMessage(List<Message> messages, Product product, Language language) {
		if(messages == null)
			return Either.left(new IllegalArgumentException("Message list was null"));
		return messages.stream()
			.filter(msg -> {
				if(msg instanceof ProductMessage) {
					ProductMessage msg1 = (ProductMessage) msg;
					return msg1.getProduct().equals(product) && msg1.getLanguage().equals(language);
				} else {
					return false;
				}
			}).findAny()
			.<Either<Exception,ProductMessage>>map(msg -> Either.right((ProductMessage) msg))
			.orElse(Either.left(new IllegalArgumentException(String.format("ProductMessage missing: %s, %s", product,language))));
	}
	
	public static <A> Either<Exception,A> getProductMessage(List<Message> messages, Product product, Language language, PartialFunction<ProductMessage,A,Exception> parser) {
		return getProductMessage(messages, product, language)
			.<A>flatMap(Either.<ProductMessage,A,Exception>either(parser));
		
	}
}
