package com.eviware.loadui.ui.fx.views.rename;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Label;

import com.eviware.loadui.api.traits.Labeled;
import com.eviware.loadui.ui.fx.control.ConfirmationDialog;
import com.eviware.loadui.ui.fx.control.fields.ValidatableStringField;

public class RenameDialog extends ConfirmationDialog
{
	public RenameDialog( final Labeled.Mutable labeled, Node owner )
	{
		super( owner, "Rename: " + labeled.getLabel(), "Rename" );

		Label newName = new Label( "New name" );
		final ValidatableStringField newNameField = new ValidatableStringField( ValidatableStringField.NOT_EMPTY );
		newNameField.setText( labeled.getLabel() );
		newNameField.selectAll();

		getItems().setAll( newName, newNameField );

		confirmDisableProperty().bind( Bindings.not( ( newNameField.isValidProperty() ) ) );

		setOnConfirm( new EventHandler<ActionEvent>()
		{
			@Override
			public void handle( ActionEvent event )
			{
				close();
				labeled.setLabel( newNameField.getValue() );
			}
		} );
	}
}
