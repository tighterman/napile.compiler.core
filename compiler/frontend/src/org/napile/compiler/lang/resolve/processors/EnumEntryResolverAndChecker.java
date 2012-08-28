/*
 * Copyright 2010-2012 napile.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.napile.compiler.lang.resolve.processors;

import java.util.Map;

import javax.inject.Inject;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.descriptors.ClassDescriptor;
import org.napile.compiler.lang.descriptors.EnumEntryDescriptor;
import org.napile.compiler.lang.descriptors.FunctionDescriptor;
import org.napile.compiler.lang.psi.NapileEnumEntry;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.resolve.BodiesResolveContext;
import org.napile.compiler.lang.resolve.calls.CallMaker;
import org.napile.compiler.lang.resolve.calls.CallResolver;
import org.napile.compiler.lang.resolve.calls.OverloadResolutionResults;
import org.napile.compiler.lang.resolve.calls.autocasts.DataFlowInfo;
import org.napile.compiler.lang.resolve.scopes.JetScope;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import org.napile.compiler.lang.types.TypeUtils;

/**
 * @author VISTALL
 * @date 0:10/29.08.12
 */
public class EnumEntryResolverAndChecker
{
	@NotNull
	private CallResolver callResolver;
	@NotNull
	private BindingTrace trace;

	@Inject
	public void setCallResolver(@NotNull CallResolver callResolver)
	{
		this.callResolver = callResolver;
	}

	@Inject
	public void setTrace(@NotNull BindingTrace trace)
	{
		this.trace = trace;
	}

	public void process(@NotNull BodiesResolveContext bodiesResolveContext)
	{
		for(Map.Entry<NapileEnumEntry, EnumEntryDescriptor> entry : bodiesResolveContext.getEnumEntries().entrySet())
		{
			checkConstructorCall(entry.getKey(), entry.getValue(), bodiesResolveContext.getDeclaringScopes().get(entry.getKey()));
		}
	}

	private void checkConstructorCall(NapileEnumEntry enumEntry, EnumEntryDescriptor enumEntryDescriptor, JetScope jetScope)
	{
		ClassDescriptor classDescriptor = (ClassDescriptor) enumEntryDescriptor.getContainingDeclaration();

		OverloadResolutionResults<FunctionDescriptor> call = callResolver.resolveFunctionCall(trace, jetScope, CallMaker.makeCall(ReceiverDescriptor.NO_RECEIVER, null, enumEntry), TypeUtils.NO_EXPECTED_TYPE, DataFlowInfo.EMPTY);

	}
}
