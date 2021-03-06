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

package org.napile.compiler.lang.psi.stubs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.napile.compiler.lang.psi.NapileNamedMacro;
import org.napile.compiler.lang.psi.stubs.elements.NapileStubElementTypes;
import com.intellij.psi.stubs.NamedStub;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.io.StringRef;

/**
 * @author Nikolay Krasko
 */
public class NapilePsiMacroStub extends StubBase<NapileNamedMacro> implements NamedStub<NapileNamedMacro>
{
	private final StringRef nameRef;

	public NapilePsiMacroStub(@NotNull StubElement parent, @Nullable String name)
	{
		this(parent, StringRef.fromString(name));
	}

	public NapilePsiMacroStub(@NotNull StubElement parent, @Nullable StringRef nameRef)
	{
		super(parent, NapileStubElementTypes.MACRO);

		this.nameRef = nameRef;
	}

	@Override
	public String getName()
	{
		return StringRef.toString(nameRef);
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("NapilePsiMacroStub[");

		builder.append("name=").append(getName());

		builder.append("]");

		return builder.toString();
	}
}
