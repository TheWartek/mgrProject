package pl.mgrProject.action;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.log.Log;
import org.postgis.Point;

import pl.mgrProject.action.algorithm.Algorithm;
import pl.mgrProject.model.PrzystanekTabliczka;

/**
 * Klasa obs�uguj�ca zdarzenia ze strony g�ownej poprzez Seam Remote
 * Komponent Seam o nazwie 'homeBean' oraz komponent EJB stanowy o zasiegu konwersacji
 * Implementuje interfejs Home
 * @author bat
 *
 */
@Name("homeBean")
@Stateful
@Scope(ScopeType.CONVERSATION)
public class HomeBean implements Serializable, Home {
 

	@Logger
	private Log log;
	
	private Point startPoint ;
	
	private Point stopPoint ;

	private Date startTime;
	@In
	private EntityManager mgrDatabase;
	
	@In(create=true)
	private Algorithm algorithmBean;
	
	/**
	 * Pobiera punkt startowy
	 */
	public Point getStartPoint() {
		//startPoint.(arg0)
		return startPoint; 
	}
	
	public Boolean setStartPoint(double x, double y){
		if(x==0 || y==0) return false;
		this.startPoint = new Point(x, y);
		this.startPoint.setSrid(4326);
		log.info("homeBean: ustawiono startPoint na wspolrzedne: "+String.valueOf(x) + ", " + String.valueOf(y));
		return true;
	}

	/**
	 * Pobiera punkt koncowy
	 */
	public Point getStopPoint() {
		return stopPoint;
	}
	
	public Boolean setStopPoint(double x, double y){
		if(x==0 || y==0) return false;
		this.stopPoint = new Point(x, y);
		this.stopPoint.setSrid(4326);
		log.info("homeBean: ustawiono stopPoint na wspolrzedne: "+String.valueOf(x) + ", " + String.valueOf(y));
		return true;
	}

	
	/**
	 * Pobiera godzine startu
	 */
	public Date getStartTime() {
		return startTime;
	}

	public Boolean setStartTime(Date startTime) {
		if(startTime == null)
			return false;
		this.startTime = startTime;
		return true;
	}
	
	/**
	 * Uruchamia alogyrtm 
	 * @return Odpowiedz  
	 */
	public Odpowiedz findRoute() {
		Odpowiedz odpowiedz = null;
		
		algorithmBean.setStartPoint(startPoint);
		algorithmBean.setStopPoint(stopPoint);
		algorithmBean.setStartTime(startTime);
		Boolean result = algorithmBean.run();
		if(result)
			odpowiedz = new Odpowiedz(algorithmBean.getPath(), algorithmBean.getHours(), "OK");

		return odpowiedz;
	}
	


	@Destroy
	@Remove
	public void destory(){}


	

}
