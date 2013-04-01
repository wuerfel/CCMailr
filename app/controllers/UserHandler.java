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
  

  /**
   * edits the user-data
   * @return the result-page
   */
  public static Result editUser(){ 
	  Form<EditFrmDat> filledForm = userForm.bindFromRequest();
	  String msg = "";
	  Long uId = new Long(session().get("connected"));
	  
	  
	  if(filledForm.hasErrors()) {
		  // the filled form has errors
		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest(usrEditF.render(msg, filledForm));
		    } 
	  else {
		  // the form is filled correctly
				  if( !User.mailExists( filledForm.get().getMail(), uId ) ){
					  /*
					  TODO check if this works properly
					   actually it should go here if there's no user or
					   uid and mailadr. belong together 
					  */
					  EditFrmDat eDat =  filledForm.get();
					  String pw1 = eDat.getPwn1(); 
					  String pw2 = eDat.getPwn2();
					  User updU = User.authById(uId, eDat.getPw());
					  if(!updU.equals(null)){
						  // the user authorized himself
						  // update the  fore- and surname
						  updU.setForename(eDat.getForeName());
						  updU.setSurname(eDat.getSurName());
					  }//TODO i think this brace should be set after the return
					  if(!(pw1.isEmpty()&&pw2.isEmpty()) && pw1.equals(pw2)){
						  //new password was entered and the repetition is equally
						  updU.setPasswd(pw2);
					  }
					  updU.setId(uId); //TODO check if this is unneccessary
					  User.updateUser(updU); //update the user
					  
					  return ok(index.render(Messages.get("msg.editok")));
					  
					  } 
				  else{
					  msg = Messages.get("msg.mailex");
					  return badRequest(usrEditF.render(msg, filledForm));
				  }
		  }
	 
  }
  
  /**
   * prepopulate the editform and show it
   * @return the user-edit-form
   */
  public static Result showeditUser(){	  
	  Long id = new Long(session().get("connected"));
	  User usr = User.getById(id);	
	  Form<EditFrmDat> fillForm = Form.form(EditFrmDat.class); 
	  fillForm = fillForm.fill(EditFrmDat.prepopulate(usr));
	  return ok(usrEditF.render("", fillForm));
  }
     
}
