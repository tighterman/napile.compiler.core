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

package org.napile.idea.plugin.psi.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import org.napile.asm.io.xml.in.AsmXmlFileReader;
import org.napile.asm.tree.members.ClassNode;
import org.napile.compiler.lang.psi.stubs.NapilePsiFileStub;
import org.napile.compiler.lang.psi.stubs.elements.NapileFileElementType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.stubs.BinaryFileStubBuilder;
import com.intellij.psi.stubs.Stub;
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @date 14:35/15.10.12
 */
public class NXmlFileStubBuilder implements BinaryFileStubBuilder
{
	@Override
	public boolean acceptsFile(VirtualFile file)
	{
		return false;
	}

	@Nullable
	@Override
	public Stub buildStubTree(VirtualFile virtualFile, byte[] content, Project project)
	{
		AsmXmlFileReader reader = new AsmXmlFileReader();

		try
		{
			ClassNode classNode = reader.read(new ByteArrayInputStream(content));

			NapilePsiFileStub psiFileStub = new NapilePsiFileStub(null, StringRef.fromString(classNode.name.parent().getFqName()), true);

			return psiFileStub;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int getStubVersion()
	{
		return NapileFileElementType.STUB_VERSION + 1;
	}
}
