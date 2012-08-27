package pl.mgrProject.action.rest;

import java.io.StringWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

import pl.mgrProject.model.Linia;
import pl.mgrProject.model.Przystanek;
import pl.mgrProject.model.PrzystanekTabliczka;
import flexjson.JSONSerializer;

@Path("/rozklad")
@Name("rozkladServiceBean")
public class RozkladServiceBean implements RozkladService {

	// @EJB(beanName = "PrzystanekTabliczkaDAOBean")
	// private PrzystanekTabliczkaDAO przystanekTabliczkaDAO;

	// @In(create = true, required = true, value="liniaDAO")
	// private LiniaDAO liniaDAO;

	@In
	private EntityManager mgrDatabase;

	// @EJB(beanName = "PrzystanekDAOBean")
	// private PrzystanekDAO przystanekDAO;

	@GET
	@Path("/all")
	public String all(@QueryParam("format") String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@GET
	@Path("/linie/")
	@Produces(MediaType.TEXT_PLAIN)
	public String linie(@QueryParam("format") String format) {
		try {
			System.out.println("Wybrano format: "
					+ (format == null ? "json" : format));

			List<Linia> listaLinii = null;

			listaLinii = mgrDatabase.createNamedQuery("wszystkieLinie")
					.getResultList();

			System.out.println("Pobrano " + listaLinii.size() + " lini");

			return printOut(listaLinii, format, null);
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	@GET
	@Path("/linie/{numer}")
	@Produces(MediaType.TEXT_PLAIN)
	public String linia(@PathParam("numer") Integer numer,
			@QueryParam("format") String format) {
		try {
			System.out.println("Wybrano format: "
					+ (format == null ? "json" : format));
			System.out.println("Pobieranie linii z nr " + numer);
			List<Linia> listaLinii = null;

			listaLinii = mgrDatabase.createNamedQuery("liniePoNumerze")
					.setParameter("numer", numer).getResultList();

			System.out.println("Pobrano " + listaLinii.size() + " lini");
			int sum = 0;
			for (Linia linia : listaLinii) {
				List<PrzystanekTabliczka> przystanekTabliczka = linia
						.getPrzystanekTabliczka();
				sum += przystanekTabliczka.size();

			}

			System.out.println("Pobrano " + sum + " tabliczek");

			return printOut(listaLinii, format, "przystanekTabliczka");
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	@GET
	@Path("/przystanki/")
	@Produces(MediaType.TEXT_PLAIN)
	public String przystanki(@QueryParam("format") String format) {
		try {
			System.out.println("Wybrano format: "
					+ (format == null ? "json" : format));

			List<Przystanek> listaPrzystankow = null;

			listaPrzystankow = mgrDatabase.createNamedQuery(
					"wszystkiePrzystanki").getResultList();

			System.out.println("Pobrano " + listaPrzystankow.size()
					+ " przystankow");

			return printOut(listaPrzystankow, format, null);
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}

	}

	@GET
	@Path("/przystanki/{id}")
	@Produces(MediaType.TEXT_PLAIN)
	public String przystanek(@PathParam("id") Long id,
			@QueryParam("format") String format) {
		try {
			System.out.println("Wybrano format: " + format == null ? "json"
					: format);
			System.out.println("Pobieranie przystanku z nr " + id);
			List<Przystanek> listaPrzystankow = null;

			listaPrzystankow = mgrDatabase
					.createQuery("SELECT p from Przystanek p where p.id = :id")
					.setParameter("id", id).getResultList();

			System.out.println("Pobrano " + listaPrzystankow.size()
					+ " przystankow");
			int sum = 0;
			for (Przystanek przystanek : listaPrzystankow) {
				List<PrzystanekTabliczka> przystanekTabliczka = przystanek
						.getPrzystanekTabliczki();
				sum += przystanekTabliczka.size();

			}

			System.out.println("Pobrano " + sum + " tabliczek");

			return printOut(listaPrzystankow, format, "przystanekTabliczki");
		} catch (Exception e) {
			e.printStackTrace();
			return e.toString();
		}
	}

	private String printOut(Object obj, String format, String... include)
			throws Exception {

		if (format != null && (format.equals("xml") || format.equals("XML"))) {
			return toXml(obj);
		} else {
			return toJson(obj, include);
		}
	}

	private String toJson(Object obj, String... include) {
		String json = null;
		if (include != null) {
			json = new JSONSerializer().include(include).serialize(obj);
		} else {
			json = new JSONSerializer().serialize(obj);
		}
		return json;
	}

	private String toXml(Object obj) throws JAXBException {

		JAXBContext jc = JAXBContext.newInstance(new Class[] {
				Przystanek.class, Linia.class });

		Marshaller m = jc.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		StringWriter sw = new StringWriter();

		// marshall the object to XML
		m.marshal(obj, sw);

		// print it out for this example
		return sw.toString();

	}

}
