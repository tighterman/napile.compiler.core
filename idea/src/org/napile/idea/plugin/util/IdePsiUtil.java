package org.napile.idea.plugin.util;

import org.jetbrains.annotations.NotNull;
import org.napile.asm.lib.NapileAnnotationPackage;
import org.napile.compiler.lang.descriptors.DeclarationDescriptor;
import org.napile.compiler.lang.descriptors.MethodDescriptor;
import org.napile.compiler.lang.descriptors.MutableClassDescriptor;
import org.napile.compiler.lang.psi.NapileClassLike;
import org.napile.compiler.lang.psi.NapileDeclaration;
import org.napile.compiler.lang.resolve.AnnotationUtils;
import org.napile.compiler.util.PluginKeys;
import org.napile.compiler.util.RunUtil;

/**
 * @author VISTALL
 * @date 14:23/08.10.12
 */
public class IdePsiUtil extends RunUtil
{
	public static boolean isDeprecated(@NotNull NapileDeclaration declaration)
	{
		DeclarationDescriptor descriptor = declaration.getUserData(PluginKeys.DESCRIPTOR_KEY);
		if(descriptor == null)
			return false;
		return AnnotationUtils.hasAnnotation(descriptor, NapileAnnotationPackage.DEPRECATED);
	}

	public static boolean hasRunMethod(@NotNull NapileClassLike classLike)
	{
		MutableClassDescriptor descriptor = (MutableClassDescriptor) classLike.getUserData(PluginKeys.DESCRIPTOR_KEY);
		if(descriptor == null)
			return false;

		for(MethodDescriptor methodDescriptor : descriptor.getMethods())
			if(isRunPoint(methodDescriptor))
				return true;
		return false;
	}
}
