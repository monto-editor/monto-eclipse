package de.tudarmstadt.stg.monto.message;

import static org.junit.Assert.*;

import org.junit.Test;

public class KeyTest {

	@Test
	public void testThatFreshIdsAreNewerThanOldIds() {
		LongKey id1 = new LongKey(0);
		LongKey id2 = id1.freshId();
		LongKey id3 = id2.freshId();
		
		// assert that newerThan is a total ordering
		// base cases
		assertTrue(id2.newerThan(id1));
		assertTrue(id3.newerThan(id2));
		
		// transitive case
		assertTrue(id3.newerThan(id1));
		
		// base case negation
		assertFalse(id1.newerThan(id2));
		assertFalse(id2.newerThan(id3));

		// transitive negation
		assertFalse(id1.newerThan(id3));

		// non reflexive
		assertFalse(id1.newerThan(id1));
		assertFalse(id2.newerThan(id2));
		assertFalse(id3.newerThan(id3));
	}

}
