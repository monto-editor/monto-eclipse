package de.tudarmstadt.stg.monto.profiling;

import java.io.PrintWriter;

import de.tudarmstadt.stg.monto.message.Message;

public class Profiler implements AutoCloseable {
	
	private final PrintWriter writer;

	public Profiler(PrintWriter writer) {
		this.writer = writer;
	}
	
	public void start(Class<?> klass, String method, Message message) {
		writer.format("start,%s,%s,%s,%d,%d\n",
				klass.getSimpleName(),
				method,
				message.getSource(),
				message.getVersionId().longValue(),
				System.nanoTime());
	}
	
	public void start(Class<?> klass, String method, Message message, long time) {
		writer.format("start,%s,%s,%s,%d,%d\n",
				klass.getSimpleName(),
				method,
				message.getSource(),
				message.getVersionId().longValue(),
				time);
	}
	
	public void end(Class<?> klass, String method, Message message) {
		writer.format("end,%s,%s,%s,%d,%d\n",
				klass.getSimpleName(),
				method,
				message.getSource(),
				message.getVersionId().longValue(),
				System.nanoTime());
	}
	
	public void end(Class<?> klass, String method, Message message, long time) {
		writer.format("end,%s,%s,%s,%d,%d\n",
				klass.getSimpleName(),
				method,
				message.getSource(),
				message.getVersionId().longValue(),
				time);
	}
	
	@Override
	public void close() throws Exception {
		writer.close();
	}
}
