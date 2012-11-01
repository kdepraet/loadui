package com.eviware.loadui.ui.fx.util;

import java.util.List;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

public final class NodeUtils
{
	public static Rectangle2D localToScreen( Node node, Scene scene )
	{
		Bounds selectableBounds = node.localToScene( node.getBoundsInLocal() );

		return new Rectangle2D( selectableBounds.getMinX() + scene.getX() + scene.getWindow().getX(),
				selectableBounds.getMinY() + scene.getY() + scene.getWindow().getY(), node.getBoundsInLocal().getWidth(),
				node.getBoundsInLocal().getHeight() );
	}

	public static Node findFrontNodeAtCoordinate( Node root, Point2D point, Node... ignored )
	{
		if( !root.contains( root.sceneToLocal( point ) ) )
		{
			return null;
		}

		for( Node ignore : ignored )
		{
			if( isDescendant( ignore, root ) )
			{
				return null;
			}
		}

		if( root instanceof Parent )
		{
			List<Node> children = ( ( Parent )root ).getChildrenUnmodifiable();
			for( int i = children.size() - 1; i >= 0; i-- )
			{
				Node result = findFrontNodeAtCoordinate( children.get( i ), point, ignored );
				if( result != null )
				{
					return result;
				}
			}
		}

		return root;
	}

	public static boolean isDescendant( Node ancestor, Node descendant )
	{
		Node current = descendant;
		while( current != null )
		{
			if( current == ancestor )
			{
				return true;
			}

			current = current.getParent();
		}

		return false;
	}

}