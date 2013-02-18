package controllers;

import java.util.HashMap;

import models.EditFrmDat;
import models.User;
import play.*;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.*;

import views.html.*;

/**
 * Handles the actions of the user-object
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */

@Security.Authenticated(SecHandler.class)
public class UserHandler extends Controller {
	static Form<EditFrmDat> userForm = Form.form(EditFrmDat.class);
  

  
  public static Result editUser(){ 
	  Form<EditFrmDat> filledForm = userForm.bindFromRequest();
	  String msg = "";
	  Long uId = new Long(session().get("connected"));
	  
	  
	  if(filledForm.hasErrors()) {
		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest(usrEditF.render(msg, filledForm));
		    } 
	  else {
				  if( !User.mailExists( filledForm.get().getMail(), uId ) ){
					  
					  EditFrmDat eDat =  filledForm.get();
					  String pw1 = eDat.getPwn1(); 
					  String pw2 = eDat.getPwn2();
					  User updU = User.authById(uId, eDat.getPw());
					  if(!updU.equals(null)){
						  updU.setForename(eDat.getForeName());
						  updU.setSurname(eDat.getSurName());
						  
					  }
					  if(!(pw1.isEmpty()&&pw2.isEmpty()) && pw1.equals(pw2)){
						  updU.setPasswd(pw2);
					  }
					  updU.setId(uId);
					  User.updateUser(updU); 
					  return ok(index.render(Messages.get("msg.editok")));  
					  } 
				  else{
					  msg = Messages.get("msg.mailex");
					  return badRequest(usrEditF.render(msg, filledForm));
				  }
		  }
	 
  }
  public static Result showeditUser(){	  
	  Long id = new Long(session().get("connected"));
	  User usr = User.getById(id);	
	  Form<EditFrmDat> fillForm= Form.form(EditFrmDat.class); 
	  fillForm = fillForm.fill(EditFrmDat.prepopulate(usr));
	  return ok(usrEditF.render("", fillForm));
  }
     
}
