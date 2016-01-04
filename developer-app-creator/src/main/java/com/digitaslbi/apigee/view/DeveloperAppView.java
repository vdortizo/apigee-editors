package com.digitaslbi.apigee.view;

import java.nio.file.Path;

import javax.swing.event.UndoableEditListener;

public interface DeveloperAppView extends Runnable, UndoableEditListener {
    public void initialize( boolean firstTime );
    
    public Path getFileOrDirectory( boolean directoyOnly );
    
    public Path saveFileOrDirectory( boolean directoyOnly );
    
    public boolean showMessage( String message, boolean isError, boolean isYesNo );
    
    public String requestNewInput( String message );
}