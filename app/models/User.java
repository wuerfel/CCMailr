package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import play.db.ebean.*;
import play.data.validation.Constraints.*;
import javax.persistence.*;

import lib.BCrypt;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
/**
 * User Object
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */
@Entity
public class User extends Model{
	//UserId
	@Id
	private long id;
	//Forename of the User
	@Required
	private String forename;
	//Surname of the User
	@Required
	private String surname;
	//Mailadress
	@Required
	@Email
	private String mail;
	//Password
	@Required
	private String passwd;
	
	//Admin-Flag
	private boolean admin; 
	
	

	//Relation to the Mailboxes 
	@OneToMany(mappedBy="usr", cascade=CascadeType.ALL)
    public List<MBox> boxes = new ArrayList<MBox>();
    //Finder
	public static Finder<Long,User> find = new Finder(Long.class, User.class);
	
	// ----------------------------- Getter and Setter ------------------------
	/**
	 * Standard constructor, just initialize the variables
	 */
	public User(){
		id = 0;
		forename = "";
		surname = "";
		mail = "";
		passwd = "";
	}
	
	/**
	 * constructor for the userobject
	 * @param fName forename
	 * @param sName surname
	 * @param eMail mail
	 * @param pw password
	 */
	public User(String fName, String sName, String eMail, String pw){
		id = 0;
		setForename(fName);
		setSurname(sName);
		setMail(eMail);
		setPasswd(pw);
		setAdmin(false);
	}
	/**
	 * persists a user in the DB
	 * @param u the user
	 */
	public static void createUser(User u){
		u.setMail(u.getMail().toLowerCase());
		u.save();
	}
	
	/**
	 * @return the Id of a user
	 */
	public long getId() {
		return id;
	}

	/**
	 * Sets the id of a Userobject used to identify it in the db
	 * @param id 
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the forename of a user
	 */
	public String getForename() {
		return forename;
	}
	/**
	 * Sets the forename of a user
	 * @param forename
	 */
	public void setForename(String forename) {
		this.forename = forename;
	}
	/**
	 * @return the surname of a user
	 */
	public String getSurname() {
		return surname;
	}
	/**
	 * Sets the surname of a user
	 * @param surname
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}
	/**
	 * @return the real Mailadress of a user
	 */
	public String getMail() {
		return mail;
	}
	/**
	 * Sets the real Mailadress of a user
	 * @param mail
	 */
	public void setMail(String mail) {
		this.mail = mail.toLowerCase();
	}
	/**
	 * returns the hashvalue of the bcrypted password
	 * @return
	 */
	public String getPasswd() {
		return passwd;
	}
	/**
	 * sets and bcrypts the given password
	 * @param passwd
	 */
	public void setPasswd(String passwd) {
		this.passwd = BCrypt.hashpw(passwd, BCrypt.gensalt());
	}
	
	/**
	 * @return true if a user is admin 
	 */
	public boolean isAdmin() {
		return admin;
	}
	/**
	 * enables or disables the admin-functions
	 * @param admin: enables functions if true
	 */
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	/**
	 * @return the list of all users in the database
	 */
	public static List<User> all() {
		return find.all();
	}
	
	
	//---------------------------- EBean/Finder-Functions----------------------
	/**
	 * checks if a mailadress exists in the database
	 * @param mail: mailadress of a user
	 * @return true if the given adress exists
	 */	
	public static boolean mailExists(String mail){
		if( !find.where().eq( "mail", mail.toLowerCase() ).findList().isEmpty() ){
			return true;
		}else{ return false; }
		
	}
	
	
	/**
	 * 
	 * @param mail
	 * @param uId
	 * @return
	 */
	//rewrote mailExists() for Editing MBoxes & Users
	
	//TODO FIX this function
	public static boolean mailExists(String mail, Long uId){
		
		//get all users with the given mail
		List<User> ul = find.where().eq( "mail", mail.toLowerCase() ).findList();
		
		if( !ul.isEmpty() ){
			//there are some users with that mailadress
			
			if( (ul.size() == 1) && (ul.get(0).getId() == uId) ){
				//there's only one user and the given UID is equal to the UID in the db
				return false;
				}
			else{
				//there's more than one user with that address and/or 
				//the UID belongs to another user
				return true; 
				}
			} 
		else {
			// there's no user with that address
			return false;
			}
	}
	
	/**
	 * returns the user-object that belongs to the given mailadress
	 * @param mail: adress of a user
	 * @return the user
	 */
	public static User getUsrByMail(String mail){
		return find.where().eq("mail", mail.toLowerCase()).findUnique();		
	}
	
	/**
	 * 
	 * @param mail
	 * @param pw
	 * @return the user object if the given mail and password belong together 
	 */

	public static User auth(String mail, String pw){
		//get the user by the mailadress
		User usr = find.where().eq("mail", mail.toLowerCase()).findUnique();
		
		if( !(usr == null) ){
			//there's a user with that address
			
			// TODO next code is redundant, same like authById()
			if(BCrypt.checkpw( pw, usr.getPasswd())){
				//check if the given password is correct
				return usr;
			}
			else {
				//the password is wrong
				return null;
			}
		}
		else {
			//there's no user with that address
			return null; 
			}
	}
	
	/**
	 * 
	 * @param id
	 * @param pw
	 * @return the user object if the given userId and password belong together 
	 */
	
	public static User authById(Long id, String pw){
		User usr = find.ref(id);
		if( BCrypt.checkpw( pw, usr.getPasswd() ) ){
			return usr;
		}
		else {
			
			return null;
		}
	}
	/**
	 * 
	 * @param id: a users id
	 * @return the user-object  
	 */
	public static User getById(Long id){
		return find.ref(id);
	}
	
	/**
	 * updates the data of a user
	 * @param usr: the edited user-object
	 */
	
	public static void updateUser(User usr){
		Ebean.update( usr );
	}

	/**
	 * deletes the user with the given id 
	 * @param id: the id of the user that has to be deleted
	 */

	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	
	/**
	 * promotes the User and Updates the DB 
	 * @param id
	 */
	public static void promote(Long id) {
		User usr = find.ref(id);
		usr.setAdmin( !usr.isAdmin() );
		Ebean.update( usr );
	}
	
}
