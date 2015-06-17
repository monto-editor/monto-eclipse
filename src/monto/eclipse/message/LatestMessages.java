package monto.eclipse.message;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LatestMessages<M extends Message> {
	private Map<Source,M> messages = new HashMap<>();
	
	public IsNewer<M> addMessage(M message) {
		synchronized(messages) {
			messages.putIfAbsent(message.getSource(), message);
			M latest = messages.get(message.getSource());
			if(message.getVersionId().newerThan(latest.getVersionId())) {
				messages.put(message.getSource(), message);
				return new Newer<M>(message);
			} else {
				return new Older<M>();
			}
		}
	}
	
	public M getNewest(Source source) {
		synchronized(messages) {
			return messages.get(source);
		}
	}
	
	public static interface IsNewer<M> {
		public void isNewer(Consumer<M> cons);
	}
	
	public static class Newer<M> implements IsNewer<M> {

		M message;
		public Newer(M message) {
			this.message = message;
		}
		
		@Override
		public void isNewer(Consumer<M> cons) {
			cons.accept(message);
		}
	}
	
	public static class Older<M> implements IsNewer<M> {

		@Override
		public void isNewer(Consumer<M> cons) {
			// skip calling the consumer
		}
		
	}
}
