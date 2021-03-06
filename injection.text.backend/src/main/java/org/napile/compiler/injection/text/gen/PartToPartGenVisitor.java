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

package org.napile.compiler.injection.text.gen;

import java.util.Arrays;
import java.util.Collections;

import org.napile.asm.AsmConstants;
import org.napile.asm.lib.NapileLangPackage;
import org.napile.asm.resolve.name.Name;
import org.napile.asm.tree.members.MethodParameterNode;
import org.napile.asm.tree.members.bytecode.MethodRef;
import org.napile.asm.tree.members.bytecode.adapter.InstructionAdapter;
import org.napile.asm.tree.members.types.TypeNode;
import org.napile.compiler.codegen.processors.AsmNodeUtil;
import org.napile.compiler.codegen.processors.ExpressionCodegen;
import org.napile.compiler.codegen.processors.codegen.TypeConstants;
import org.napile.compiler.injection.text.lang.lexer.TextTokens;
import org.napile.compiler.lang.psi.NapileExpression;
import org.napile.compiler.lang.psi.NapileVisitorVoid;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19:08/12.11.12
 */
public class PartToPartGenVisitor extends NapileVisitorVoid
{
	private static final MethodRef PLUS_REF = new MethodRef(NapileLangPackage.STRING_BUILDER.child(Name.identifier("plus")), Arrays.asList(AsmNodeUtil.parameterNode("element", TypeConstants.ANY_NULLABLE)), Collections.<TypeNode>emptyList(),  TypeConstants.STRING_BUILDER);
	private static final MethodRef TO_STRING_REF = new MethodRef(NapileLangPackage.ANY.child(Name.identifier("toString")), Collections.<MethodParameterNode>emptyList(), Collections.<TypeNode>emptyList(), AsmConstants.STRING_TYPE);

	private StringBuilder builder = null;

	private final ExpressionCodegen generator;
	private final InstructionAdapter adapter;

	//private final ReservedInstruction countInstruction;

	public PartToPartGenVisitor(InstructionAdapter adapter, ExpressionCodegen generator)
	{
		this.adapter = adapter;
		this.generator = generator;

		//countInstruction = adapter.reserve();

		//TODO [VISTALL] change constructor call
		adapter.newObject(TypeConstants.STRING_BUILDER, Arrays.<MethodParameterNode>asList(/*TODO size*/));
	}

	@Override
	public void visitElement(PsiElement element)
	{
		if(element.getNode().getElementType() == TextTokens.TEXT_PART)
		{
			if(builder == null)
				builder = new StringBuilder();

			builder.append(element.getText());
		}
		else
			element.acceptChildren(this);  //FIXME [VISTALL] need this?
	}

	@Override
	public void visitExpression(NapileExpression expression)
	{
		appendLastString();

		generator.gen(expression, AsmConstants.ANY_TYPE);

		adapter.invokeVirtual(PLUS_REF, false);
	}

	private void appendLastString()
	{
		if(builder != null)
		{
			adapter.newString(builder.toString());
			builder = null;

			adapter.invokeVirtual(PLUS_REF, false);
		}
	}

	public void genToString()
	{
		appendLastString();

		adapter.invokeVirtual(TO_STRING_REF, false);
	}
}
