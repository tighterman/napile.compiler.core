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

package org.napile.compiler.lang.resolve.scopes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.FqName;
import org.napile.asm.resolve.name.Name;
import org.napile.compiler.lang.descriptors.ClassDescriptor;
import org.napile.compiler.lang.descriptors.ClassifierDescriptor;
import org.napile.compiler.lang.descriptors.MethodDescriptor;
import org.napile.compiler.lang.descriptors.NamespaceDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import com.google.common.collect.Sets;

/**
 * @author abreslav
 */
public abstract class WritableScopeWithImports extends JetScopeAdapter implements WritableScope
{

	@NotNull
	private final String debugName;

	@Nullable
	private List<JetScope> imports;
	private WritableScope currentIndividualImportScope;
	protected final RedeclarationHandler redeclarationHandler;

	public WritableScopeWithImports(@NotNull JetScope scope, @NotNull RedeclarationHandler redeclarationHandler, @NotNull String debugName)
	{
		super(scope);
		this.redeclarationHandler = redeclarationHandler;
		this.debugName = debugName;
	}


	private LockLevel lockLevel = LockLevel.WRITING;

	@Override
	public WritableScope changeLockLevel(LockLevel lockLevel)
	{
		if(lockLevel.ordinal() < this.lockLevel.ordinal())
		{
			throw new IllegalStateException("cannot lower lock level from " + this.lockLevel + " to " + lockLevel + " at " + toString());
		}
		this.lockLevel = lockLevel;
		return this;
	}

	protected void checkMayRead()
	{
		if(lockLevel != LockLevel.READING && lockLevel != LockLevel.BOTH)
		{
			throw new IllegalStateException("cannot read with lock level " + lockLevel + " at " + toString());
		}
	}

	protected void checkMayWrite()
	{
		if(lockLevel != LockLevel.WRITING && lockLevel != LockLevel.BOTH)
		{
			throw new IllegalStateException("cannot write with lock level " + lockLevel + " at " + toString());
		}
	}

	protected void checkMayNotWrite()
	{
		if(lockLevel == LockLevel.WRITING || lockLevel == LockLevel.BOTH)
		{
			throw new IllegalStateException("cannot write with lock level " + lockLevel + " at " + toString());
		}
	}


	@NotNull
	protected final List<JetScope> getImports()
	{
		if(imports == null)
		{
			imports = new ArrayList<JetScope>();
		}
		return imports;
	}

	@Override
	public void importScope(@NotNull JetScope imported)
	{
		if(imported == this)
		{
			throw new IllegalStateException("cannot import scope into self");
		}

		checkMayWrite();

		getImports().add(0, imported);
		currentIndividualImportScope = null;
	}

	@Override
	public void getImplicitReceiversHierarchy(@NotNull List<ReceiverDescriptor> result)
	{
		checkMayRead();

		super.getImplicitReceiversHierarchy(result);
		// Imported scopes come with their receivers
		// Example: class member resolution scope imports a scope of it's class object
		//          members of the class object must be able to find it as an implicit receiver
		for(JetScope scope : getImports())
		{
			ReceiverDescriptor definedReceiver = scope.getImplicitReceiver();
			if(definedReceiver.exists())
			{
				result.add(0, definedReceiver);
			}
		}
	}

	@NotNull
	@Override
	public Set<VariableDescriptor> getProperties(@NotNull Name name)
	{
		checkMayRead();

		Set<VariableDescriptor> properties = Sets.newLinkedHashSet();
		for(JetScope imported : getImports())
		{
			properties.addAll(imported.getProperties(name));
		}
		return properties;
	}

	@Override
	public VariableDescriptor getLocalVariable(@NotNull Name name)
	{
		checkMayRead();

		// Meaningful lookup goes here
		for(JetScope imported : getImports())
		{
			VariableDescriptor importedDescriptor = imported.getLocalVariable(name);
			if(importedDescriptor != null)
			{
				return importedDescriptor;
			}
		}
		return null;
	}

	@NotNull
	@Override
	public Collection<MethodDescriptor> getMethods(@NotNull Name name)
	{
		checkMayRead();

		if(getImports().isEmpty())
		{
			return Collections.emptySet();
		}
		Set<MethodDescriptor> result = Sets.newLinkedHashSet();
		for(JetScope imported : getImports())
		{
			result.addAll(imported.getMethods(name));
		}
		return result;
	}

	@Override
	public ClassifierDescriptor getClassifier(@NotNull Name name)
	{
		checkMayRead();

		for(JetScope imported : getImports())
		{
			ClassifierDescriptor importedClassifier = imported.getClassifier(name);
			if(importedClassifier != null)
			{
				return importedClassifier;
			}
		}
		return null;
	}

	@Override
	public ClassDescriptor getClass(@NotNull FqName name)
	{
		checkMayRead();

		for(JetScope imported : getImports())
		{
			ClassDescriptor importedClassifier = imported.getClass(name);
			if(importedClassifier != null)
				return importedClassifier;

		}
		return null;
	}

	@Override
	public ClassDescriptor getObjectDescriptor(@NotNull Name name)
	{
		checkMayRead();

		for(JetScope imported : getImports())
		{
			ClassDescriptor objectDescriptor = imported.getObjectDescriptor(name);
			if(objectDescriptor != null)
			{
				return objectDescriptor;
			}
		}
		return null;
	}

	@Override
	public NamespaceDescriptor getNamespace(@NotNull Name name)
	{
		checkMayRead();

		for(JetScope imported : getImports())
		{
			NamespaceDescriptor importedDescriptor = imported.getNamespace(name);
			if(importedDescriptor != null)
			{
				return importedDescriptor;
			}
		}
		return null;
	}

	private WritableScope getCurrentIndividualImportScope()
	{
		if(currentIndividualImportScope == null)
		{
			WritableScopeImpl writableScope = new WritableScopeImpl(EMPTY, getContainingDeclaration(), RedeclarationHandler.DO_NOTHING, "Individual import scope");
			writableScope.changeLockLevel(LockLevel.BOTH);
			importScope(writableScope);
			currentIndividualImportScope = writableScope;
		}
		return currentIndividualImportScope;
	}

	@Override
	public void importClassifierAlias(@NotNull Name importedClassifierName, @NotNull ClassifierDescriptor classifierDescriptor)
	{
		checkMayWrite();

		getCurrentIndividualImportScope().addClassifierAlias(importedClassifierName, classifierDescriptor);
	}


	@Override
	public void importNamespaceAlias(@NotNull Name aliasName, @NotNull NamespaceDescriptor namespaceDescriptor)
	{
		checkMayWrite();

		getCurrentIndividualImportScope().addNamespaceAlias(aliasName, namespaceDescriptor);
	}

	@Override
	public void importFunctionAlias(@NotNull Name aliasName, @NotNull MethodDescriptor methodDescriptor)
	{
		checkMayWrite();

		getCurrentIndividualImportScope().addFunctionAlias(aliasName, methodDescriptor);
	}

	@Override
	public void importVariableAlias(@NotNull Name aliasName, @NotNull VariableDescriptor variableDescriptor)
	{
		checkMayWrite();

		getCurrentIndividualImportScope().addVariableAlias(aliasName, variableDescriptor);
	}

	@Override
	public void clearImports()
	{
		currentIndividualImportScope = null;
		getImports().clear();
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() +
				"@" +
				Integer.toHexString(System.identityHashCode(this)) +
				" " +
				debugName +
				" for " +
				getContainingDeclaration();
	}
}
