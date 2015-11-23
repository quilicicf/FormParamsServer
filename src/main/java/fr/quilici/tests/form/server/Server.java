/**
 * Copyright 2010-2014 Restlet S.A.S. All rights reserved.
 * 
 * Restlet and APISpark are registered trademarks of Restlet S.A.S.
 */


package fr.quilici.tests.form.server;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static org.restlet.engine.Engine.setLogLevel;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;

import org.restlet.Component;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.html.HtmlConverter;
import org.restlet.ext.jackson.JacksonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    private static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws Exception {
        int port = 9003;

        for (int i = 0; i < args.length; i++) {
            if ("-p".equals(args[i])) {
                port = parseInt(args[++i]);
            }
        }

        Engine.getInstance().getRegisteredConverters().add(new JacksonConverter());
        Engine.getInstance().getRegisteredConverters().add(new HtmlConverter());
        setLogLevel(Level.WARNING);
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, port);

        c.getDefaultHost().attach("/v1", new FormServerApplication());
        c.start();

        LOGGER.info(format("Server started and listening to: http://%s:%s/v1/*", getLocalIPAddress(), port));
        LOGGER.info("Set the parameter -p to change the port.");
    }

    private static String getLocalIPAddress() throws UnknownHostException, IOException {
        Socket s = new Socket("google.com", 80);
        String host = s.getLocalAddress().getHostAddress();
        s.close();

        return host;
    }
}
