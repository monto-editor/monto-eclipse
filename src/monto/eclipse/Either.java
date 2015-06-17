package monto.eclipse;

import java.util.function.Function;

public interface Either<A,B> {
	
	public static <A,B> Either<A,B> left(A a) {
		return new Left<A,B>(a);
	}
	
	public static <A,B> Either<A,B> right(B b) {
		return new Right<A,B>(b);
	}
	
	public static <A,B,E extends Exception> Function<A,Either<Exception,B>> either(PartialFunction<A,B,E> f) {
		return a -> {
			try {
				return Either.right(f.apply(a));
			} catch (Exception e) {
				return Either.left(e);
			}
		};	
	}
	
	public static class Left<A,B> implements Either<A,B> {
		private A a;
		public Left(A a) {
			this.a = a;
		
		}
		@Override
		public <C> Either<A, C> map(Function<B, C> f) {
			return Either.left(a);
		}
		
		@Override
		public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) {
			return Either.left(a);
		}
		
		@Override
		public B orElse(B b) {
			return b;
		}
		
		@Override
		public <C> C match(Function<A, C> left, Function<B, C> right) {
			return left.apply(a);
		}
	}
	
	public static class Right<A,B> implements Either<A,B> {
		private B b;
		public Right(B b) {
			this.b = b;
		}
		
		@Override
		public <C> Either<A, C> map(Function<B, C> f) {
			return Either.right(f.apply(b));
		}

		@Override
		public <C> Either<A, C> flatMap(Function<B, Either<A, C>> f) {
			return f.apply(b);
		}

		@Override
		public B orElse(B b) {
			return this.b;
		}

		@Override
		public <C> C match(Function<A, C> left, Function<B, C> right) {
			return right.apply(b);
		}
	}
	
	public <C> Either<A,C> map(Function<B,C> f);
	public <C> Either<A,C> flatMap(Function<B,Either<A,C>> f);
	public B orElse(B b);
	public <C> C match(Function<A,C> left, Function<B,C> right);
}
