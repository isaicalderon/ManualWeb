package com.matco.manual.bean;

import java.io.Serializable;
import java.text.DecimalFormat;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.matco.manual.entity.Alumno;
import com.matco.manual.facade.AlumnoFacade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
@ManagedBean(name = "administradorAlumnosBean")
@ViewScoped

public class AdministradorAlumnosBean extends GenericBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7062286680165571605L;
	private static final DecimalFormat formatoMatricula = new DecimalFormat("000000");
	
	private static final Logger LOG = Logger.getLogger(AdministradorAlumnosBean.class); // Para el log4j
	private FacesContext ctx = FacesContext.getCurrentInstance(); // Para poder conseguir los parametros del web.xml
	private String configFile = ctx.getExternalContext().getInitParameter("admintx_csa"); 
	
	private AlumnoFacade alumnoFacade = new AlumnoFacade(configFile); // instancia de la fachada para conectar con los metodos
	
	private List<Alumno> alumnosList; // lista de alumnos
	private Alumno alumno; //el alumno que sera editado
	private LoginBean loginBean; // instancia del loginBean para obtener el usuario
	
	private String nombres; // nombres del alumno
	private String apellidoPaterno;
	private String apellidoMaterno;
	private int matricula;
	private String error; // Detalle del error que puede generar una fachada
	private String summary = "Alumnos"; // Es la tabla en la que estamos trabajando
	private String usuario; //  Es el usuario que esta loggeado en la app
	private String details; // Es el detalle despues de una llamada exitosa a la fachada
	
	public List<Alumno> getAlumnosList() {
		return alumnosList;
	}
	public void setAlumnosList(List<Alumno> alumnosList) {
		this.alumnosList = alumnosList;
	}
	public Alumno getAlumno() {
		return alumno;
	}
	public void setAlumno(Alumno alumno) {
		this.alumno = alumno;
	}
	public String getNombres() {
		return nombres;
	}
	public void setNombres(String nombres) {
		this.nombres = nombres;
	}
	public String getApellidoPaterno() {
		return apellidoPaterno;
	}
	public void setApellidoPaterno(String apellidoPaterno) {
		this.apellidoPaterno = apellidoPaterno;
	}
	public String getApellidoMaterno() {
		return apellidoMaterno;
	}
	public void setApellidoMaterno(String apellidoMaterno) {
		this.apellidoMaterno = apellidoMaterno;
	}
	public int getMatricula() {
		return matricula;
	}
	public void setMatricula(int matricula) {
		this.matricula = matricula;
	}
	
	@PostConstruct
	public void init() {
		loginBean = this.obtenerBean("loginBean");
		usuario = loginBean.getUsuario() != null ? loginBean.getUsuario().getUsuario() : "DESARROLLO";
	}
	
	public List<Alumno> listarAlumnos() {
		List<Alumno> alumnos = new ArrayList<Alumno>();
		try {
			
		
		Comparator<Alumno> comp = (Alumno a, Alumno b) -> {
			Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
			Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
			return fechaB.compareTo(fechaA);
		};
		Collections.sort(alumnos, comp);
		} catch(Exception e) {
			error = "No se pudieron listar los "+ summary;
			LOG.error(error, e);
			agregarMensajeError(summary, error);
		}
		return alumnos;
	}
	
	public void agregarAlumno() {
		Alumno nuevo = new Alumno();
		nuevo.setNombres(nombres);
		nuevo.setApellidoPaterno(apellidoPaterno);
		nuevo.setApellidoMaterno(apellidoMaterno);
		nuevo.setCreadoPor(usuario);
		try {
			int matricula = alumnoFacade.guardarAlumno(nuevo);
			nuevo.setMatricula(matricula);
			details = "Se ha agregado correctamente el alumno "+this.getNombreFormateado(nuevo);
			agregarMensajeInfo(summary, details);
		} catch (Exception e) {
			error = "No se pudo agregar el alumno "+this.getNombreCompleto(nuevo);
			LOG.error(summary, e);
			agregarMensajeError(summary, error);
		}
		reload();
		
	}
	
	public void reload() {
		alumnosList.clear();
		alumnosList = listarAlumnos();
		nombres = "";
		apellidoPaterno = "";
		apellidoMaterno = "";
		matricula = 0;
		alumno = null;
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('tablaAlumnos').clearFilters()");
	}
	
	public void modificarAlumno() {
		Alumno modificar = alumno;
		modificar.setNombres(nombres);
		modificar.setApellidoPaterno(apellidoPaterno);
		modificar.setApellidoMaterno(apellidoMaterno);
		modificar.setModificadoPor(usuario);
		try {
			alumnoFacade.modificarAlumno(modificar);
			details = "Se ha modificado correctamente el alumno "+this.getNombreFormateado(modificar);
			agregarMensajeInfo(summary, details);
		} catch (Exception e) {
			error = "No se pudo modificar el alumno "+this.getNombreCompleto(modificar);
			LOG.error(error, e);
			agregarMensajeError(summary, error);
		}
		reload();
	}
	
	public void eliminarAlumno() {
		Alumno tmp = alumno;
		try {
			alumnoFacade.eliminarAlumno(tmp);
			details = "Se ha eliminado al ex-alumno: "+this.getNombreFormateado(tmp);
			agregarMensajeInfo(summary, details);
		} catch(Exception e) {
			error = "No se pudo eliminar al alumno "+this.getNombreCompleto(tmp);
			LOG.error(error, e);
			agregarMensajeError(summary, error);
		}
		reload();
	}
	
	/* todo puño el qe hizo el manual */
	public String getNombreCompleto(Alumno al) {
		return al.getNombres()+" "+al.getApellidoPaterno()+" "+al.getApellidoMaterno();
	}
	
	public String getNombreFormateado(Alumno alumno) {
		return formatoMatricula.format(alumno.getMatricula()+" - "+getNombreCompleto(alumno));
	}
	
	
}



















