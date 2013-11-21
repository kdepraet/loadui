package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * @author renato
 */
public class HasProjects
{

	public ProjectRef assertProjectExistsWithName( final String expectedName )
	{
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		return Iterables.find( workspace.getProjectRefs(), new Predicate<ProjectRef>()
		{
			@Override
			public boolean apply( ProjectRef input )
			{
				return input.getLabel().equals( expectedName );
			}
		} );
	}

}
