package com.digitaslbi.apigee.view;

import javax.swing.Action;
import javax.swing.JPanel;

import lombok.Setter;

/**
 * A simple panel implementation class that holds input inside an action element
 * 
 * @author Victor Ortiz
 */
public class InputPanel extends JPanel {
	private static final long serialVersionUID = -2337376928008422799L;
	
	@Setter private Action action;
	
	public InputPanel( boolean isDoubleBuffered ) {
		super( isDoubleBuffered );
	}
	
	public String getInput() {
		return ( String ) action.getValue( null );
	}
}