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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import test.flow.support.match.PatternMatcher;

/**
 * @author Dan Garrette
 * @author Dave Syer
 */
public class PatternMatcherTests {

	@Test
	public void testMatchNoWildcardYes() {
		assertTrue(new PatternMatcher("abc").match("abc"));
	}

	@Test
	public void testMatchNoWildcardNo() {
		assertFalse(new PatternMatcher("abc").match("ab"));
	}

	@Test
	public void testMatchSingleYes() {
		assertTrue(new PatternMatcher("a?c").match("abc"));
	}

	@Test
	public void testMatchSingleNo() {
		assertFalse(new PatternMatcher("a?c").match("ab"));
	}

	@Test
	public void testMatchSingleWildcardNo() {
		assertTrue(new PatternMatcher("a?*").match("abc"));
	}

	@Test
	public void testMatchStarYes() {
		assertTrue(new PatternMatcher("a*c").match("abdegc"));
	}

	@Test
	public void testMatchTwoStars() {
		assertTrue(new PatternMatcher("a*d*").match("abcdeg"));
	}

	@Test
	public void testMatchPastEnd() {
		assertFalse(new PatternMatcher("a*de").match("abcdeg"));
	}

	@Test
	public void testMatchPastEndTwoStars() {
		assertTrue(new PatternMatcher("a*d*g*").match("abcdeg"));
	}

	@Test
	public void testMatchStarAtEnd() {
		assertTrue(new PatternMatcher("ab*").match("ab"));
	}

	@Test
	public void testMatchPlaceholderAtEnd() {
		assertTrue(new PatternMatcher("a*c?").match("abcd"));
	}

	@Test
	public void testNoMatchStar() {
		assertFalse(new PatternMatcher("a*c").match("abdeg"));
	}

	@Test
	public void testmatchEmpty() {
		assertTrue(new PatternMatcher("").match("CONTINUABLE"));
	}

	@Test
	public void testmatchExact() {
		PatternMatcher matcher = new PatternMatcher("CONTINUABLE");
		assertTrue(matcher.match("CONTINUABLE"));
	}

	@Test
	public void testmatchWildcard() {
		PatternMatcher matcher = new PatternMatcher("CONTIN*");
		assertTrue(matcher.match("CONTINUABLE"));
	}

	@Test
	public void testmatchPlaceholder() {
		PatternMatcher matcher = new PatternMatcher("CONTIN???LE");
		assertTrue(matcher.match("CONTINUABLE"));
	}

	@Test
	public void testSimpleOrderingEqual() {
		PatternMatcher matcher = new PatternMatcher("CONTIN???LE");
		assertEquals(0, matcher.compareTo(matcher));
	}

	@Test
	public void testSimpleOrderingMoreGeneral() {
		PatternMatcher matcher = new PatternMatcher("CONTIN???LE");
		PatternMatcher other = new PatternMatcher("CONTINUABLE");
		assertEquals(1, matcher.compareTo(other));
		assertEquals(-1, other.compareTo(matcher));
	}

	@Test
	public void testSimpleOrderingMostGeneral() {
		PatternMatcher matcher = new PatternMatcher("*");
		PatternMatcher other = new PatternMatcher("CONTINUABLE");
		assertEquals(1, matcher.compareTo(other));
		assertEquals(-1, other.compareTo(matcher));
	}

	@Test
	public void testSubstringAndWildcard() {
		PatternMatcher matcher = new PatternMatcher("CONTIN*");
		PatternMatcher other = new PatternMatcher("CONTINUABLE");
		assertEquals(1, matcher.compareTo(other));
		assertEquals(-1, other.compareTo(matcher));
	}

	@Test
	public void testSimpleOrderingMostToNextGeneral() {
		PatternMatcher matcher = new PatternMatcher("*");
		PatternMatcher other = new PatternMatcher("C?");
		assertEquals(1, matcher.compareTo(other));
		assertEquals(-1, other.compareTo(matcher));
	}

	@Test
	public void testSimpleOrderingAdjacent() {
		PatternMatcher matcher = new PatternMatcher("CON*");
		PatternMatcher other = new PatternMatcher("CON?");
		assertEquals(1, matcher.compareTo(other));
		assertEquals(-1, other.compareTo(matcher));
	}


}
