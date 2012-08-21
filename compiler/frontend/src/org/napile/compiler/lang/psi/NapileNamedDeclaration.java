package org.napile.compiler.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.jet.lang.resolve.name.Name;
import com.intellij.psi.PsiNameIdentifierOwner;

/**
 * @author Nikolay Krasko
 */
public interface NapileNamedDeclaration extends NapileDeclaration, PsiNameIdentifierOwner, NapileStatementExpression, NapileNamed
{
	@NotNull
	Name getNameAsSafeName();
}