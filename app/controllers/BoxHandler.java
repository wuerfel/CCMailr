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
	static Form<MbFrmDat> boxFrm = Form.form(MbFrmDat.class);
	

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
		  	//not all fields were filled
		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest(mboxAddF.render(msg,MBox.allUser(id), lst,filledForm));
		    } 
	  else {
			  //checks whether the Box already exists
			  if( !MBox.mailExists( filledForm.get().getAddress(), filledForm.get().getDomain() ) ){

				  MBox mb = new MBox();
				  String mbName = filledForm.get().getAddress().toLowerCase();
				  // deletes all special characters 
				  //TODO return an error-page if there are some special-chars in the address...
				  mbName = mbName.replaceAll("[^a-zA-Z0-9.]","");
				  // deletes a the dot if its placed at the end of the mailaddress
				  //TODO return an error-page if there is a dot at the end
				  //TODO there should be another mail-exists-check after all deletions.. 
				  if( mbName.endsWith(".") ){ mbName = mbName.substring( 0, mbName.length()-1 ); }
				  
				  //set the data of the box
				  //TODO check whether the next cmd is redundant, maybe it can be removed? 
				  mb.setDomain( filledForm.get().getDomain() );
				  mb.setAdress( mbName );
				  mb.setExpired( false );
				  //TODO check the existence of the new domainname
				  mb.setDomain( filledForm.get().getDomain().toLowerCase() );
				  
				  Long ts = parseDuration( filledForm.get().getDuration() );
				  
				  if( ts == -1 ){ //show an error-page if the timestamp is faulty
					  msg = Messages.get("msg.wrongf");
					  return badRequest( mboxAddF.render(msg, MBox.allUser(id), lst, filledForm) );
				  }
				  // sets the activity-time of the mailbox
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
				  //the mailbox already exists
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
	  //deletes the box from mailserver
	  MBox mb = MBox.getById(boxid);
	  jmc.deleteAllUserData(mb.getAdress(), mb.getDomain(), mb.getUsr().getMail());
	  //deletes the box from DB
	  MBox.delete( boxid );
	 return redirect( routes.BoxHandler.showBoxes() );
  }

/**
 * Edits a Mailbox
 * @param boxId
 * @return error/success-page
 */
public static Result editBox(Long boxId){
	
	  Form<MbFrmDat> filledForm = boxFrm.bindFromRequest();
	  String msg = "";
	  //lst = the domainlist
	  List<String> lst = Arrays.asList(jmc.getDomainList());
	  
	  if(filledForm.hasErrors()) {

		  	 msg = Messages.get("msg.formerr");
		  	 msg += filledForm.errors().toString();
		    return badRequest( mboxEditF.render( msg, boxId, lst, filledForm ) );
		  } 
	  else { //the form was filled correctly
		  
			  if( !MBox.mailExists( filledForm.get().getAddress(), filledForm.get().getDomain(), boxId ) ){
				  // the given mailbox exists
				  boolean changes = false;
				  //we get the boxID with the POST-Request
				  //TODO check if the user who sends the POST equals to the owner of the box
				  MBox mb = MBox.getById(boxId);
				  String newLName = filledForm.get().getAddress().toLowerCase();
				  String newDName = filledForm.get().getDomain().toLowerCase();
				  String oldLName = mb.getAdress();
				  String oldDName = mb.getDomain();
				  String fwd = mb.getUsr().getMail();
			  
				  if(!newLName.equals(oldLName)){ //a new localname was chosen

					  //TODO return an error-page if there are some special-chars in the address...
					  // deletes a the dot if its placed at the end of the mailaddress
					  //TODO return an error-page if there is a dot at the end
					  //TODO there should be another mail-exists-check after all deletions.. 
					  newLName = newLName.replaceAll("[^a-zA-Z0-9.]","");
					  if(newLName.endsWith(".")){newLName=newLName.substring(0, newLName.length()-1);}
					  mb.setAdress( newLName );
					  changes = true;
				  }
				  
				  if(!newDName.equals(oldDName)){ //a new domainname was chosen
					  //TODO check the existence of the new domainname
					  mb.setDomain(newDName);
					  changes = true;
				  }
				  
				  Long ts = parseDuration( filledForm.get().getDuration() );
				  if( ts == -1 ){ // a faulty timestamp was given -> return an errorpage
					  msg =  Messages.get("msg.wrongf");
					  return badRequest( mboxEditF.render( msg, boxId, lst, filledForm ) );
				  }
				  
				  if( !(mb.getTS_Active() == ts) ){
					  //check if the MBox-TS is unequal to the given TS in the form
					  mb.setTS_Active( ts );  
					  changes = true;
				  }

				  //Updates the Box if changes were made
				  if(changes){
					  mb.setExpired(false);
					  //TODO consider a failure in both update-processes
					  MBox.updateMBox( mb );
					  jmc.editBox(oldLName, oldDName, newLName, newDName, fwd);
				  }
				  
				  return redirect( routes.BoxHandler.showBoxes() );  
				  } 
			  else{ 
				  // mailexists-check was false
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
	  //TODO nullpointerexception if the james-server is unavailable
	  List<String> lst = Arrays.asList(jmc.getDomainList());
	  mbdat.setDuration( "5h" );
	  boxFrm = boxFrm.fill( mbdat );

	  return ok( mboxAddF.render( "", MBox.allUser(id), lst, boxFrm) );
  }
  
  /**
   * Generates a random name for the mailbox
   * @return a random name
   */ 
  private static String getRndName(){
	  //TODO modify this function, also use at least digits and uppercase-letters and a variable length 
	  Random rand = new Random();
	  StringBuffer strBuf = new StringBuffer();
	  for (int i = 0 ; i < 7 ; i++ ) {
		//generates a random char between a and z (an ascii a is 97, z is 122 in dec)
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
	  if( !(mb.getTS_Active() == 0) && (mb.getTS_Active() < dt.getMillis()) ){
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
		  String[] str = s.split(",");
		  String helper = "";
		  
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
			  if( (d == -1) || (h == -1) ){
				  return -1;
			  }
			  s = s.trim();
			  if( s.equals("0") ){
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
	  if( ( d > 30 ) || ( ( d == 30 ) && (h >= 0) ) ){
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
	  helper = helper.trim();
	  if( helper.matches("[0-9]+") ){
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
	  helper = helper.trim();
	  if( helper.matches("[\\d+][d|h][,][\\d][d|h]") || helper.matches("[\\d+][d|h]") || helper.matches("[0]") ){
		  return 0;
		  
	  }
	  else{
		return -1;  
	  }
}
  
	private static String parseTime(Long milis){
		DateTime dt = new DateTime();
		float times = (milis - dt.getMillis()) / 3600000.0f; //in hours
		if( milis == 0 ){
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
		
		return hours + "h," + days + "d";
	}
}


