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

package org.napile.compiler.lang.resolve;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.diagnostics.Diagnostic;
import org.napile.compiler.util.slicedmap.MutableSlicedMap;
import org.napile.compiler.util.slicedmap.ReadOnlySlice;
import org.napile.compiler.util.slicedmap.SlicedMapImpl;
import org.napile.compiler.util.slicedmap.WritableSlice;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiCompiledElement;

/**
 * @author abreslav
 */
public class BindingTraceImpl implements BindingTrace
{
	private final List<Diagnostic> diagnostics = Lists.newArrayList();

	private final MutableSlicedMap map = SlicedMapImpl.create();

	@Override
	public void report(@NotNull Diagnostic diagnostic)
	{
		if(diagnostic.getPsiElement() instanceof PsiCompiledElement)
			return;

		diagnostics.add(diagnostic);
	}

	@NotNull
	@Override
	public List<Diagnostic> getDiagnostics()
	{
		return diagnostics;
	}

	public void clearDiagnostics()
	{
		diagnostics.clear();
	}

	@Nullable
	@Override
	public BindingTrace getParent()
	{
		return null;
	}

	@Override
	public <K, V> void record(WritableSlice<K, V> slice, K key, V value)
	{
		map.put(slice, key, value);
	}

	@Override
	public <K> void record(WritableSlice<K, Boolean> slice, K key)
	{
		record(slice, key, true);
	}

	@Override
	public <K, V> V get(ReadOnlySlice<K, V> slice, K key)
	{
		return map.get(slice, key);
	}

	@Override
	@NotNull
	public <K, V> V safeGet(ReadOnlySlice<K, V> slice, K key)
	{
		return get(slice, key);
	}

	@NotNull
	@Override
	public <K, V> Collection<K> getKeys(WritableSlice<K, V> slice)
	{
		return map.getKeys(slice);
	}
}
