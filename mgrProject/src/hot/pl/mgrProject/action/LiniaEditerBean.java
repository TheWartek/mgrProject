package pl.mgrProject.action;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.log.Log;

import pl.mgrProject.action.algorithm.AdjacencyMatrix;
import pl.mgrProject.model.Linia;
import pl.mgrProject.model.Odjazd;
import pl.mgrProject.model.PrzystanekTabliczka;
import pl.mgrProject.model.TypDnia;

/**
 * Implementacja interfejsu do edycji Linii
 * 
 * @author bat
 * 
 */
@Stateful
@Name("liniaEditer")
@Scope(ScopeType.CONVERSATION)
public class LiniaEditerBean implements LiniaEditer, Serializable {

	@Logger
	private Log log;

	@In
	private EntityManager mgrDatabase;

	@In(required = false)
	@Out(required = false)
	private Linia editedLinia;

	@In(create=true)
	private AdjacencyMatrix adjacencyMatrixBean;
	
	
	@DataModel
	List<PrzystanekTabliczka> ptList;

	@Factory("ptList")
	public List<PrzystanekTabliczka> getPtList() {
		if (editedLinia != null) {
			// Liczba przystankow w bazie
			Long liczba = (Long) mgrDatabase
					.createQuery(
							"SELECT COUNT(p) FROM PrzystanekTabliczka p WHERE p.linia = :linia")
					.setParameter("linia", editedLinia).getSingleResult();
			log.info("Liczba Tabliczek w bazie: " + liczba);

			// jezeli jeszcze nie pobrano z bazy lub liczba przystankow sie
			// rozni
			if (ptList == null || liczba != ptList.size()) {
				ptList = mgrDatabase.createNamedQuery("tabliczkiPoLinii")
						.setParameter("linia", editedLinia).getResultList();
				log.info("Pobrano z bazy " + ptList.size() + " tabliczek");
			}
			return ptList;
		} else
			return null;

	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void merge() {
		if (editedLinia == null)
			return;

		for (PrzystanekTabliczka pt : ptList) {
			List<Odjazd> odjazdy = pt.getOdjazdy();
			for (Odjazd odjazd : odjazdy) {
				if (odjazd.getId() == null)
					mgrDatabase.persist(odjazd);
				else
					mgrDatabase.merge(odjazd);
			}
			mgrDatabase.merge(pt);
		}
		mgrDatabase.merge(editedLinia);
		log.info("Uaktualniono linie nr " + editedLinia.getNumer());
		mgrDatabase.flush();
		editedLinia = null;
		adjacencyMatrixBean.compileMap();
		// destory();
	}

	public void cancel() {
		this.editedLinia = null;
		// destory();
	}

	@Begin(join = true)
	public void setEditedLinia(Linia l) {
		editedLinia = l;
		// lazy loading
		ptList = editedLinia.getPrzystanekTabliczka();
		ptList.size();
		for (PrzystanekTabliczka pt : ptList) {
			// lazy loading
			pt.getOdjazdy().size();
		}
		mgrDatabase.flush();
		mgrDatabase.clear();
	}

	public Linia getEditedLinia() {
		return editedLinia;
	}

	public void removeOdjazd(Odjazd o) {
		if (editedLinia == null)
			return;
	}

	public void addOdjazdSw() {
		addOdjazd(TypDnia.SWIETA);
	}

	public void addOdjazdPo() {

		addOdjazd(TypDnia.DZIEN_POWSZEDNI);
	}

	/**
	 * Funkcja oblicza nastepne godziny na przystankach, po zmianie godziny
	 * pierwszej lub po zmianie odleglosci pomedzy przystankami
	 */
	public void change() {

		Calendar calendar = new GregorianCalendar();

		// bez ostatniego (pobieram aktualny, ustawiam nastepny)
		for (int i = 0; i < ptList.size() - 1; ++i) {
			PrzystanekTabliczka pt = ptList.get(i);

			List<Odjazd> odjazdy = pt.getOdjazdy();
			List<Odjazd> nextOdjazdy = pt.getNastepnyPrzystanek().getOdjazdy();

			if (odjazdy.size() != nextOdjazdy.size()) {
				System.out.println("Ilo�� odjazd�w si� nie zgadza");
				return;
			}

			for (int j = 0; j < odjazdy.size(); ++j) {
				Odjazd odjazd = odjazdy.get(j);
				calendar.setTime(odjazd.getCzas());
				calendar.add(Calendar.MINUTE, pt.getCzasDoNastepnego());

				Odjazd nextOdjazd = nextOdjazdy.get(j);
				nextOdjazd.setCzas(calendar.getTime());
			}

		}
	}

	@Destroy
	@Remove
	public void destory() {
	}

	private void addOdjazd(TypDnia t) {

		if (editedLinia == null)
			return;

		Calendar now = new GregorianCalendar();

		for (int i = 0; i < ptList.size(); ++i) {
			PrzystanekTabliczka pt = ptList.get(i);
			Odjazd odj = new Odjazd();
			odj.setCzas(now.getTime());
			odj.setTypDnia(t);
			odj.setPrzystanekTabliczka(pt);
			pt.addOdjazd(odj);
			now.add(Calendar.MINUTE, pt.getCzasDoNastepnego());
		}

	}

	@Begin(join=true)
	public void setEditedLinia(Long id) {
		editedLinia = (Linia)mgrDatabase.createQuery("SELECT l FROM Linia l where l.id = :id").setParameter("id", id).getSingleResult();
		// lazy loading
		ptList = editedLinia.getPrzystanekTabliczka();
		ptList.size();
		for (PrzystanekTabliczka pt : ptList) {
			// lazy loading
			pt.getOdjazdy().size();
		}
		mgrDatabase.flush();
		mgrDatabase.clear();		
	}
}
