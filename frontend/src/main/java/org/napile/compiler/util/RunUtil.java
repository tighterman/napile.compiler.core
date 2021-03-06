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

package org.napile.compiler.util;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.lib.NapileLangPackage;
import org.napile.compiler.lang.descriptors.CallParameterDescriptor;
import org.napile.compiler.lang.descriptors.MethodDescriptor;
import org.napile.compiler.lang.types.NapileType;
import org.napile.compiler.lang.types.TypeUtils;

/**
 * @author VISTALL
 * @since 3:39/20.09.12
 */
public class RunUtil
{
	public static boolean isRunPoint(@NotNull MethodDescriptor methodDescriptor)
	{
		if(!methodDescriptor.isStatic())
			return false;
		if(!TypeUtils.isEqualFqName(methodDescriptor.getReturnType(), NapileLangPackage.NULL))
			return false;
		if(methodDescriptor.getValueParameters().size() != 1)
			return false;
		if(!methodDescriptor.getName().getName().equals("main"))
			return false;

		CallParameterDescriptor parameterDescriptor = methodDescriptor.getValueParameters().get(0);

		NapileType napileType = parameterDescriptor.getReturnType();

		if(!TypeUtils.isEqualFqName(napileType, NapileLangPackage.ARRAY))
			return false;

		if(napileType.getArguments().size() != 1)
			return false;

		NapileType arg = napileType.getArguments().get(0);
		if(!TypeUtils.isEqualFqName(arg, NapileLangPackage.STRING))
			return false;

		return true;
	}
}
