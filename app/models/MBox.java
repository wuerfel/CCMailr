package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.joda.time.DateTime;

import com.avaje.ebean.Ebean;

import controllers.routes;

import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
import play.mvc.Result;
/**
 * Object for a Mailbox
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */
@Entity
public class MBox extends Model{
	//Mailbox ID
	@Id
	private long id;
	
	//Mailadress of the Box
	@Required
	private String adress;

	//Timestamp for the end of the validity period
	private long ts_Active;
	
	//Flag for the validity
	private boolean expired;
	
	private String domain;
	
	//Owner of the Box
	@ManyToOne
	@JoinColumn(name = "usr_id", nullable = false)
	private User usr;
	
	//Finder
	public static Finder<Long,MBox> find = new Finder(Long.class, MBox.class);
	
	
	// Getter und Setter
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getAdress() {
		return adress;
	}
	public void setAdress(String adress) {
		this.adress = adress;
	}
	
	public long getTS_Active() {
		return ts_Active;
	}
	public void setTS_Active(long tS_Active) {
		ts_Active = tS_Active;
	}
	public boolean isExpired() {
		return expired;
	}
	public boolean isActive(){
		return !expired;
	}
	public void setExpired(boolean expired) {
		this.expired = expired;
	}

	public User getUsr() {
		return usr;
	}
	public void setUsr(User usr) {
		this.usr = usr;
	}
		
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	
	// EBean Functions
	
	/**
	 * deletes a box
	 * @param id
	 */
	public static void delete(Long id) {
		find.ref(id).delete();
	}
	/**
	 * @param id ID of the Box
	 * @return the box
	 */
	public static MBox getById(Long id) {
		return find.ref(id);
	}
	
	/**
	 * returns the Local Part of a Box
	 * @param id
	 * @return the local part
	 */
	public static String getNameById(Long id) {
		return find.ref(id).getAdress();
	}
	
	/**
	 * @param mbId
	 * @return the mailaddress of the owner of this box
	 */
	public static String getFWD(Long mbId){
		return find.ref(mbId).getUsr().getMail();
	}
	/**
	 * @return all available boxes
	 */
	public static List<MBox> all() {
		return find.all();
	}
	/**
	 * 
	 * @param id ID of a User
	 * @return returns all Boxes of a specific user
	 */
	public static List<MBox> allUser(Long id) {
		return find.where().eq("usr_id", id.toString()).findList();
	}
	/**
	 * stores the Mailbox in the database
	 * @param mb
	 */
	public static void createMBox(MBox mb){
		mb.save();
	}
	/**
	 * checks if a given adress exists
	 * @param mail
	 * @return
	 */
	public static boolean mailExists(String mail, String domain){
		if( !find.where().eq( "adress", mail.toLowerCase() ).eq("domain", domain).findList().isEmpty() ){
			return true;
		} else{ 
			return false;
			}	
	}
	/**
	 * @return the timestamp in a Date format
	 */
	public String getTSAsString(){
		
		DateTime dt = new DateTime(this.ts_Active);

		if(this.ts_Active == 0){
			return "unlimited";
		}else{
			
			String min="";
			if(dt.getMinuteOfHour()<10){
				min = "0"+String.valueOf(dt.getMinuteOfHour());
			}
			else{
				min = String.valueOf(dt.getMinuteOfHour());
			}
		
		return dt.getDayOfMonth()+"."+dt.getMonthOfYear()+"."+dt.getYear()+" "
				+ dt.getHourOfDay()+":"+min;
		}
	}
	
	//rewrote mailExists() for Editing MBoxes
	public static boolean mailExists(String mail, String domain ,Long mbId){
		
		List<MBox> ml = find.where().eq( "adress", mail.toLowerCase() ).eq("domain", domain).findList();
		
		if( !ml.isEmpty() ){ // there's another adress..
			if( ( ml.size() == 1 ) && ( ml.get(0).getId() == mbId ) ){ 
				// Mailbox has the same Id  
				return false;
				}else{
					// more than 1 result or another Id
					return true;
				}
		} else { // there's no other adress
				return false; 
			}
		
	}
	/**
	 * generates a list of the boxes who will expire in the next hour(s)
	 * @param hours the hour(s) 
	 * @return List of MBoxes
	 */
	public static List<MBox> getNextBoxes(int hours){
		DateTime dt = new DateTime();
		dt = dt.plusHours(hours);
		
		return find.where().eq("expired", false).lt("ts_Active", dt.getMillis()).ne("ts_Active", 0).findList();
	}
	
	public static void updateMBox(MBox mb){
		Ebean.update(mb);
	}
	
	
	/**
	 * sets the valid box as invalid and vice versa
	 * @param mId Id of the box
	 * @return value of true means that its now enabled (== not expired)
	 */
	public static boolean enable(Long mId) {
		MBox mb = find.ref(mId);
		mb.setExpired(!mb.isExpired());
		Ebean.update(mb);
		return (!mb.isExpired());
	}
	
	
}
