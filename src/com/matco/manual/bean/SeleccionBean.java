package com.matco.manual.bean;

import java.util.List;
import com.matco.manual.entity.Alumno;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean(name = "seleccionBean")
@SessionScoped
public class SeleccionBean {
	
	private List<Alumno> alumnosListFiltrada;
	
	//private List<Alumno> alumnosListFiltrada2;
	
	public List<Alumno> getAlumnosListFiltrada() {
		return alumnosListFiltrada;
	}

	public void setAlumnosListFiltrada(List<Alumno> alumnosListFiltrada) {
		this.alumnosListFiltrada = alumnosListFiltrada;
	}
	
	/*
	public List<Alumno> getAlumnosListFiltrada2() {
		return alumnosListFiltrada2;
	}

	public void setAlumnosListFiltrada2(List<Alumno> alumnosListFiltrada2) {
		this.alumnosListFiltrada2 = alumnosListFiltrada2;
	}
	*/

	
	
}

