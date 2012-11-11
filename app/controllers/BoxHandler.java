package controllers;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.joda.time.DateTime;

import models.Jamesconn;
import models.MBox;
import models.User;
import models.MbFrmDat;
import play.*;
import play.data.Form;
import play.i18n.Messages;
import play.mvc.*;
import views.html.*;
/**
 * Handles all actions for the Mailboxes 
 * like add, delete and edit box
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */
@Security.Authenticated(SecHandler.class)
public class BoxHandler extends Controller {
	private static String host = Play.application().configuration().getString("jserv.host");
	private static String port = Play.application().configuration().getString("jserv.port");
	private static Jamesconn jmc = new Jamesconn(host, port);
	static Form<MbFrmDat> boxFrm = form(MbFrmDat.class);
	

	/**
	 * Adds a Mailbox to the Useraccount
	 * @return the Mailbox-Overviewpage
	 */
  public static Result addBox(){
	  
	  Form<MbFrmDat> filledForm = boxFrm.bindFromRequest();
	  String msg = "";
	  Long id = new Long(session().get("connected"));
	  List<String> lst = Arrays.asList( jmc.getDomainList() );
	  
	  if( filledForm.hasErrors() ) {
		  	//nicht alle Felder wurden ausgefuellt
		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest(mboxAddF.render(msg,MBox.allUser(id), lst,filledForm));
		    } 
	  else {
			  //checks whether the Box already exists
			  if( !MBox.mailExists( filledForm.get().getAddress(), filledForm.get().getDomain() ) ){

				  MBox mb = new MBox();
				  String mbName = filledForm.get().getAddress().toLowerCase();
				  mb.setDomain(filledForm.get().getDomain());
				  mb.setAdress(mbName);
				  mb.setExpired(false);
				  mb.setDomain(filledForm.get().getDomain().toLowerCase());
				  Long ts = parseDuration( filledForm.get().getDuration() );
				  if(ts==-1){
					  msg =  Messages.get("msg.wrongf");
					  return badRequest( mboxAddF.render(msg, MBox.allUser(id), lst, filledForm) );
				  }
				  mb.setTS_Active( ts );  
				  
				  mb.setUsr( User.getById(id) );
				  
				  String fwd = mb.getUsr().getMail();
				  //creates the Box on the MailServer and sets the FWD
				  jmc.addUser(mb.getAdress(), mb.getDomain(), fwd);
				  //creates the Box in the DB
				  MBox.createMBox(mb);
				  
				  return redirect( routes.BoxHandler.showBoxes() );  
				  } 
			  else{
				  msg = Messages.get("msg.mailex");
				  return badRequest( mboxAddF.render(msg, MBox.allUser(id), lst, filledForm) );
			  }
		  }
  }
  
  /**
   * Deletes a Box from Mailserver and DB
   * @param boxid the ID of the Mailbox 
   * @return the Mailbox-Overviewpage
   */
  public static Result deleteBox(Long boxid){
	  //deletes the Box from Mailserver
	  MBox mb = MBox.getById(boxid);
	  jmc.deleteAllUserData(mb.getAdress(), mb.getDomain(), mb.getUsr().getMail());
	  //deletes the Box from DB
	  MBox.delete( boxid );
	 return redirect( routes.BoxHandler.showBoxes() );
  }

/**
 * Edits a Mailbox
 * @param boxId
 * @return
 */
public static Result editBox(Long boxId){
	
	  Form<MbFrmDat> filledForm = boxFrm.bindFromRequest();
	  String msg = "";
	  List<String> lst = Arrays.asList(jmc.getDomainList());
	  
	  if(filledForm.hasErrors()) {

		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest( mboxEditF.render( msg, boxId, lst, filledForm ) );
		  } 
	  else {
			  if( !MBox.mailExists( filledForm.get().getAddress(), filledForm.get().getDomain(), boxId ) ){
				  boolean changes = false;
				  MBox mb = MBox.getById(boxId);
				  String newLName = filledForm.get().getAddress().toLowerCase();
				  String newDName = filledForm.get().getDomain().toLowerCase();
				  String oldLName = mb.getAdress();
				  String oldDName = mb.getDomain();
				  String fwd = mb.getUsr().getMail();
			  
				  if(!newLName.equals(oldLName)){
					  mb.setAdress( newLName );
					  changes = true;
				  }
				  if(!newDName.equals(oldDName)){
					  mb.setDomain(newDName);
					  changes = true;
				  }
				  Long ts = parseDuration( filledForm.get().getDuration() );
				  if(ts==-1){
					  msg =  Messages.get("msg.wrongf");
					  return badRequest( mboxEditF.render( msg, boxId, lst, filledForm ) );
				  }
				  if(!(mb.getTS_Active() == ts)){
					  mb.setTS_Active( ts );  
					  changes = true;
				  }

				  //Updates the Boxes if changes were made
				  if(changes){
					  mb.setExpired(false);
					  MBox.updateMBox( mb );
					  jmc.editBox(oldLName, oldDName, newLName, newDName, fwd);
				  }
				  
				  return redirect( routes.BoxHandler.showBoxes() );  
				  } 
			  else{ 
				  // mailexists was false
				  msg =  Messages.get("msg.mailex");
				  return badRequest( mboxEditF.render( msg, boxId, lst, filledForm ) );
			  }
		  }
}

/**
 * Shows the edit-form for the box with boxId.
 * @param boxId ID of the Box
 * @return the edit-form
 */
public static Result showEditBox(Long boxId){

	  MbFrmDat mbdat = new MbFrmDat();
	  MBox mb = MBox.getById( boxId ); 
	  mbdat.setAddress( mb.getAdress() );
	  mbdat.setDomain(mb.getDomain());
	  mbdat.setDuration( parseTime( mb.getTS_Active() ) );
	  List<String> lst=Arrays.asList(jmc.getDomainList());
	  boxFrm = boxFrm.fill( mbdat );
	  return ok( mboxEditF.render( "", boxId, lst, boxFrm ) );

  }

/**
 * Generates the mailbox-overview-page of a user.
 * with prepopulated values for the mail-address
 * @return the mailbox-overview-page
 */
  public static Result showBoxes(){

	  Long id = new Long( session().get("connected") );
	  MbFrmDat mbdat = new MbFrmDat();
	  mbdat.setAddress( getRndName() );
	  //TODO nullpointerexception wenn server nicht laeuft
	  List<String> lst=Arrays.asList(jmc.getDomainList());
	  mbdat.setDuration( "5h" );
	  boxFrm = boxFrm.fill( mbdat );

	  return ok( mboxAddF.render( "", MBox.allUser(id), lst, boxFrm) );
  }
  
  /**
   * Generates a random name for the mailbox
   * @return a random name
   */ 
  private static String getRndName(){
	  Random rand = new Random();
	  StringBuffer strBuf = new StringBuffer();
	  for (int i = 0 ; i < 7 ; i++ ) {
		  //TODO Intervall anpassen
	  	strBuf.append( (char) ( (Math.abs( rand.nextInt() ) %26 ) +97 ) );
	  }
	  return strBuf.toString();
  }
  
  
  /**
   * sets the box valid/invalid 
   * @param id the ID of the mailbox
   * @return the rendered mailbox-overview-page
   */
 
  public static Result expireBox( Long id ){
	  //checks whether its valid or invalid at the DB
	  MBox mb = MBox.getById(id);
	  DateTime dt = new DateTime();
	  if(!(mb.getTS_Active()==0)&&(mb.getTS_Active()< dt.getMillis())){
		  //if the validity period is over return the Edit page
		  return redirect(routes.BoxHandler.showEditBox(id));
	  }
		if(MBox.enable( id )){
			//Box is now valid
			jmc.addUser(mb.getAdress(), mb.getDomain(), mb.getUsr().getMail());
		}
		else{
			//Box is now invalid
			jmc.deleteAllUserData(mb.getAdress(), mb.getDomain(), mb.getUsr().getMail());
		}
		return redirect( routes.BoxHandler.showBoxes() );
	}
	
  
  public static long parseDuration(String s){
	  int d = 0; int h = 0;
	  if( parseHelp2(s) >= 0 ){
	  //checks if the string is in the right format
	  // there should be 1 or 2 values (d,h or h,d or h or d or 0)
		  String[] str=s.split(",");
		  String helper="";
		  
	  	  for(int i = 0; i < str.length; i++){
				  str[i] = str[i].toLowerCase();
				
				  if(str[i].contains("d")){
					  helper = str[i].substring(0, str[i].indexOf('d'));
					  d = parseHelp(helper);
				  } else if(str[i].contains("h")){
					  helper = str[i].substring(0, str[i].indexOf('h'));
					  h = parseHelp(helper);
				  }
			  }
			  if((d==-1) || (h==-1)){
				  return -1;
			  }
			  s = s.trim();
			  if(s.equals("0")){
				  return 0;
			  }
		  }
	  else {
		  //
		  return -1;
	  }
	  
	  //everything is okay with the String
	  // so return the milisecs
	  if(h>=24){
		  d += h/24;
		  h = h % 24;
	  }
	  if( ( d > 30 ) || ( ( d == 30 ) && (h >= 0)) ){
		  //max 30days allowed, higher means unlimited
		  return 0;
	  }
	  
	  DateTime dt = new DateTime();
	  return dt.plusDays(d).plusHours(h).getMillis();
  }
  
  /**
   * helper function for parseDuration()
   * checks if a string consists only of digits
   * @param helper string to check
   * @return the integer value of the string or -1 
   * 		if the string does not match
   */
  private static int parseHelp(String helper){
	  helper=helper.trim();
	  if(helper.matches("[0-9]+")){
		  return Integer.parseInt(helper);
		  
	  }
	  else{
		return -1;  
	  }

  }
  /**
   * 
   * @param helper
   * @return
   */
  private static int parseHelp2(String helper){
	  helper=helper.trim();
	  if(helper.matches("[\\d+][d|h][,][\\d][d|h]") || helper.matches("[\\d+][d|h]") || helper.matches("[0]")){
		  return 0;
		  
	  }
	  else{
		return -1;  
	  }
}
  
	private static String parseTime(Long milis){
		DateTime dt = new DateTime();
		float times = (milis - dt.getMillis()) / 3600000.0f; //in hours
		if(milis == 0){
			//the box is "unlimited"
			return "0";
			}
		if(times < 0){
			//the box is expired
			return "1h,1d";
			}

		int hours = Math.round(times);
		int days = hours / 24;
		hours = hours % 24;
		
		return hours+"h,"+days+"d";
	}
}


