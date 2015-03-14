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
package test.flow.support.match;

import org.springframework.util.StringUtils;


/**
 * {@link Matcher} that identifies Strings based on a pattern containing
 * wildcards (*) and placeholders (?).
 * 
 * @author Dave Syer
 * 
 */
public class PatternMatcher implements Matcher<String> {

	private final String pattern;
	private final String regex;

	/**
	 * Construct a pattern matcher from the pattern provided.
	 * 
	 * @param pattern
	 */
	public PatternMatcher(String pattern) {
		super();
		if (!StringUtils.hasText(pattern)) {
			this.pattern = "*";
		}
		else {
			this.pattern = pattern;
		}
		regex = this.pattern.replace("?", ".").replace("*", ".*?");
	}

	/**
	 * The pattern that matches strings in this matcher
	 * @return the pattern that matches strings in this matcher
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * Check if the provided value matches the pattern, signalling that the
	 * next State should be executed.
	 * 
	 * @param value the value to compare
	 * @return true if the pattern matches this status
	 */
	public boolean match(String value) {
		return value.matches(regex);
	}

	/**
	 * Sorts by decreasing specificity of pattern, based on just counting
	 * wildcards (with * taking precedence over ?). If wildcard counts are equal
	 * then falls back to alphabetic comparison. Hence * &gt; foo* &gt; ??? &gt;
	 * fo? > foo.
	 * @see Comparable#compareTo(Object)
	 */
	public int compareTo(Matcher<String> o) {
		if (o instanceof PatternMatcher) {
			return compareTo((PatternMatcher) o);
		}
		return o.match(pattern) ? -1 : 1;
	}

	private int compareTo(PatternMatcher other) {
		String value = other.pattern;
		if (pattern.equals(value)) {
			return 0;
		}
		int patternCount = StringUtils.countOccurrencesOf(pattern, "*");
		int valueCount = StringUtils.countOccurrencesOf(value, "*");
		if (patternCount > valueCount) {
			return 1;
		}
		if (patternCount < valueCount) {
			return -1;
		}
		patternCount = StringUtils.countOccurrencesOf(pattern, "?");
		valueCount = StringUtils.countOccurrencesOf(value, "?");
		if (patternCount > valueCount) {
			return 1;
		}
		if (patternCount < valueCount) {
			return -1;
		}
		return pattern.compareTo(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PatternMatcher))
			return false;
		PatternMatcher other = (PatternMatcher) obj;
		return other.pattern.equals(pattern);
	}

	@Override
	public int hashCode() {
		return 17 + 79 * pattern.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return pattern;
	}

}
