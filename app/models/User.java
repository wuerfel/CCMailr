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
	 * Standard constructor, just initializes the variables
	 */
	public User(){
		id=0;
		forename="";
		surname="";
		mail="";
		passwd="";
	}
	/**
	 * Constructor for Userobject
	 * @param fName forename
	 * @param sName surname
	 * @param eMail mail
	 * @param pw password
	 */
	public User(String fName, String sName, String eMail, String pw){
		id=0;
		setForename(fName);
		setSurname(sName);
		setMail(eMail);
		setPasswd(pw);
		setAdmin(false);
	}
	/**
	 * persists a user in the DB
	 * @param u
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
	public void setForename(String forename) {
		this.forename = forename;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getMail() {
		return mail;
	}
	public void setMail(String mail) {
		this.mail = mail.toLowerCase();
	}
	public String getPasswd() {
		return passwd;
	}
	public void setPasswd(String passwd) {
		this.passwd = BCrypt.hashpw(passwd, BCrypt.gensalt());
	}
	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	public static List<User> all() {
		return find.all();
	}
	
	
	//---------------------------- EBean/Finder-Functions----------------------
	public static boolean mailExists(String mail){
		if( !find.where().eq( "mail", mail.toLowerCase() ).findList().isEmpty() ){
			return true;
		}else{ return false; }
		
	}
	//rewrote mailExists() for Editing MBoxes	
	public static boolean mailExists(String mail, Long uId){
		List<User> ul = find.where().eq( "mail", mail.toLowerCase() ).findList();
		if( !ul.isEmpty() ){
			if( (ul.size() == 1) && (ul.get(0).getId() == uId) ){ 
				return false;
				}
			else{ return true; }
			} 
		else { return false; }
	}

	public static User getUsrByMail(String mail){
		return find.where().eq("mail", mail.toLowerCase()).findUnique();		
	}

	public static User auth(String mail, String pw){
		User usr = find.where().eq("mail", mail.toLowerCase()).findUnique();
		if(!(usr==null)){
			if(BCrypt.checkpw( pw, usr.getPasswd())){
			return usr;
			}
			else {
				return null;
			}
		}
		else { return null; }
	}
	public static User authById(Long id, String pw){
		User usr = find.ref(id);
		if(BCrypt.checkpw( pw, usr.getPasswd())){
		return usr;
		}
		else {
			return null;
		}
	}
	
	public static User getById(Long id){
		return find.ref(id);
	}
	public static void updateUser(User usr){
		Ebean.update( usr );
	}


	public static void delete(Long id) {
		find.ref(id).delete();
	}
	
	//promotes the User and Updates the DB
	public static void promote(Long id) {
		User usr = find.ref(id);
		usr.setAdmin( !usr.isAdmin() );
		Ebean.update( usr );
	}
	
}
