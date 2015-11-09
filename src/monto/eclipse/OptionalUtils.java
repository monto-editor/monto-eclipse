package monto.eclipse;

import java.util.Optional;
import java.util.function.Function;

public class OptionalUtils {

	public static <A,B,E extends Exception> Function<A,Optional<B>> withException(PartialFunction<A,B,E> f) {
		return (A a) -> {
			try {
				return Optional.of(f.apply(a));
			} catch(Exception e) {
				Activator.error(e);
				return Optional.empty();
			}
		};
	}
}
