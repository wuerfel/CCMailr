
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;
import javassist.bytecode.Descriptor.Iterator;
import models.Jamesconn;
import models.MBox;
import models.User;
import akka.util.Duration;
import play.*;
import play.libs.Akka;
import play.mvc.*;
import play.mvc.Http.RequestHeader;
import views.html.index;
import static play.mvc.Results.*;

/**
 * Handles the onStart, onStop and onError actions
 * @author Patrick Thum 2012
 * released under Apache 2.0 License
 */

public class Global extends GlobalSettings {

	private static Jamesconn jmc;
  @Override
  public void onStart(Application app) {
	final String host = Play.application().configuration().getString("jserv.host");
	final String port = Play.application().configuration().getString("jserv.port");
	final String mint = Play.application().configuration().getString("mbox.interval");
	final String msize = Play.application().configuration().getString("mbox.size");
	final String admMail = Play.application().configuration().getString("adm.mail");
 
	jmc = new Jamesconn(host, port);
    Logger.info("Application has started");
    User adm = User.getUsrByMail( admMail );
    //create the adminaccount specified in the application.conf if not exists
    if( adm == null ){
    	String admFName = Play.application().configuration().getString("adm.fname");
    	String admSName = Play.application().configuration().getString("adm.sname");
    	String admPw = Play.application().configuration().getString("adm.pw");
    	
    	adm = new User(admFName, admSName, admMail, admPw);
  	  	adm.setAdmin(true);
  	  	User.createUser(adm);
    }

    
    
    Akka.system().scheduler().schedule(Duration.create(0, TimeUnit.SECONDS),
    		  Duration.create(new Integer(mint), TimeUnit.MINUTES),
    		  new Runnable() {
    		    public void run() {
    		      List<MBox> mbList = MBox.getNextBoxes(new Integer(msize));
    		      ListIterator<MBox> it = mbList.listIterator();
    		      DateTime dt = new DateTime();
    		      while(it.hasNext()){
    		    	  MBox mb = it.next();
    		    	  if(dt.isAfter( mb.getTS_Active() ) && !( mb.getTS_Active() == 0 ) ){
    		    		  //this element is expired
    		    		  MBox.enable( mb.getId() );
    		    		  jmc.deleteAllUserData(mb.getAdress(), mb.getDomain(), mb.getUsr().getMail());
    		    	  }
    		      }
    		      
    		    }
    		  }
    		); 
    
  }  
  
  @Override
  public void onStop(Application app) {
    Logger.info("Application shutdown...");
  } 
  
  
  
  @Override
  public Result onError(RequestHeader request, Throwable t) {
    //return internalServerError(views.html.errorPage(t));
	  return internalServerError("Sorry, something went wrong..");
  }  
    
    
}