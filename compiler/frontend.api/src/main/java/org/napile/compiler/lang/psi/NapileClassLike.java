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

package org.napile.compiler.lang.psi;

import org.jetbrains.annotations.Nullable;
import org.napile.asm.resolve.name.FqName;
import org.napile.asm.resolve.name.Name;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author max
 */
public interface NapileClassLike extends PsiNameIdentifierOwner, NapileDeclarationContainer<NapileDeclaration>, NapileModifierListOwner, NapileElement, NapileSuperListOwner
{
	NapileClassLike[] EMPTY_ARRAY = new NapileClassLike[0];

	@Nullable
	Name getNameAsName();

	FqName getFqName();

	@Nullable
	NapileClassBody getBody();
}
