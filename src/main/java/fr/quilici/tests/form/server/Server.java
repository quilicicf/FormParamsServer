/**
 * Copyright 2010-2014 Restlet S.A.S. All rights reserved.
 * 
 * Restlet and APISpark are registered trademarks of Restlet S.A.S.
 */


package fr.quilici.tests.form.server;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.html.HtmlConverter;
import org.restlet.ext.jackson.JacksonConverter;

public class Server {

    public static void main(String[] args) throws Exception {
        Engine.getInstance().getRegisteredConverters().add(new JacksonConverter());
        Engine.getInstance().getRegisteredConverters().add(new HtmlConverter());
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 9003);

        c.getDefaultHost().attach("/v1", new FormServerApplication());
        c.start();
    }
}
