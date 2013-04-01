package models;
import java.util.Random;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import lib.BCrypt;

/**
 * Handles the communication to the Apache James 3.0beta4 Server
 * via JMX 
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */

public class Jamesconn {
	private String host;
	private String port;
	private JMXServiceURL url;
	private JMXConnector jmxc;
	private ObjectName dlist;
	private ObjectName users;
	private ObjectName rcprw;
	private MBeanServerConnection mbsc;
	private String strClass =  String.class.getName();
	

    public Jamesconn()  {
    	try{
    		
    	dlist = new ObjectName("org.apache.james:type=component,name=domainlist");
    	users = new ObjectName("org.apache.james:type=component,name=usersrepository");
    	rcprw = new ObjectName("org.apache.james:type=component,name=recipientrewritetable");
    	String serviceURL = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";
    	url = new JMXServiceURL(serviceURL);
    	jmxc = JMXConnectorFactory.connect(url, null);
    	mbsc = jmxc.getMBeanServerConnection();
    	}
    	catch ( Exception e){
    		e.printStackTrace();
    	}
    }
    public Jamesconn(String jhost, String jport)  {
    	try{
    		jhost = jhost.trim(); jport = jport.trim();
    	dlist = new ObjectName("org.apache.james:type=component,name=domainlist");
    	users = new ObjectName("org.apache.james:type=component,name=usersrepository");
    	rcprw = new ObjectName("org.apache.james:type=component,name=recipientrewritetable");
    	String serviceURL = "service:jmx:rmi:///jndi/rmi://"+jhost+":"+jport+"/jmxrmi";
    	url = new JMXServiceURL(serviceURL);
    	jmxc = JMXConnectorFactory.connect(url, null);
    	mbsc = jmxc.getMBeanServerConnection();
    	}
    	catch ( Exception e){
    		e.printStackTrace();
    	}
    }

    

    /**
     * Returns the available Domains
     * @return
     */
    public String[] getDomainList(){

	   	 try {
			return (String[])mbsc.getAttribute(dlist, "Domains");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    }
    
    public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public String getPort() {
		return port;
	}


	public void setPort(String port) {
		this.port = port;
	}


	/**
     * @param dName
     */
    //adds the specified Name to the Domainlist
    public void addDomain(String dName){
    	try {
    	String[] param = {dName};
    	String[] sig = {strClass};
    	
			mbsc.invoke(dlist, "addDomain", param, sig);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * removes a specific domain from the domainlist
     * @param dName: the domainname
     */
    public void removeDomain(String dName){
    	try {
    	String[] param = {dName};
    	String[] sig = {strClass};
    	
			mbsc.invoke(dlist, "removeDomain", param, sig);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Creates the User and sets the Forward
     * @param uName: Local Part of the E-Mail
     * @param dName: Domain Part of the E-Mail
     * @param sFwd: Forward Address
     */
    public void addUser(String uName, String dName, String sFwd){
    	try{
    		String[] param = { uName+"@"+dName, generatePassword()}; 
    		String[] sig = {strClass, strClass};
    		mbsc.invoke(users, "addUser", param, sig);
    		setFwd(uName, dName, sFwd);
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    /**
     * Deletes the Useraccount from the Mailserver
     * @param uName: Local Part of the E-Mail
     * @param dName: Domain Part of the E-Mail
     */
    public void deleteUser(String uName, String dName){
    	try{
    		String[] param = { uName+"@"+dName}; 
    		String[] sig = {strClass};
    		mbsc.invoke(users, "deleteUser", param, sig);
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    
    /**
     * Deletes all Userdata at the Mailserver
     * Includes the Forward and the Useracc
     * @param uName the local-part of the address
     * @param dName the domain-part of the address
     * @param sFwd the Forward-Destination
     */
    
    public void deleteAllUserData(String uName, String dName, String sFwd){
    	unsetFwd(uName, dName, sFwd);
    	deleteUser(uName,dName);
    }
    
    /**
     * changes the Local and/or Domain-Part of a Box
     *  
     * @param uOName old local-part of the Box
     * @param dOName old domain of the Box
     * @param uNName new local-part of the Box
     * @param dNName new domain of the Box
     * @param sFwd forward destination
     */
    public void editBox(String uOName, String dOName, String uNName, String dNName, String sFwd){
    	deleteAllUserData(uOName, dOName, sFwd);
    	addUser(uNName, dNName, sFwd);
    }
    
    
    /**
     * sets the forward to the mailaddress
     * @param uName
     * @param dName
     * @param sFwd
     */
    public void setFwd(String uName, String dName, String sFwd){
    	try{
    		
    		String[] params = {uName, dName, sFwd};
    		String[] signature = {strClass, strClass, strClass};
    		
    		mbsc.invoke(rcprw, "addAddressMapping", params, signature);
    		
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }
    /**
     *  removes the mail-forward mapping
     * @param uName: Local-Part of the Mail  
     * @param dName: Domain-Part of the Mail
     * @param sFwd: Mail (Forward Destination)
     */
    public void unsetFwd(String uName, String dName, String sFwd){
    	try{
    		String[] params = {uName, dName, sFwd};
    		String[] signature = {strClass, strClass, strClass};
    		mbsc.invoke(rcprw, "removeAddressMapping", params, signature);
    		
    	} catch(Exception e){
    		e.printStackTrace();
    		
    	}
    }
    
   /**
    *  generates a random BCrypt hashed password 
    * @return a random password
    */
    public String generatePassword(){
  	  Random rand = new Random();
  	  StringBuffer strBuf = new StringBuffer();
  	  for (int i = 0 ; i < 9 ; i++ ) {
  		  //TODO Intervall anpassen
  	  	strBuf.append( (char) ( (Math.abs( rand.nextInt() ) %26 ) +97 ) );
  	  }
  	  String s  = BCrypt.hashpw(strBuf.toString(),BCrypt.gensalt());
    return s;
    }
}
