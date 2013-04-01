package controllers;

import org.joda.time.DateTime;

import com.avaje.ebean.Ebean;

import models.EditFrmDat;
import models.Login;
import models.User;
import play.*;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.*;
import views.html.*;
import  java.text.MessageFormat;
import java.util.Properties;
import java.util.Random;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 * Handles all general application actions like login, logout,
 * forgot password or index page
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */

public class Application extends Controller {

	static Form<Login> login = Form.form(Login.class);
	static Form<User> userForm = Form.form(User.class);
	static Form<EditFrmDat> registerForm = Form.form(EditFrmDat.class);
	
	
  public static Result index() {
    return ok(index.render(Messages.get("app.welcome")));
  }
  
//-------------------- Registration -----------------------------------
   /**
    * shows the registration form
    * @return
    */
  
  public static Result showForm(){  	
	  return ok(usrAddF.render("", registerForm));
  }
  
  /**
   * Creates the User (POST for register)
   * @return
   */
public static Result createUser(){
	
	Form<EditFrmDat> filledForm = registerForm.bindFromRequest();
	EditFrmDat efd = filledForm.get();
	String msg = "";
	//next field isn't shown to the user->set to a default value to prevent form errors
	efd.setPwn2("0");  
	filledForm.fill(efd);
	
	if(filledForm.hasErrors()) {
		msg = Messages.get("msg.formerr");
		msg += filledForm.errors().toString();
		return badRequest(usrAddF.render(msg, filledForm));
		} 
	else { //form was filled correctly, go on!
		
		if( !User.mailExists( filledForm.get().getMail() ) ){
			// a new user, check if the passwords are matching
			
			if(efd.getPw().equals( efd.getPwn1() )){
			//create the user	
			User.createUser(filledForm.get().getAsUser()); 
			return ok(index.render(Messages.get("msg.regok")));
			
			} else {
				//password mismatch
				return badRequest(usrAddF.render(Messages.get("msg.formerr"), filledForm)); 
				
			} 
		  }
		else{ //mailadress already exists
			msg=Messages.get("msg.mailex");
			return badRequest(usrAddF.render(msg, filledForm));
			}
		}
	}
  
  
//-------------------- Login/-out Functions -----------------------------------

  /**
   * shows the login form
   * @return the rendered login form
   */
  public static Result loginForm(){
	  return ok( loginF.render("", login) );
  }
  
  
  /**
   *  Handles the logout process
   * @return the index page
   */
  public static Result logout(){
	  session().clear();
	  return ok( index.render(Messages.get("msg.logout")) );
  }
  
  /**
   * Handles the login-process 
   * @return the login form or the index page 
   */
  public static Result loginProc(){
	  
	  Form<Login> filledForm = login.bindFromRequest();
	  String msg = Messages.get("msg.formerr");

	  if( filledForm.hasErrors() ) {
		  //some fields weren't filled
		  	msg += filledForm.errors().toString();
		    return badRequest( loginF.render(msg, filledForm) );
		  } 
	  else {
		  
		  Login l = filledForm.get();
		  //get the user if authentication was correct
		  User lgr = User.auth(l.getMail().toLowerCase(), l.getPwd());
		  
		  if( lgr != null ){ //correct login
			  //set the cookie
			  session( "connected", String.valueOf( lgr.getId() ) );

			  if( lgr.isAdmin() ){
				  //also set an admin-flag if the account is an admin-account
				  session( "adm", String.valueOf(true) ); 
				  }
			  //TODO: ADM-Zugriff per DB, nicht per Cookie?
			  return ok( index.render( Messages.get("msg.login") ) ); 
			  }
		  //TODO maybe this should go into an else-path?
		 msg = Messages.get("msg.formerr");
	    return badRequest( loginF.render( msg, filledForm ) );  
	  }
  }
  
  /**
   * shows the forgot pw page
   * @return forgot-pw-form
   */
  public static Result forgotPW(){
	  return ok( forgotPw.render("", login) );
  }
  
  /**
   * generates a new password and sends it to the user
   * @return index page
   */
  public static Result pwResend(){
	  
	  Form<Login> filledForm = login.bindFromRequest();
	  String msg = Messages.get("msg.formerr");
	  Login l = filledForm.get();
	  //next field isn't shown to the user->set to a default value to prevent form errors
	  l.setPwd("sth");
	  filledForm.fill(l);
	  
	  if( filledForm.hasErrors() ) {
		  //some fields weren't filled
		  msg += filledForm.errors().toString();
		  return badRequest( forgotPw.render(msg, filledForm) );
		  } 
	  else {
		  User usr = User.getUsrByMail(l.getMail());
		  if( usr != null ){ //mailadress was correct
			  //generate a new pw and send it to the given mailadress
			  //TODO [SEC]IMPORTANT! if sendMail() fails, we'll get an empty String (which will be set as PW)
			  String newPw = sendMail(usr.getMail(), usr.getMail());
			  //set the new pw in the db
			  usr.setPasswd(newPw);
			  Ebean.update(usr);
			  return ok( index.render( Messages.get("forgpw.succ") ) ); 
			  }
		  //TODO missing else
		 msg = Messages.get("msg.formerr");
	    return badRequest( forgotPw.render( msg, filledForm ) );  
	  }
	  

  }
  
  /**
   * sends the forgot-password mail to a user
   * @param mail recipient address of a user
   * @param forename name of a user for the text
   * @return the password to set it in the db
   */
  
 private static String sendMail(String mail, String forename){
	 	  //standard-host of this application
	 	  //TODO configurable sender-address?
	 	  String host = Play.application().configuration().getString("fpw.host");
	      String to = mail;
	      String from = "admin@"+host;
	      Properties properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", host);
	      Session session = Session.getDefaultInstance(properties);
	      	
	      try{	
	    	  //add the header-informations
		         MimeMessage message = new MimeMessage(session);
		         message.setFrom(new InternetAddress(from));
		         message.addRecipient(Message.RecipientType.TO,
		                                  new InternetAddress(to));
		         message.setSubject(Messages.get("forgpw.title"));
		         
		      //add the message body
		         String rueck = getRndPw();
		         //TODO create a better message-text
		         message.setText(Messages.get("forgpw.msg", forename, rueck ));
		         Transport.send(message);
		         return rueck;
		         
	      }catch (Exception e) {
	         e.printStackTrace();
	         return "";
	      }
	}
 
 /**
  * generates a random password 
  * @return a random password with 7 characters
  */
 //TODO modify this function, also use at least digits and uppercase-letters and a variable length 
 private static String getRndPw(){
	  Random rand = new Random();
	  StringBuffer strBuf = new StringBuffer();
	  
	  for (int i = 0 ; i < 7 ; i++ ) {
		  //generates a random char between a and z (an ascii a is 97, z is 122 in dec)
	  	strBuf.append( (char) ( (Math.abs( rand.nextInt() ) %26 ) +97 ) );
	  }
	  return strBuf.toString();
 }
	
  
}
