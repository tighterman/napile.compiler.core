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
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.psi.NapileClass;
import org.napile.compiler.lang.psi.NapileEnumEntry;
import org.napile.compiler.lang.psi.NapilePsiUtil;
import org.napile.compiler.lang.psi.stubs.PsiJetClassStub;
import org.napile.compiler.lang.psi.stubs.impl.PsiJetClassStubImpl;
import org.jetbrains.jet.lang.resolve.name.FqName;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;
import com.intellij.util.io.StringRef;

/**
 * @author Nikolay Krasko
 */
public class JetClassElementType extends JetStubElementType<PsiJetClassStub, NapileClass>
{

	public JetClassElementType(@NotNull @NonNls String debugName)
	{
		super(debugName);
	}

	@Override
	public NapileClass createPsi(@NotNull PsiJetClassStub stub)
	{
		return !stub.isEnumEntry() ? new NapileClass(stub) : new NapileEnumEntry(stub);
	}

	@Override
	public NapileClass createPsiFromAst(@NotNull ASTNode node)
	{
		return node.getElementType() != JetStubElementTypes.ENUM_ENTRY ? new NapileClass(node) : new NapileEnumEntry(node);
	}

	@Override
	public PsiJetClassStub createStub(@NotNull NapileClass psi, StubElement parentStub)
	{
		FqName fqName = NapilePsiUtil.getFQName(psi);
		boolean isEnumEntry = psi instanceof NapileEnumEntry;
		return new PsiJetClassStubImpl(getStubType(isEnumEntry), parentStub, fqName != null ? fqName.getFqName() : null, psi.getName(), psi.getSuperNames(), isEnumEntry);
	}

	@Override
	public void serialize(PsiJetClassStub stub, StubOutputStream dataStream) throws IOException
	{
		dataStream.writeName(stub.getName());
		dataStream.writeName(stub.getQualifiedName());
		dataStream.writeBoolean(stub.isEnumEntry());

		List<String> superNames = stub.getSuperNames();
		dataStream.writeVarInt(superNames.size());
		for(String name : superNames)
		{
			dataStream.writeName(name);
		}
	}

	@Override
	public PsiJetClassStub deserialize(StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		StringRef name = dataStream.readName();
		StringRef qualifiedName = dataStream.readName();
		boolean isEnumEntry = dataStream.readBoolean();

		int superCount = dataStream.readVarInt();
		StringRef[] superNames = StringRef.createArray(superCount);
		for(int i = 0; i < superCount; i++)
		{
			superNames[i] = dataStream.readName();
		}

		return new PsiJetClassStubImpl(getStubType(isEnumEntry), parentStub, qualifiedName, name, superNames, isEnumEntry);
	}

	@Override
	public void indexStub(PsiJetClassStub stub, IndexSink sink)
	{
		StubIndexServiceFactory.getInstance().indexClass(stub, sink);
	}

	private static JetClassElementType getStubType(boolean isEnumEntry)
	{
		return isEnumEntry ? JetStubElementTypes.ENUM_ENTRY : JetStubElementTypes.CLASS;
	}
}