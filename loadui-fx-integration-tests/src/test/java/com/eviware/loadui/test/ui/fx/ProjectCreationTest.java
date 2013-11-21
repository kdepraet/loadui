package com.eviware.loadui.test.ui.fx;

import com.eviware.loadui.api.events.EventFirer;
import com.eviware.loadui.api.model.ProjectRef;
import com.eviware.loadui.api.model.WorkspaceItem;
import com.eviware.loadui.test.TestState;
import com.eviware.loadui.test.categories.IntegrationTest;
import com.eviware.loadui.test.ui.fx.states.ProjectCreatedWithoutAgentsState;
import com.eviware.loadui.util.test.TestUtils;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.is;
import static org.loadui.testfx.Assertions.verifyThat;

/**
 * @author renato
 */
@Category( IntegrationTest.class )
public class ProjectCreationTest extends FxIntegrationTestBase
{

	private String nameOfCreatedProject;

	HasProjects hasProjects = new HasProjects();

	@After
	public void cleanup() throws Exception
	{
		if( nameOfCreatedProject != null )
		{
			try
			{
				ProjectRef ref = hasProjects.assertProjectExistsWithName( nameOfCreatedProject );
				ref.delete( true );
			}
			catch( Exception e )
			{
				System.out.println( getClass().getName() + ": Exception during cleanup -> " + e );
			}
		}
	}


	@Test
	public void shouldRenameProjectThoughMenuButton()
	{
		String newName = "Renamed Project";
		renameProjectThroughMenuButton( newName );
		hasProjects.assertProjectExistsWithName( newName );
	}

	@Test
	public void shouldRenameProjectThoughContextMenu()
	{
		String newName = "Another Project";
		renameProjectThroughContextMenu( newName );
		hasProjects.assertProjectExistsWithName( newName );
	}

	@Test
	public void shouldCloneProjectThroughMenuButton()
	{
		nameOfCreatedProject = "Cloned";
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		int projectCount = workspace.getProjectRefs().size();

		cloneProjectThroughMenuButton( nameOfCreatedProject );

		assertProjectCountIs( projectCount + 1 );
		hasProjects.assertProjectExistsWithName( nameOfCreatedProject );
	}

	@Test
	public void shouldCloneProjectThroughContextMenu()
	{
		nameOfCreatedProject = "Cloned2";
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		int projectCount = workspace.getProjectRefs().size();

		cloneProjectThroughContextMenu( nameOfCreatedProject );

		assertProjectCountIs( projectCount + 1 );
		hasProjects.assertProjectExistsWithName( nameOfCreatedProject );
	}

	@Test
	public void shouldCreateProjectThroughContextMenu()
	{
		nameOfCreatedProject = "Awesome Project";
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		int projectCount = workspace.getProjectRefs().size();

		createProjectThroughContextMenu( nameOfCreatedProject );

		sleep( 1500 ); // extra precaution due to assertion failed in jenkins

		assertProjectCountIs( projectCount + 1 );
		hasProjects.assertProjectExistsWithName( nameOfCreatedProject );
	}

	private void renameProjectThroughMenuButton( String newProjectName )
	{
		click( "#projectRefCarousel .project-ref-view #menuButton" ).click( "#rename-item" )
				.type( newProjectName ).type( KeyCode.ENTER );
		waitOnProjectCarouselEvents();
	}

	private void renameProjectThroughContextMenu( String newProjectName )
	{
		move( "#projectRefCarousel .project-ref-view" ).click( MouseButton.SECONDARY )
				.click( "#rename-item" ).type( newProjectName ).type( KeyCode.ENTER );
		waitOnProjectCarouselEvents();
	}

	private void cloneProjectThroughMenuButton( String name )
	{
		click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void cloneProjectThroughContextMenu( String name )
	{
		click( "#projectRefCarousel .project-ref-view .menu-button" ).click( "#clone-item" )
				.type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void createProjectThroughContextMenu( String name )
	{
		move( "#projectRefCarousel .prev" ).click( MouseButton.SECONDARY ).sleep( 500 )
				.click( "#create-item" ).type( name ).click( ".check-box" ).click( "#default" );
		waitOnProjectCarouselEvents();
	}

	private void assertProjectCountIs( int expectedCount )
	{
		WorkspaceItem workspace = FxIntegrationBase.getWorkspaceItem();
		verifyThat( workspace.getProjectRefs().size(), is( expectedCount ) );
	}

	private void waitOnProjectCarouselEvents()
	{
		try
		{
			TestUtils.awaitEvents( ( EventFirer )find( "#projectRefCarousel" ) );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public TestState getStartingState()
	{
		return ProjectCreatedWithoutAgentsState.STATE;
	}
}
