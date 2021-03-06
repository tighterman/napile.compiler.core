package org.napile.compiler.injection.protobuf.lang.psi.impl.declaration;

import org.jetbrains.annotations.NotNull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import org.napile.compiler.injection.protobuf.lang.psi.PbPsiElementVisitor;
import org.napile.compiler.injection.protobuf.lang.psi.api.declaration.PbMessageDef;
import org.napile.compiler.injection.protobuf.lang.psi.impl.auxiliary.PbNamedBlockHolderImpl;
import org.napile.compiler.injection.protobuf.lang.psi.utils.PbPsiUtil;

/**
 * @author Nikolay Matveev
 */

public class PbMessageDefImpl extends PbNamedBlockHolderImpl implements PbMessageDef
{

	public PbMessageDefImpl(ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull PbPsiElementVisitor visitor)
	{
		visitor.visitMessageDefinition(this);
	}

	@Override
	public PsiElement getNameElement()
	{
		return PbPsiUtil.getChild(this, 1, true, true, false);
	}
}
