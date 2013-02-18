package controllers;

import java.util.Arrays;
import java.util.List;

import models.AdmFrmDat;
import models.Jamesconn;
import models.MbFrmDat;
import models.User;
import play.Play;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.index;
import views.html.usrAdminF;

/**
 * Handles all Actions for the Admin Section
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */
@Security.Authenticated(SecHandler.class)
public class AdminHandler extends Controller{
	private static String host = Play.application().configuration().getString("jserv.host");
	private static String port = Play.application().configuration().getString("jserv.port");
	private static Jamesconn jmc = new Jamesconn(host, port);
	static Form<User> userAdm = Form.form(User.class);
	static Form<AdmFrmDat> domainFrm = Form.form(AdmFrmDat.class);
	//TODO ADMFRMDAT anpassen auf alle daten der Seite
	  // ---------------------Functions for the Admin-Section ---------------------
	  //TODO: implement some useful adminfunctions :)
	  
	  // Shows all Users - Admin-Section
	  public static Result showUsers(){ //TODO Handle Account-Generation
		  if( ( session().containsKey("adm") ) ){
			  List<String> lst = Arrays.asList(jmc.getDomainList());
			  return ok( usrAdminF.render("", User.all(), lst, userAdm, domainFrm) );
			  	}else return badRequest( index.render("not authorized!") ); 	  	
	  }
	  
	  // promotes the User - Admin-Section 
	  public static Result promoteUser(Long id){
		  
		  User.promote(id);
		  return redirect(routes.AdminHandler.showUsers());
	  }
	  
	  /**
	   * Handles the user delete function
	   * @param id
	   * @return
	   */
	  public static Result deleteUser(Long id){
		  User.delete(id);
		  return redirect(routes.AdminHandler.showUsers());
	  }
	  public static Result addDomain(){
		  Form<AdmFrmDat> filledForm = domainFrm.bindFromRequest();
		  AdmFrmDat afd = filledForm.get();		  
		  jmc.addDomain( afd.getDomain() );
		  List<String> lst = Arrays.asList( jmc.getDomainList() );
		  return ok( usrAdminF.render(Messages.get("adm.domadded"), User.all(), lst, userAdm, domainFrm)  ); 
	  }
	  public static Result deleteDomain(){
		  Form<AdmFrmDat> filledForm = domainFrm.bindFromRequest();
		  AdmFrmDat afd = filledForm.get();		  
		  jmc.removeDomain( afd.getDomain() );
		  List<String> lst = Arrays.asList( jmc.getDomainList() );
		  return ok( usrAdminF.render(Messages.get("adm.domdeleted"), User.all(), lst, userAdm, domainFrm)  ); 
	  }
}


