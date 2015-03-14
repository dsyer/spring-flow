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
package test.flow.support;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import test.flow.support.match.AlwaysMatcher;
import test.flow.support.match.PatternMatcher;

/**
 * @author Dave Syer
 * 
 */
public class TransitionTests {

	@Test
	public void testIsEnd() {
		Transition<String, String> transition = Transition.createEnd(null, new AlwaysMatcher<String>());
		assertTrue(transition.isEnd());
		assertNull(transition.getNext());
	}

	@Test
	public void testEqualsIsEnd() {
		Transition<String, TransitionTests> transition = Transition.createEnd(null, this);
		assertTrue(transition.matches(this));
		assertTrue(transition.isEnd());
		assertNull(transition.getNext());
	}

	@Test
	public void testMatchesStar() {
		Transition<String, String> transition = Transition.create(null, new PatternMatcher("*"), "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesNull() {
		Transition<String, String> transition = Transition.create(null, null, "start");
		assertTrue(transition.matches("CONTINUABLE"));
	}

	@Test
	public void testMatchesEquals() {
		Transition<String, TransitionTests> transition = Transition.create(null, this, "start");
		assertTrue(transition.matches(this));
	}

	@Test
	public void testNonEquals() {
		Transition<String, String> transition = Transition.create(null, "*", "start");
		assertFalse(transition.equals(this));
	}

	@Test
	public void testEquals() {
		Transition<String, String> transition = Transition.create(null, "*", "start");
		Transition<String, String> other = Transition.create(null, new PatternMatcher("*"), "start");
		assertTrue(transition.equals(other));
	}

	@Test
	public void testToString() {
		Transition<String, String> transition = Transition.create(null, new PatternMatcher("CONTIN???LE"), "start");
		String string = transition.toString();
		assertTrue("Wrong string: " + string, string.contains("Transition"));
		assertTrue("Wrong string: " + string, string.contains("start"));
		assertTrue("Wrong string: " + string, string.contains("CONTIN???LE"));
		assertTrue("Wrong string: " + string, string.contains("next="));
	}

}
