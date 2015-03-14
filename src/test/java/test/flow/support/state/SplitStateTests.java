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

import java.util.ArrayList;
import java.util.Collection;

import org.easymock.EasyMock;
import org.junit.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import test.flow.Flow;
import test.flow.FlowResult;

/**
 * @author Dave Syer
 * 
 */
public class SplitStateTests {

	@Test
	public void testBasicHandling() throws Exception {

		Collection<Flow<Object, String>> flows = new ArrayList<Flow<Object, String>>();
		@SuppressWarnings("unchecked")
		Flow<Object, String> flow1 = EasyMock.createMock(Flow.class);
		@SuppressWarnings("unchecked")
		Flow<Object, String> flow2 = EasyMock.createMock(Flow.class);
		flows.add(flow1);
		flows.add(flow2);

		SplitState<Object, String> state = new SplitState<Object, String>("foo", flows);

		EasyMock.expect(flow1.start(null)).andReturn(new FlowResult<Object, String>("step1", null, "COMPLETED"));
		EasyMock.expect(flow2.start(null)).andReturn(new FlowResult<Object, String>("step1", null, "COMPLETED"));
		EasyMock.replay(flow1, flow2);

		String result = state.handle(null);
		assertEquals("COMPLETED", result);

		EasyMock.verify(flow1, flow2);

	}

	@Test
	public void testConcurrentHandling() throws Exception {

		Collection<Flow<Object, String>> flows = new ArrayList<Flow<Object, String>>();
		@SuppressWarnings("unchecked")
		Flow<Object, String> flow1 = EasyMock.createMock(Flow.class);
		@SuppressWarnings("unchecked")
		Flow<Object, String> flow2 = EasyMock.createMock(Flow.class);
		flows.add(flow1);
		flows.add(flow2);

		SplitState<Object, String> state = new SplitState<Object, String>("foo", flows);
		state.setTaskExecutor(new SimpleAsyncTaskExecutor());

		EasyMock.expect(flow1.start(null)).andReturn(new FlowResult<Object, String>("step1", null, "COMPLETED"));
		EasyMock.expect(flow2.start(null)).andReturn(new FlowResult<Object, String>("step1", null, "COMPLETED"));
		EasyMock.replay(flow1, flow2);

		String result = state.handle(null);
		assertEquals("COMPLETED", result);

		EasyMock.verify(flow1, flow2);

	}

}
