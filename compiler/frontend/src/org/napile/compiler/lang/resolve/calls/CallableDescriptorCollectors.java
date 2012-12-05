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

package org.napile.compiler.lang.resolve.calls;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.descriptors.CallableDescriptor;
import org.napile.compiler.lang.descriptors.ClassifierDescriptor;
import org.napile.compiler.lang.descriptors.ConstructorDescriptor;
import org.napile.compiler.lang.descriptors.MethodDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.resolve.scopes.JetScope;
import org.napile.compiler.lang.types.ErrorUtils;
import org.napile.compiler.lang.types.JetType;
import com.google.common.collect.Lists;

/**
 * @author abreslav
 */
public class CallableDescriptorCollectors
{

	/*package*/ static CallableDescriptorCollector<MethodDescriptor> FUNCTIONS = new CallableDescriptorCollector<MethodDescriptor>()
	{

		@NotNull
		@Override
		public Collection<MethodDescriptor> getNonExtensionsByName(JetScope scope, Name name, boolean set)
		{
			return fromScope(scope, name);
		}

		@NotNull
		@Override
		public Collection<MethodDescriptor> getMembersByName(@NotNull JetType receiverType, Name name, boolean set)
		{
			return fromScope(receiverType.getMemberScope(), name);
		}

		@NotNull
		@Override
		public Collection<MethodDescriptor> getNonMembersByName(JetScope scope, Name name, boolean set)
		{
			return Collections.emptyList();
		}

		private Collection<MethodDescriptor> fromScope(JetScope scope, Name name)
		{
			Collection<MethodDescriptor> methodDescriptors = scope.getMethods(name);
			Collection<ConstructorDescriptor> constructorDescriptors = Collections.emptyList();

			ClassifierDescriptor classifier = scope.getClassifier(name);
			if(classifier != null && !ErrorUtils.isError(classifier.getTypeConstructor()))
				constructorDescriptors = classifier.getConstructors();

			Set<MethodDescriptor> members = new HashSet<MethodDescriptor>(methodDescriptors.size() + constructorDescriptors.size());
			members.addAll(methodDescriptors);
			members.addAll(constructorDescriptors);
			return members;
		}
	};

	/*package*/ static CallableDescriptorCollector<VariableDescriptor> VARIABLES = new CallableDescriptorCollector<VariableDescriptor>()
	{

		@NotNull
		@Override
		public Collection<VariableDescriptor> getNonExtensionsByName(JetScope scope, Name name, boolean set)
		{
			return Collections.emptyList();
		}

		@NotNull
		@Override
		public Collection<VariableDescriptor> getMembersByName(@NotNull JetType receiverType, Name name, boolean set)
		{
			return receiverType.getMemberScope().getProperties(name);
		}

		@NotNull
		@Override
		public Collection<VariableDescriptor> getNonMembersByName(JetScope scope, Name name, boolean set)
		{
			return Collections.emptyList();
		}
	};



	/*package*/ static List<CallableDescriptorCollector<? extends CallableDescriptor>> FUNCTIONS_AND_VARIABLES = Lists.newArrayList(FUNCTIONS, VARIABLES);
}
