package com.matco.manual.bean;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.matco.controlaccesos.service.ControlAccesosEndpoint;
import com.matco.controlaccesos.service.ControlAccesosService;
import com.matco.controlaccesos.service.Exception_Exception;
import com.matco.controlaccesos.service.Usuario;

/**
 * Se encarga de autenticar al usuario y cargar sus permisos en caso de tener
 * acceso a la aplicación En caso de no tener acceso se muestra un mensaje de
 * error
 */
@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean extends GenericBean implements Serializable {

	private static final long serialVersionUID = 7427723168362309690L;
	private static final Logger log = Logger.getLogger(LoginBean.class);
	private String leyendaUsuario;
	private String leyendaLogOut;
	private String username;
	private String password;
	private String nombreUsuario;
	private String correoElectronicoUsuario;
	private String rol;
	private String rolOracion;
	private String sistema;
	private String modulo;
	private Usuario usuario;
	private ControlAccesosEndpoint controlAccesosPort;

	private List<String> rolesPermitidos;

	public LoginBean() {
//		rolesPermitidos = obtenerRolesPermitidos();
	}

	/**
	 * Obtiene los roles que tienen acceso al sistema.
	 * 
	 * @return Roles permitidos
	 */
	@SuppressWarnings("unused")
	private List<String> obtenerRolesPermitidos() {
		ResourceBundle recursosRoles = obtenerResourceBundleRoles();

		if (recursosRoles == null) {
			return null;
		}

		List<String> roles = obtenerPropiedadDeRecurso(recursosRoles, "roles");
		return roles;
	}

	/**
	 * Obtiene la propiedad solicitada del archivo de recursos especificado. Se
	 * recupera un listado con los valores encontrados, los mismos se encuentran
	 * separados por una barra vertical (|).
	 * 
	 * @param bundle
	 *            Archivo de recursos
	 * @param propiedad
	 *            Campo o valor solicitado
	 * @return Lista con los valores encontrados
	 */
	private List<String> obtenerPropiedadDeRecurso(ResourceBundle bundle, String propiedad) {
		String atributo = bundle.getString(propiedad);
		String[] valores = atributo.split("\\|");

		List<String> listaValores = new ArrayList<String>();

		for (String valor : valores) {
			listaValores.add(valor);
		}

		return listaValores;
	}

	/**
	 * Obtiene el archivo de recursos con los roles permitidos en la aplicación.
	 * El archivo se busca en el directorio de trabajo.
	 * 
	 * @return Recursos que contiene los roles de usuario
	 */
	private ResourceBundle obtenerResourceBundleRoles() {
		FileInputStream inputStream = null;
		ResourceBundle recursos = null;
		try {
			inputStream = new FileInputStream("roles_amce.properties");
			recursos = new PropertyResourceBundle(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return recursos;
	}

	/**
	 * Valida los datos del usuario y si tiene acceso al sistema regresa un
	 * <b>outcome</b> para redireccionar a la página principal. Si los datos
	 * ingresados no corresponden a un usuario válido, se vuelve a solicitar el
	 * usuario y contraseña.
	 * 
	 * @return {@link String} <b>outcome</b> para una regla de navegación
	 */
	public void login() {
		if (tienePermisoUsuario()) {
			this.leyendaUsuario = nombreUsuario + " (" + rol + "), Bienvenido al Sistema ";
			this.leyendaLogOut = "[Cerrar sesión]";
			redireccionar("muestrasSos");
		}
		else {
			redireccionar("no");
		}
	}

	/**
	 * Termina la sesión del usuario y regresa un <b>outcome</b> para
	 * redireccionar a la página de login.
	 * 
	 * @return {@link String} <b>outcome</b> para una regla de navegación
	 */
	public String logout() {
		getRequest().getSession().invalidate();
		this.usuario = null;
		return "logout";
	}

	/**
	 * Determina si el usuario que intenta iniciar sesión cuenta con permisos
	 * suficientes para ingresar al sistema.
	 * 
	 * @return <b>true</b> si el usuario tiene acceso al sistema y <b>false</b>
	 *         en caso contrario
	 */
	private boolean tienePermisoUsuario() {
		String urlServidor = getFacesContext().getExternalContext().getInitParameter("controlaccesos_wshost");
		String puertoServidor = getFacesContext().getExternalContext().getInitParameter("controlaccesos_wsport");
		String servidor = "http://" + urlServidor + ":" + puertoServidor
				+ "/controlaccesosservices/controlaccesos?wsdl";
		boolean existeUsuario = false;

		try {
			URL url = new URL(servidor);
			ControlAccesosService controlAccesos = new ControlAccesosService(url);
			controlAccesosPort = controlAccesos.getControlAccesosServicePort();

			existeUsuario = controlAccesosPort.esUsuarioContrasenaValido(username.trim(), password.trim());

			if (existeUsuario) {
				sistema = getFacesContext().getExternalContext().getInitParameter("sistema");
				modulo = getFacesContext().getExternalContext().getInitParameter("modulo");
				usuario = controlAccesosPort.obtenerDatosUsuario(username, password, sistema, modulo);
				rol = usuario.getRol().getIdRolUsuario();
				correoElectronicoUsuario = usuario.getEmail();
				boolean permitido = (rol != null);

				if (!permitido) {

					agregarMensajeError("No tiene permisos para ingresar");
				} else {
					setRolOracion(rol.substring(0, 1).toUpperCase() + rol.substring(1, rol.length()).toLowerCase());
				}
				return permitido;
			} else {
				agregarMensajeError("Usuario o contraseña no válidos");
			}
		} catch (Exception_Exception e) {
			String mensajeError = e.getMessage();
			log.error(mensajeError, e);
			agregarMensajeError("Error al iniciar sesión");
		} catch (MalformedURLException e) {
			String mensajeError = "Error al intentar validar usuario";
			log.error(mensajeError, e);
			agregarMensajeError(mensajeError);
		}
		return existeUsuario;
	}

	public void onLoadRedirigirLogin() {
		if (usuario != null && usuario.getUsuario() != null && usuario.getContrasena() != null
				&& usuario.getRol() != null && usuario.getRol().getIdRolUsuario() != null) {
			redireccionar("inicio");
		}
	}

	/**
	 * Return the environment-specific object instance for the current request.
	 * 
	 * @return {@link HttpServletRequest} Solicitud actual
	 */
	private HttpServletRequest getRequest() {
		FacesContext context = getFacesContext();
		ExternalContext extContext = context.getExternalContext();
		HttpServletRequest request = (HttpServletRequest) extContext.getRequest();
		return request;
	}

	/**
	 * Se sobre escribe el método toString para que regrese el String de la
	 * clase con el usuario en sesión
	 */
	@Override
	public String toString() {
		String className = super.toString();
		String usernameValue = (username != null) ? " [username=" + username + "]" : "";
		String classUser = className + usernameValue;
		return classUser;
	}

	public String getLeyendaUsuario() {
		return leyendaUsuario;
	}

	public void setLeyendaUsuario(String leyendaUsuario) {
		this.leyendaUsuario = leyendaUsuario;
	}

	public String getLeyendaLogOut() {
		return leyendaLogOut;
	}

	public void setLeyendaLogOut(String leyendaLogOut) {
		this.leyendaLogOut = leyendaLogOut;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public ControlAccesosEndpoint getControlAccesosPort() {
		return controlAccesosPort;
	}

	public void setControlAccesosPort(ControlAccesosEndpoint controlAccesosPort) {
		this.controlAccesosPort = controlAccesosPort;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getRolOracion() {
		return rolOracion;
	}

	public void setRolOracion(String rolOracion) {
		this.rolOracion = rolOracion;
	}

	public String getCorreoElectronicoUsuario() {
		return correoElectronicoUsuario;
	}

	public void setCorreoElectronicoUsuario(String correoElectronicoUsuario) {
		this.correoElectronicoUsuario = correoElectronicoUsuario;
	}

	public List<String> getRolesPermitidos() {
		return rolesPermitidos;
	}

	public void setRolesPermitidos(List<String> rolesPermitidos) {
		this.rolesPermitidos = rolesPermitidos;
	}

}
