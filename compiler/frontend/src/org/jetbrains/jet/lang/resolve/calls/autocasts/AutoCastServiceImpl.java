/*
 * Copyright 2010-2012 JetBrains s.r.o.
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

package org.jetbrains.jet.lang.resolve.calls.autocasts;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.scopes.receivers.ReceiverDescriptor;
import com.google.common.collect.Lists;

/**
 * @author abreslav
 */
public class AutoCastServiceImpl implements AutoCastService
{
	private final DataFlowInfo dataFlowInfo;
	private final BindingContext bindingContext;

	public AutoCastServiceImpl(DataFlowInfo dataFlowInfo, BindingContext bindingContext)
	{
		this.dataFlowInfo = dataFlowInfo;
		this.bindingContext = bindingContext;
	}

	@NotNull
	@Override
	public List<ReceiverDescriptor> getVariantsForReceiver(@NotNull ReceiverDescriptor receiverDescriptor)
	{
		List<ReceiverDescriptor> variants = Lists.newArrayList(AutoCastUtils.getAutoCastVariants(bindingContext, dataFlowInfo, receiverDescriptor));
		variants.add(receiverDescriptor);
		return variants;
	}

	@NotNull
	@Override
	public DataFlowInfo getDataFlowInfo()
	{
		return dataFlowInfo;
	}

	@Override
	public boolean isNotNull(@NotNull ReceiverDescriptor receiver)
	{
		if(!receiver.getType().isNullable())
			return true;

		List<ReceiverDescriptor> autoCastVariants = AutoCastUtils.getAutoCastVariants(bindingContext, dataFlowInfo, receiver);
		for(ReceiverDescriptor autoCastVariant : autoCastVariants)
		{
			if(!autoCastVariant.getType().isNullable())
				return true;
		}
		return false;
	}
}
