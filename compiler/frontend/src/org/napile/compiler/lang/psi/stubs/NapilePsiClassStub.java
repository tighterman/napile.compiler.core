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

package org.napile.compiler.lang.psi.stubs;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.descriptors.ClassKind;
import org.napile.compiler.lang.psi.NapileClass;
import org.napile.compiler.lang.psi.stubs.elements.NapileStubElementTypes;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.io.StringRef;

/**
 * @author Nikolay Krasko
 */
public class NapilePsiClassStub extends StubBase<NapileClass> implements NamedStub<NapileClass>
{
	private final StringRef qualifiedName;
	private final StringRef name;
	private final StringRef[] superNames;
	private final ClassKind classKind;

	public NapilePsiClassStub(StubElement parent, @Nullable final String qualifiedName, String name, List<String> superNames, ClassKind classKind)
	{
		this(parent, StringRef.fromString(qualifiedName), StringRef.fromString(name), wrapStrings(superNames), classKind);
	}

	public NapilePsiClassStub(StubElement parent, StringRef qualifiedName, StringRef name, StringRef[] superNames, ClassKind classKind)
	{
		super(parent, NapileStubElementTypes.CLASS);
		this.qualifiedName = qualifiedName;
		this.name = name;
		this.superNames = superNames;
		this.classKind = classKind;
	}

	private static StringRef[] wrapStrings(List<String> names)
	{
		StringRef[] refs = new StringRef[names.size()];
		for(int i = 0; i < names.size(); i++)
		{
			refs[i] = StringRef.fromString(names.get(i));
		}
		return refs;
	}

	public String getQualifiedName()
	{
		return StringRef.toString(qualifiedName);
	}

	public ClassKind getKind()
	{
		return classKind;
	}

	@Override
	public String getName()
	{
		return StringRef.toString(name);
	}

	@NotNull
	public List<String> getSuperNames()
	{
		List<String> result = new ArrayList<String>();
		for(StringRef ref : superNames)
		{
			result.add(ref.toString());
		}
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("NapilePsiClassStub[");

		builder.append("name=").append(getName());
		builder.append(" fqn=").append(getQualifiedName());
		builder.append(" classKind=").append(getKind());
		builder.append(" superNames=").append("[").append(StringUtil.join(ArrayUtil.toStringArray(getSuperNames()))).append("]");

		builder.append("]");

		return builder.toString();
	}
}