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

/*
 * @author max
 */
package org.napile.compiler;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.napile.compiler.lang.NapileLanguage;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.NotNullLazyValue;

public class NapileFileType extends LanguageFileType
{
	public static final NapileFileType INSTANCE = new NapileFileType();
	private final NotNullLazyValue<Icon> myIcon = new NotNullLazyValue<Icon>()
	{
		@NotNull
		@Override
		protected Icon compute()
		{
			return IconLoader.getIcon("/org/napile/icons/fileTypes/napile.png");
		}
	};

	private NapileFileType()
	{
		super(NapileLanguage.INSTANCE);
	}

	@Override
	@NotNull
	public String getName()
	{
		return NapileLanguage.NAME;
	}

	@Override
	@NotNull
	public String getDescription()
	{
		return "Napile source file";
	}

	@Override
	@NotNull
	public String getDefaultExtension()
	{
		return "ns";
	}

	@Override
	public Icon getIcon()
	{
		return myIcon.getValue();
	}
}
