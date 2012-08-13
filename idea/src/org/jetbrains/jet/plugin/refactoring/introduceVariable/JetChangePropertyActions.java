package org.jetbrains.jet.plugin.refactoring.introduceVariable;

import org.jetbrains.jet.lang.psi.JetProperty;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.project.Project;

/**
 * User: Alefas
 * Date: 14.02.12
 */
public class JetChangePropertyActions
{
	private JetChangePropertyActions()
	{
	}

	public static void declareValueOrVariable(Project project, boolean isVariable, JetProperty property)
	{
		ASTNode node;
		if(isVariable)
		{
			node = JetPsiFactory.createVarNode(project);
		}
		else
		{
			node = JetPsiFactory.createValNode(project);
		}
		property.getValOrVarNode().getPsi().replace(node.getPsi());
	}
}
