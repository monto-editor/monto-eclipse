package de.tudarmstadt.stg.monto.profiling;

import java.io.PrintWriter;

import de.tudarmstadt.stg.monto.message.Message;

public class Profiler implements AutoCloseable {
	
	private final PrintWriter writer;

	public Profiler(PrintWriter writer) {
		this.writer = writer;
	}
	
	public void start(Class<?> klass, String method, Message message) {
		writer.format("start,%s,%s,%s,%d,%d",
				klass,
				method,
				message.getSource(),
				message.getId().longValue(),
				System.nanoTime());
	}
	
	public void end(Class<?> klass, String method, Message message) {
		writer.format("end,%s,%s,%s,%d,%d",
				klass,
				method,
				message.getSource(),
				message.getId().longValue(),
				System.nanoTime());
	}

	@Override
	public void close() throws Exception {
		writer.close();
	}
}
