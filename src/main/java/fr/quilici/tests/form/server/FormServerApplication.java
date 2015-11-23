/**
 * Copyright 2010-2014 Restlet S.A.S. All rights reserved.
 * 
 * Restlet and APISpark are registered trademarks of Restlet S.A.S.
 */


package fr.quilici.tests.form.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class FormServerApplication extends Application {


    @Override
    public Restlet createInboundRoot() {
        Router router = new Router();
        router.attachDefault(PersonServerResource.class);
        return router;
    }
}
