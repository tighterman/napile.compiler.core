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

package org.napile.compiler.lang.cfg;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.cfg.PseudocodeTraverser.Edges;
import org.napile.compiler.lang.cfg.PseudocodeTraverser.InstructionAnalyzeStrategy;
import org.napile.compiler.lang.cfg.PseudocodeTraverser.InstructionDataMergeStrategy;
import org.napile.compiler.lang.cfg.pseudocode.Instruction;
import org.napile.compiler.lang.cfg.pseudocode.LocalDeclarationInstruction;
import org.napile.compiler.lang.cfg.pseudocode.Pseudocode;
import org.napile.compiler.lang.cfg.pseudocode.PseudocodeUtil;
import org.napile.compiler.lang.cfg.pseudocode.ReadValueInstruction;
import org.napile.compiler.lang.cfg.pseudocode.VariableDeclarationInstruction;
import org.napile.compiler.lang.cfg.pseudocode.WriteValueInstruction;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.psi.NapileDeclaration;
import org.napile.compiler.lang.psi.NapileVariable;
import org.napile.compiler.lang.resolve.BindingTraceKeys;
import org.napile.compiler.lang.resolve.BindingTrace;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author svtk
 */
public class PseudocodeVariablesData
{
	private final Pseudocode pseudocode;
	private final BindingTrace bindingTrace;

	private final Map<Pseudocode, Set<VariableDescriptor>> declaredVariablesInEachDeclaration = Maps.newHashMap();
	private final Map<Pseudocode, Set<VariableDescriptor>> usedVariablesInEachDeclaration = Maps.newHashMap();

	private Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> variableInitializersMap;
	private Map<Instruction, Edges<Map<VariableDescriptor, VariableUseState>>> variableStatusMap;

	public PseudocodeVariablesData(@NotNull Pseudocode pseudocode, @NotNull BindingTrace bindingTrace)
	{
		this.pseudocode = pseudocode;
		this.bindingTrace = bindingTrace;
	}

	@NotNull
	public Pseudocode getPseudocode()
	{
		return pseudocode;
	}

	@NotNull
	public Set<VariableDescriptor> getUsedVariables(@NotNull Pseudocode pseudocode)
	{
		Set<VariableDescriptor> usedVariables = usedVariablesInEachDeclaration.get(pseudocode);
		if(usedVariables == null)
		{
			final Set<VariableDescriptor> result = Sets.newHashSet();
			PseudocodeTraverser.traverse(pseudocode, true, new InstructionAnalyzeStrategy()
			{
				@Override
				public void execute(@NotNull Instruction instruction)
				{
					VariableDescriptor variableDescriptor = PseudocodeUtil.extractVariableDescriptorIfAny(instruction, false, bindingTrace);
					if(variableDescriptor != null)
					{
						result.add(variableDescriptor);
					}
				}
			});
			usedVariables = Collections.unmodifiableSet(result);
			usedVariablesInEachDeclaration.put(pseudocode, usedVariables);
		}
		return usedVariables;
	}

	@NotNull
	public Set<VariableDescriptor> getDeclaredVariables(@NotNull Pseudocode pseudocode)
	{
		Set<VariableDescriptor> declaredVariables = declaredVariablesInEachDeclaration.get(pseudocode);
		if(declaredVariables == null)
		{
			declaredVariables = Sets.newHashSet();
			for(Instruction instruction : pseudocode.getInstructions())
			{
				if(instruction instanceof VariableDeclarationInstruction)
				{
					NapileDeclaration variableDeclarationElement = ((VariableDeclarationInstruction) instruction).getVariableDeclarationElement();
					VariableDescriptor descriptor = bindingTrace.get(BindingTraceKeys.VARIABLE, variableDeclarationElement);
					if(descriptor != null)
						declaredVariables.add(descriptor);
				}
			}
			declaredVariables = Collections.unmodifiableSet(declaredVariables);
			declaredVariablesInEachDeclaration.put(pseudocode, declaredVariables);
		}
		return declaredVariables;
	}

	// variable initializers

	@NotNull
	public Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> getVariableInitializers()
	{
		if(variableInitializersMap == null)
		{
			variableInitializersMap = getVariableInitializers(pseudocode);
		}
		return variableInitializersMap;
	}

	@NotNull
	private Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> getVariableInitializers(@NotNull Pseudocode pseudocode)
	{

		Set<VariableDescriptor> usedVariables = getUsedVariables(pseudocode);
		Set<VariableDescriptor> declaredVariables = getDeclaredVariables(pseudocode);
		Map<VariableDescriptor, VariableInitState> initialMap = Collections.emptyMap();
		final Map<VariableDescriptor, VariableInitState> initialMapForStartInstruction = prepareInitializersMapForStartInstruction(usedVariables, declaredVariables);

		Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> variableInitializersMap = PseudocodeTraverser.collectData(pseudocode, /* directOrder = */ true, /* lookInside = */ false, initialMap, initialMapForStartInstruction, new PseudocodeTraverser.InstructionDataMergeStrategy<Map<VariableDescriptor, VariableInitState>>()
		{
			@Override
			public Edges<Map<VariableDescriptor, VariableInitState>> execute(@NotNull Instruction instruction, @NotNull Collection<Map<VariableDescriptor, VariableInitState>> incomingEdgesData)
			{

				Map<VariableDescriptor, VariableInitState> enterInstructionData = mergeIncomingEdgesDataForInitializers(incomingEdgesData);
				Map<VariableDescriptor, VariableInitState> exitInstructionData = addVariableInitStateFromCurrentInstructionIfAny(instruction, enterInstructionData);
				return Edges.create(enterInstructionData, exitInstructionData);
			}
		});


		for(LocalDeclarationInstruction localDeclarationInstruction : pseudocode.getLocalDeclarations())
		{
			Pseudocode localPseudocode = localDeclarationInstruction.getBody();
			Map<Instruction, Edges<Map<VariableDescriptor, VariableInitState>>> initializersForLocalDeclaration = getVariableInitializers(localPseudocode);

			for(Instruction instruction : initializersForLocalDeclaration.keySet())
			{
				//todo
				if(!variableInitializersMap.containsKey(instruction))
				{
					variableInitializersMap.put(instruction, initializersForLocalDeclaration.get(instruction));
				}
			}
			variableInitializersMap.putAll(initializersForLocalDeclaration);
		}
		return variableInitializersMap;
	}

	@NotNull
	private Map<VariableDescriptor, VariableInitState> prepareInitializersMapForStartInstruction(@NotNull Collection<VariableDescriptor> usedVariables, @NotNull Collection<VariableDescriptor> declaredVariables)
	{

		Map<VariableDescriptor, VariableInitState> initialMapForStartInstruction = Maps.newHashMap();
		VariableInitState initializedForExternalVariable = VariableInitState.create(true);
		VariableInitState notInitializedForDeclaredVariable = VariableInitState.create(false);

		for(VariableDescriptor variable : usedVariables)
		{
			if(declaredVariables.contains(variable))
			{
				initialMapForStartInstruction.put(variable, notInitializedForDeclaredVariable);
			}
			else
			{
				initialMapForStartInstruction.put(variable, initializedForExternalVariable);
			}
		}
		return initialMapForStartInstruction;
	}

	@NotNull
	private Map<VariableDescriptor, VariableInitState> mergeIncomingEdgesDataForInitializers(@NotNull Collection<Map<VariableDescriptor, VariableInitState>> incomingEdgesData)
	{

		Set<VariableDescriptor> variablesInScope = Sets.newHashSet();
		for(Map<VariableDescriptor, VariableInitState> edgeData : incomingEdgesData)
		{
			variablesInScope.addAll(edgeData.keySet());
		}

		Map<VariableDescriptor, VariableInitState> enterInstructionData = Maps.newHashMap();
		for(VariableDescriptor variable : variablesInScope)
		{
			Set<VariableInitState> edgesDataForVariable = Sets.newHashSet();
			for(Map<VariableDescriptor, VariableInitState> edgeData : incomingEdgesData)
			{
				VariableInitState initState = edgeData.get(variable);
				if(initState != null)
				{
					edgesDataForVariable.add(initState);
				}
			}
			enterInstructionData.put(variable, VariableInitState.create(edgesDataForVariable));
		}
		return enterInstructionData;
	}

	@NotNull
	private Map<VariableDescriptor, VariableInitState> addVariableInitStateFromCurrentInstructionIfAny(@NotNull Instruction instruction, @NotNull Map<VariableDescriptor, VariableInitState> enterInstructionData)
	{

		if(!(instruction instanceof WriteValueInstruction) && !(instruction instanceof VariableDeclarationInstruction))
		{
			return enterInstructionData;
		}
		VariableDescriptor variable = PseudocodeUtil.extractVariableDescriptorIfAny(instruction, false, bindingTrace);
		if(variable == null)
		{
			return enterInstructionData;
		}
		Map<VariableDescriptor, VariableInitState> exitInstructionData = Maps.newHashMap(enterInstructionData);
		if(instruction instanceof WriteValueInstruction)
		{
			VariableInitState enterInitState = enterInstructionData.get(variable);
			VariableInitState initializationAtThisElement = VariableInitState.create(((WriteValueInstruction) instruction).getElement() instanceof NapileVariable, enterInitState);
			exitInstructionData.put(variable, initializationAtThisElement);
		}
		else
		{ // instruction instanceof VariableDeclarationInstruction
			VariableInitState enterInitState = enterInstructionData.get(variable);
			if(enterInitState == null || !enterInitState.isInitialized || !enterInitState.isDeclared)
			{
				boolean isInitialized = enterInitState != null && enterInitState.isInitialized;
				VariableInitState variableDeclarationInfo = VariableInitState.create(isInitialized, true);
				exitInstructionData.put(variable, variableDeclarationInfo);
			}
		}
		return exitInstructionData;
	}

	// variable use

	@NotNull
	public Map<Instruction, Edges<Map<VariableDescriptor, VariableUseState>>> getVariableUseStatusData()
	{
		if(variableStatusMap == null)
		{
			Map<VariableDescriptor, VariableUseState> sinkInstructionData = Maps.newHashMap();
			for(VariableDescriptor usedVariable : usedVariablesInEachDeclaration.get(pseudocode))
			{
				sinkInstructionData.put(usedVariable, VariableUseState.UNUSED);
			}
			InstructionDataMergeStrategy<Map<VariableDescriptor, VariableUseState>> collectVariableUseStatusStrategy = new InstructionDataMergeStrategy<Map<VariableDescriptor, VariableUseState>>()
			{
				@Override
				public Edges<Map<VariableDescriptor, VariableUseState>> execute(@NotNull Instruction instruction, @NotNull Collection<Map<VariableDescriptor, VariableUseState>> incomingEdgesData)
				{

					Map<VariableDescriptor, VariableUseState> enterResult = Maps.newHashMap();
					for(Map<VariableDescriptor, VariableUseState> edgeData : incomingEdgesData)
					{
						for(Map.Entry<VariableDescriptor, VariableUseState> entry : edgeData.entrySet())
						{
							VariableDescriptor variableDescriptor = entry.getKey();
							VariableUseState variableUseState = entry.getValue();
							enterResult.put(variableDescriptor, variableUseState.merge(enterResult.get(variableDescriptor)));
						}
					}
					VariableDescriptor variableDescriptor = PseudocodeUtil.extractVariableDescriptorIfAny(instruction, true, bindingTrace);
					if(variableDescriptor == null || (!(instruction instanceof ReadValueInstruction) && !(instruction instanceof WriteValueInstruction)))
					{
						return Edges.create(enterResult, enterResult);
					}
					Map<VariableDescriptor, VariableUseState> exitResult = Maps.newHashMap(enterResult);
					if(instruction instanceof ReadValueInstruction)
					{
						exitResult.put(variableDescriptor, VariableUseState.LAST_READ);
					}
					else
					{ //instruction instanceof WriteValueInstruction
						VariableUseState variableUseState = enterResult.get(variableDescriptor);
						if(variableUseState == null)
						{
							variableUseState = VariableUseState.UNUSED;
						}
						switch(variableUseState)
						{
							case UNUSED:
							case ONLY_WRITTEN_NEVER_READ:
								exitResult.put(variableDescriptor, VariableUseState.ONLY_WRITTEN_NEVER_READ);
								break;
							case LAST_WRITTEN:
							case LAST_READ:
								exitResult.put(variableDescriptor, VariableUseState.LAST_WRITTEN);
						}
					}
					return Edges.create(enterResult, exitResult);
				}
			};
			variableStatusMap = PseudocodeTraverser.collectData(pseudocode, false, true, Collections.<VariableDescriptor, VariableUseState>emptyMap(), sinkInstructionData, collectVariableUseStatusStrategy);
		}
		return variableStatusMap;
	}

}
