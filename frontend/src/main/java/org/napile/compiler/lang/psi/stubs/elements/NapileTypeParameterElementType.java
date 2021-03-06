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

package org.napile.compiler.lang.psi.stubs.elements;

import java.io.IOException;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileTypeParameter;
import org.napile.compiler.lang.psi.impl.NapileTypeParameterImpl;
import org.napile.compiler.lang.psi.stubs.NapilePsiTypeParameterStub;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author Nikolay Krasko
 */
public class NapileTypeParameterElementType extends NapileStubElementType<NapilePsiTypeParameterStub, NapileTypeParameter>
{
	public NapileTypeParameterElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@Override
	public NapileTypeParameter createPsiFromAst(@NotNull ASTNode node)
	{
		return new NapileTypeParameterImpl(node);
	}

	@Override
	public NapileTypeParameter createPsi(@NotNull NapilePsiTypeParameterStub stub)
	{
		return getPsiFactory(stub).createTypeParameter(stub);
	}

	@Override
	public NapilePsiTypeParameterStub createStub(@NotNull NapileTypeParameter psi, StubElement parentStub)
	{
		return new NapilePsiTypeParameterStub(parentStub, psi.getName());
	}

	@Override
	public void serialize(NapilePsiTypeParameterStub stub, StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getName());
	}

	@Override
	public NapilePsiTypeParameterStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new NapilePsiTypeParameterStub(parentStub, dataStream.readName());
	}

	@Override
	public void indexStub(NapilePsiTypeParameterStub stub, IndexSink sink)
	{
		// No index
	}
}
