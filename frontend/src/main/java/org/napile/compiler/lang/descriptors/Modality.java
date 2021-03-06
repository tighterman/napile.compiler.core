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

package org.napile.compiler.lang.descriptors;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.lexer.NapileTokens;
import org.napile.compiler.lang.psi.NapileClass;
import org.napile.compiler.lang.psi.NapileDeclarationWithBody;
import org.napile.compiler.lang.psi.NapileModifierListOwner;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author abreslav
 */
public enum Modality
{
	// THE ORDER OF ENTRIES MATTERS HERE
	FINAL,
	OPEN,
	ABSTRACT;

	@NotNull
	public static Modality resolve(@NotNull NapileModifierListOwner modifierListOwner)
	{
		if(modifierListOwner.hasModifier(NapileTokens.ABSTRACT_KEYWORD))
			return ABSTRACT;
		if(modifierListOwner.hasModifier(NapileTokens.FINAL_KEYWORD))
			return FINAL;

		if(modifierListOwner instanceof NapileDeclarationWithBody)
		{
			NapileClass napileClass = PsiTreeUtil.getParentOfType(modifierListOwner, NapileClass.class);
			if(napileClass != null &&  napileClass.hasModifier(NapileTokens.ABSTRACT_KEYWORD) && ((NapileDeclarationWithBody) modifierListOwner).getBodyExpression() == null)
			{
				if(!modifierListOwner.hasModifier(NapileTokens.NATIVE_KEYWORD))
				{
					return ABSTRACT;
				}
			}
		}
		return OPEN;
	}

	public boolean isOverridable()
	{
		return this != FINAL;
	}
}
