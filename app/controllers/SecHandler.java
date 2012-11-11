package controllers;

import org.joda.time.DateTime;

import play.*;
import play.mvc.*;
import play.mvc.Http.*;
/**
 * handles the sessions
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */
public class SecHandler extends Security.Authenticator{

    @Override
    public String getUsername(Context ctx) {
    		  
    		  return ctx.session().get("connected");

    }

    @Override
    public Result onUnauthorized(Context ctx) {
        return redirect(routes.Application.loginForm());
    }
}
