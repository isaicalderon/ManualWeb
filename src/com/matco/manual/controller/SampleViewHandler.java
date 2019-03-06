package com.matco.manual.controller;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;

/**
 *
 */
public class SampleViewHandler extends ViewHandlerWrapper {

    private ViewHandler handler;
    
    public SampleViewHandler(ViewHandler handler){
        super();
        this.handler = handler;
    }
    
    /* (non-Javadoc)
     * @see javax.faces.application.ViewHandlerWrapper#getWrapped()
     */
    @Override
    public ViewHandler getWrapped() {
        return handler;
    }

}