package pl.mgrProject.action;

import java.util.EventListener;
import java.util.List;

import javax.ejb.Local;
import javax.ejb.Remove;

import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.remoting.WebRemote;

import pl.mgrProject.model.Linia;
import pl.mgrProject.model.TypKomunikacji;

@Local
public interface LiniaDAO {

	

	@WebRemote
	public Boolean saveLinia(Integer numer, TypKomunikacji typ, List<Long> listaIdPrzystankow, Boolean liniaPowrotna);
	
	
	
	@WebRemote
	public List<Linia> getLiniaList();

	
	public void delete();
	
	
	
	public void addListener(EventListener listener);
	
	@Destroy
	@Remove
	public void destory();
}
