/*
 * Copyright 2006-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.flow.support.state;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

/**
 * @author Dave Syer
 * 
 */
public class MaxValueSplitAggregatorTests {
	
	public static enum Status {
		UNKNOWN, COMPLETED, FAILED;
	}

	private MaxValueSplitAggregator<Status> aggregator = new MaxValueSplitAggregator<Status>(Status.UNKNOWN);

	@Test
	public void testFailed() throws Exception {
		Status first = Status.COMPLETED;
		Status second = Status.FAILED;
		assertTrue("Should be negative", first.compareTo(second)<0);
		assertTrue("Should be positive", second.compareTo(first)>0);
		assertEquals(Status.FAILED, aggregator.aggregate(Arrays.asList(first, second)));
	}

	@Test
	public void testEmpty() throws Exception {
		assertEquals(Status.UNKNOWN, aggregator.aggregate(Collections.<Status> emptySet()));
	}

	@Test(expected=IllegalArgumentException.class)
	public void testEmptyNoUnknown() throws Exception {
		aggregator = new MaxValueSplitAggregator<Status>();
		assertEquals(Status.UNKNOWN, aggregator.aggregate(Collections.<Status> emptySet()));
	}

}
