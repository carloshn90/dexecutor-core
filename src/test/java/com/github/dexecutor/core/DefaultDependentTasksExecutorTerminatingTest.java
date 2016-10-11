/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dexecutor.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.github.dexecutor.core.graph.Node;
import com.github.dexecutor.core.support.TestUtil;
import com.github.dexecutor.core.support.ThreadPoolUtil;
import com.github.dexecutor.core.task.Task;
import com.github.dexecutor.core.task.TaskProvider;

/**
 * 
 * @author Nadeem Mohammad
 *
 */
public class DefaultDependentTasksExecutorTerminatingTest {
	
	@Test
	public void testDependentTaskExecution() {
		ExecutorService executorService = newExecutor();
		ExecutionEngine<Integer, Integer> executionEngine = new DefaultExecutionEngine<>(executorService);

		try {
			DefaultDependentTasksExecutor<Integer, Integer> executor = new DefaultDependentTasksExecutor<Integer, Integer>(executionEngine, new SleepyTaskProvider());

			executor.addDependency(1, 2);
			executor.addDependency(1, 2);
			executor.addDependency(1, 3);
			executor.addDependency(3, 4);
			executor.addDependency(3, 5);
			executor.addDependency(3, 6);
			executor.addDependency(2, 7);
			executor.addDependency(2, 9);
			executor.addDependency(2, 8);
			executor.addDependency(9, 10);
			executor.addDependency(12, 13);
			executor.addDependency(13, 4);
			executor.addDependency(13, 14);
			executor.addIndependent(11);

			executor.execute(ExecutionConfig.TERMINATING);
			
			Collection<Node<Integer, Integer>> processedNodesOrder = TestUtil.processedNodesOrder(executor);
			assertThat(processedNodesOrder).containsAll(executionOrderExpectedResult());
			assertThat(processedNodesOrder).size().isGreaterThanOrEqualTo(4);
			
		} finally {
			try {
				executorService.shutdownNow();
				executorService.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				
			}
		}
	}

	private Collection<Node<Integer, Integer>> executionOrderExpectedResult() {
		List<Node<Integer, Integer>> result = new ArrayList<Node<Integer, Integer>>();
		result.add(new Node<Integer, Integer>(1));
		result.add(new Node<Integer, Integer>(2));
		result.add(new Node<Integer, Integer>(11));
		result.add(new Node<Integer, Integer>(12));
		return result;
	}
	private ExecutorService newExecutor() {
		return Executors.newFixedThreadPool(ThreadPoolUtil.ioIntesivePoolSize());
	}

	private static class SleepyTaskProvider implements TaskProvider<Integer, Integer> {
		
		public Task<Integer, Integer> provideTask(final Integer id) {

			return new Task<Integer, Integer>() {

				private static final long serialVersionUID = 1L;

				public Integer execute() {
					if (id == 2) {
						throw new IllegalArgumentException("Invalid task");
					}					
					return id;
				}
			};			
		}		
	}
}
