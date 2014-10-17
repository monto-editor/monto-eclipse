package de.tudarmstadt.stg.monto;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.junitbenchmarks.AbstractBenchmark;
import com.carrotsearch.junitbenchmarks.BenchmarkOptions;

import de.tudarmstadt.stg.monto.broker.PythonBroker;

public class PythonBrokerBenchmark extends AbstractBenchmark {
	
	private static BrokerBenchmark benchmark;
	
	@BeforeClass
	public static void setUp() throws Exception {
		benchmark = new BrokerBenchmark(new PythonBroker());
		benchmark.setUp();
	}
	
	@BenchmarkOptions(benchmarkRounds = 50, warmupRounds = 10)
	@Test
	public void testBroker() throws Exception {
		benchmark.testBroker();
	}
	
	@AfterClass
	public static void tearDown() throws Exception {
		benchmark.tearDown();
	}
}
