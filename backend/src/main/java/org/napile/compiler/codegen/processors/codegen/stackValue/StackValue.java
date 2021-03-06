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

package org.napile.compiler.codegen.processors.codegen.stackValue;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.resolve.name.FqName;
import org.napile.asm.tree.members.ClassNode;
import org.napile.asm.tree.members.bytecode.adapter.InstructionAdapter;
import org.napile.asm.tree.members.types.TypeNode;
import org.napile.compiler.codegen.processors.ExpressionCodegen;
import org.napile.compiler.codegen.processors.FqNameGenerator;
import org.napile.compiler.codegen.processors.PositionMarker;
import org.napile.compiler.codegen.processors.TypeTransformer;
import org.napile.compiler.codegen.processors.adapters.MarkerInstructionAdapter;
import org.napile.compiler.codegen.processors.codegen.CallableMethod;
import org.napile.compiler.lang.descriptors.CallableDescriptor;
import org.napile.compiler.lang.descriptors.ClassDescriptor;
import org.napile.compiler.lang.descriptors.MethodDescriptor;
import org.napile.compiler.lang.descriptors.MultiTypeEntryVariableDescriptor;
import org.napile.compiler.lang.descriptors.VariableDescriptor;
import org.napile.compiler.lang.resolve.BindingTrace;
import org.napile.compiler.lang.resolve.calls.ResolvedCall;
import org.napile.compiler.lang.resolve.scopes.receivers.ReceiverDescriptor;
import com.intellij.psi.PsiElement;

/**
 * @author yole
 * @author alex.tkachman
 */
public abstract class StackValue
{
	public static StackValue none()
	{
		return None.INSTANCE;
	}

	public static Local local(PsiElement target, int index, TypeNode type)
	{
		return new Local(target, index, type);
	}

	public static Constant constant(PsiElement target, Object value, TypeNode typeNode)
	{
		return new Constant(target, value, typeNode);
	}

	public static StackValue receiver(ResolvedCall<? extends CallableDescriptor> resolvedCall, StackValue receiver, ExpressionCodegen codegen, CallableMethod callableMethod)
	{
		return receiver(resolvedCall.getResultingDescriptor(), resolvedCall.getThisObject(), receiver, codegen, callableMethod);
	}

	public static StackValue receiver(CallableDescriptor callableDescriptor, ReceiverDescriptor receiverDescriptor, StackValue receiver, ExpressionCodegen codegen, CallableMethod callableMethod)
	{
		if(receiverDescriptor.exists())
			return new CallReceiver(callableDescriptor, receiverDescriptor, receiver, codegen, callableMethod);

		return receiver;
	}

	public static StackValue thisOrOuter(ExpressionCodegen codegen, ClassDescriptor descriptor, boolean isSuper)
	{
		return new ThisOuter(codegen, descriptor, isSuper);
	}

	public static StackValue onStack(TypeNode type)
	{
		return new OnStack(type);
	}

	@NotNull
	public static StackValue variable(@NotNull PsiElement psiElement, @NotNull BindingTrace bindingTrace, @NotNull ClassNode classNode, @NotNull VariableDescriptor propertyDescriptor)
	{
		return new Variable(psiElement, FqNameGenerator.getFqName(propertyDescriptor, bindingTrace), TypeTransformer.toAsmType(bindingTrace, propertyDescriptor.getType(), classNode), propertyDescriptor.isStatic());
	}

	@NotNull
	public static StackValue variable(@NotNull PsiElement psiElement, @NotNull FqName fqName, @NotNull TypeNode type, boolean staticVar)
	{
		return new Variable(psiElement, fqName, type, staticVar);
	}

	@NotNull
	public static StackValue variableAccessor(@NotNull MethodDescriptor methodDescriptor, @NotNull TypeNode typeNode, @NotNull ExpressionCodegen gen, boolean nullable, PsiElement target)
	{
		return variableAccessor(target, methodDescriptor, typeNode, gen.bindingTrace, gen.classNode, nullable);
	}

	@NotNull
	public static StackValue variableAccessor(PsiElement target, @NotNull MethodDescriptor methodDescriptor, @NotNull TypeNode typeNode, @NotNull BindingTrace bindingTrace, @NotNull ClassNode classNode, boolean nullable)
	{
		return new VariableAccessor(typeNode, target, methodDescriptor, bindingTrace, classNode, nullable);
	}

	@NotNull
	public static StackValue simpleVariableAccessor(@NotNull ExpressionCodegen gen, @NotNull VariableDescriptor variableDescriptor, CallableMethod.CallType callType)
	{
		return simpleVariableAccessor(FqNameGenerator.getFqName(variableDescriptor, gen.bindingTrace), gen.toAsmType(variableDescriptor.getType()), callType);
	}

	@NotNull
	public static StackValue multiVariable(@NotNull ExpressionCodegen gen, @NotNull MultiTypeEntryVariableDescriptor variable)
	{
		return new MultiVariable(gen.toAsmType(variable.getType()), variable.getIndex());
	}

	@NotNull
	public static StackValue simpleVariableAccessor(@NotNull FqName fqName, @NotNull TypeNode type, CallableMethod.CallType callType)
	{
		return new SimpleVariableAccessor(fqName, type, callType);
	}

	public static StackValue collectionElement(@NotNull TypeNode typeNode, ResolvedCall<MethodDescriptor> getCall, ResolvedCall<MethodDescriptor> setCall, ExpressionCodegen expressionCodegen)
	{
		return new CollectionElement(typeNode, getCall, setCall, expressionCodegen);
	}


	private final TypeNode type;
	protected final PsiElement target;

	protected StackValue(PsiElement target, TypeNode type)
	{
		this.target = target;
		this.type = type;
	}

	public abstract void put(TypeNode type, InstructionAdapter instructionAdapter, PositionMarker positionMarker);

	/**
	 * This method is called to put the value on the top of the JVM stack if <code>depth</code> other values have been put on the
	 * JVM stack after this value was generated.
	 *
	 * @param type  the type as which the value should be put
	 * @param v     the visitor used to generate the instructions
	 * @param depth the number of new values put onto the stack
	 */
	protected void moveToTopOfStack(TypeNode type, InstructionAdapter v, int depth)
	{
		put(type, v, PositionMarker.EMPTY);
	}

	public void store(TypeNode topOfStackType, InstructionAdapter instructionAdapter, PositionMarker positionMarker)
	{
		throw new UnsupportedOperationException("cannot store to value " + this);
	}

	public int receiverSize()
	{
		return 0;
	}

	public void dupReceiver(InstructionAdapter v)
	{
	}

	protected InstructionAdapter join(final InstructionAdapter adapter, final PositionMarker marker)
	{
		if(target == null || marker == PositionMarker.EMPTY)
			return adapter;

		return new MarkerInstructionAdapter(adapter, target, marker);
	}

	protected void castTo(TypeNode toType, InstructionAdapter v)
	{
		castTo(this.type, toType, v);
	}

	public static void castTo(TypeNode fromType, TypeNode toType, InstructionAdapter v)
	{

	}

	public TypeNode getType()
	{
		return type;
	}
}
