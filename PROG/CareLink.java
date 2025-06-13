import java.util.Scanner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CareLink{
	public static void main(String[] args) {

		//CARGA DEL DRIVER DE LA BASE DE DATOS
		try {
		    Class.forName("org.mariadb.jdbc.Driver"); 
		} catch (ClassNotFoundException e) {
		    System.out.println("Driver MariaDB no encontrado");
		    e.printStackTrace();
		    return;
		}

		Scanner src = new Scanner(System.in);
		int sesion=-1;

			
		while(true){
			
			Usuario UsuarioActivo = null;


			while(UsuarioActivo==null){

				System.out.println("\n ---- Menu ---- \n1.Inicio de Sesion \n2.Registrar Usuario \n0.Salir");
				sesion=src.nextInt();
				src.nextLine();

				//	MENU DE INICIO DE SESION O REGISTRO DE USUARIO EN BASE DE DATOS
				switch(sesion){
					case 1 : 
						UsuarioActivo = inicioSesion(src);
						if (UsuarioActivo != null)
							UsuarioActivo.mostrarInfo();
						break;
					case 2 : 
						registroUsuario(src);
						break;
					case 0 : 
						System.out.println("Saliendo del programa");
						System.exit(0);
					default :
	                	System.out.println("Opción inválida.");
				}
			}

			boolean cerrarSesion = UsuarioActivo.mostrarMenu(src);
			if (cerrarSesion) {
				System.out.println("Cerrando Sesion");
			}
		}
	}


	// FUNCION DE INICIO DE SESION
	public static Usuario inicioSesion(Scanner src){

		String correo;
		String password;
		int usr;
		String rol;
		int sl = 1;

		while(sl!=0){
			System.out.println("\n----Inicio de Sesion----\n");
			System.out.println("1.Empezar \n0.Volver ");
			sl = src.nextInt();
			src.nextLine();
			
			if (sl == 1) {
				System.out.print("1.Medico  o 2.Usuario (Introduce 1 o 2 ) : ");
				usr = src.nextInt();
				src.nextLine();

				if (usr == 1)
					rol ="MEDICO";
				else 
					rol= "USUARIO";

				System.out.print("Correo : ");
				correo = src.nextLine();

				System.out.print("Contraseña : ");
				password = src.nextLine();

				try(Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")){
				
				    // SENTENCIA SQL PARA INSERTAR PARAMETROS
					String sql = "SELECT * FROM usuarios WHERE tipo_usuario = ? AND email = ? AND password = ?";
					
				    // PREPARA LAS SENTENCIAS A INTRODUCIR 
					PreparedStatement stmt = conn.prepareStatement(sql);
					    
				   	stmt.setString(1, rol);
				   	stmt.setString(2, correo);
				   	stmt.setString(3, password);

		    		ResultSet rs = stmt.executeQuery();

		    		if (rs.next()) {
		    			int id = rs.getInt("id");
		    			int edad = rs.getInt("edad");
		    			String nombre = rs.getString("nombre");
		    			String especialidad = rs.getString("especialidad");
		    			String planStr = rs.getString("plan");

		    			if ("MEDICO".equals(rol)){
			    			System.out.println("Inicio de Sesion Correcto");
		    				return new Medico(id,nombre,edad,correo,password,especialidad);
		    			}else{
			    			System.out.println("\n----Inicio de Sesion Correcto----\n");
		    				Paciente.Plan planEnum = Paciente.Plan.valueOf(planStr);
		    				return new Paciente(id,nombre,edad,correo,password,planEnum);
		    			}
		    		}
		    		else
		    			System.out.println("\n ----Usuario o contraseña Incorrecta----\n");

				} catch(SQLException e) {
		    		e.printStackTrace();
				}
			}else
				sl = 0;
		}
		return null;
	}


	// FUNCION REGISTRO DE USUARIO EN BASE DE DATOS
    public static void registroUsuario(Scanner src) {

    String nombre;
    String password;
    int edad;
    String correo;
    String rol;
    int pl;
    String plan;
    int usr;
    String especialidad;

    int sl = 1;

    while (sl != 0) {

        System.out.println("\nRegistro de Usuario : \n -----------------------");
        System.out.println("1.Empezar \n0.Volver");
        sl = src.nextInt();
        src.nextLine();

        if (sl == 1) {

            // USUARIO INTRODUCE SUS DATOS
            System.out.print("Introduce Nombre: ");
            nombre = src.nextLine();

            System.out.print("Introduce Edad: ");
            edad = src.nextInt();
            src.nextLine();

            // Validar edad
            if (edad < 18 || edad > 120) {
                System.out.println(" Edad no válida. Debe estar entre 18 y 120 años.");
                continue;
            }

            System.out.print("Introduce Correo Electrónico: ");
            correo = src.nextLine();

            // Validar correo
            if (!correo.toLowerCase().endsWith("@gmail.com")) {
                System.out.println(" Correo inválido. Debe ser una cuenta @gmail.com.");
                continue;
            }

            System.out.print("Introduce Contraseña: ");
            password = src.nextLine();

            System.out.println("Marque 1 - Médico o 2 - Usuario (Introduce 1 o 2): ");
            usr = src.nextInt();
            src.nextLine();

            if (usr == 2) {
                rol = "USUARIO";
                especialidad = null;

                System.out.println("Elige plan 1 - Gratis o 2 - Premium (Introduce 1 o 2): ");
                pl = src.nextInt();
                src.nextLine();

                if (pl == 2) {
                    plan = "PREMIUM";
                } else {
                    plan = "GRATIS";
                }
            } else {
                rol = "MEDICO";
                plan = null;
                System.out.print("Introduce tu especialidad: ");
                especialidad = src.nextLine();
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

                String checkSql = "SELECT COUNT(*) FROM usuarios WHERE email = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, correo);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                if (count > 0) {
                    System.out.println(" Ya existe un usuario con ese correo electrónico.");
                    continue;
                }

                String sql = "INSERT INTO usuarios (nombre, edad, email, password, plan, tipo_usuario, especialidad) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

                stmt.setString(1, nombre);
                stmt.setInt(2, edad);
                stmt.setString(3, correo);
                stmt.setString(4, password);
                stmt.setString(5, plan);
                stmt.setString(6, rol);
                stmt.setString(7, especialidad);

                int filas = stmt.executeUpdate();

                if (filas > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int idGenerado = generatedKeys.getInt(1);
                        System.out.println(" Usuario registrado con ID: " + idGenerado);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            sl = 0;
        }
    }
}
}


//CLASE BASE USUARIO QUE TIENE METODOS ABSTRACTOS
abstract class Usuario{

	protected int id;
	protected String nombre;
	protected int edad;
	protected String email;
	protected String password;

	public Usuario(int id , String nombre , int edad , String email , String password ){
		this.id = id;
		this.nombre = nombre;
		this.edad = edad;
		this.email = email;
		this.password = password;
	}

	public abstract void mostrarInfo();

	public abstract boolean mostrarMenu(Scanner src);

}

//CLASE QUE HEREDA DE USUARIO, APLICANDO SUS METODOS
class Medico extends Usuario{

	protected String especialidad;

	public Medico(int id , String nombre , int edad , String email , String password  , String especialidad){
		
		super(id ,  nombre ,  edad ,  email ,  password );
		this.especialidad = especialidad;

	}

	public void mostrarInfo(){
		System.out.println(" \n Medico : " + nombre + " , id : " + id + " , email : " + email + " , especialidad: " + especialidad + "\n -------------------------------------");
	}

	public boolean mostrarMenu(Scanner src){

		int opcion;

		System.out.println("\n ----Menú de Médico---- "+ this.nombre +"\n\n1.Ver Pacientes \n2.Crear Receta \n3.Historial de Salud \n4.Actividades\n0.CerrarSesion \n" );
		opcion = src.nextInt();
		src.nextLine();

		while(opcion!=0){
			switch(opcion){
				case 1 :
					verPacientes(); break;
				case 2 :
					Receta(src);break;
				case 3 :
					HistoralSalud(src);break;
				case 4 :
					Actividades(src);break;
				case 0 : 
					return true;
				default :
                	System.out.println("Opción inválida.");
			}
		
		System.out.println("\n ----Menú de Médico---- "+ this.nombre +"\n\n1.Ver Pacientes \n2.Crear Receta \n3.Historial de Salud \n4.Actividades\n0.CerrarSesion \n" );
		opcion = src.nextInt();
		src.nextLine();

		}

		return false;
	}

	public void Actividades(Scanner src){
		int op1;
		int id_paciente;

        id_paciente = identificaUsr(src);

        if (id_paciente == -1)
			return;

		System.out.println("\n1.Ver Tareas \n2.Nueva Actividad \n0.Salir");
		op1 = src.nextInt();
		src.nextLine();

		while(op1!= 0){

			switch(op1){
				case 1 :
					verActividades(id_paciente);
					break;
				case 2 :
					asignarActividad(src,id_paciente);
					break;
			}

			System.out.println("\n1.Ver Recetas \n2.Nueva Receta \n0.Salir ");
			op1 = src.nextInt();
			src.nextLine();
		}
	}
	
	public void verActividades(int idUsuario) {
	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {
	        
	        String sqlTareas = "SELECT * FROM tareas WHERE usuario_id = ? ORDER BY fecha_programada ASC";
	        PreparedStatement stmtTareas = conn.prepareStatement(sqlTareas);
	        stmtTareas.setInt(1, idUsuario);
	        
	        ResultSet rsTareas = stmtTareas.executeQuery();
	        
	        System.out.println("\n ---- Tareas asignadas ---- \n");
	        boolean hayTareas = false;

	        while (rsTareas.next()) {
	            hayTareas = true;
	            int idTarea = rsTareas.getInt("id");
	            String descripcion = rsTareas.getString("descripcion");
	            boolean completada = rsTareas.getBoolean("completada");
	            Date fecha = rsTareas.getDate("fecha_programada");

	            System.out.println("ID Tarea: " + idTarea);
	            System.out.println("Descripción: " + descripcion);
	            System.out.println("Fecha: " + fecha);
	            System.out.println("Completada: " + (completada ? "Sí" : "No"));
	            System.out.println("------------------------------------");
	        }

	        if (!hayTareas) {
	            System.out.println("No hay tareas asignadas a este usuario.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public int identificaUsr(Scanner src){
		String emailPac;
	    int idPaciente = -1;

	    System.out.println("Introduce el email del Paciente:");
	    emailPac = src.nextLine();

	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	        String sqlBuscar = "SELECT id FROM usuarios WHERE email = ? AND tipo_usuario = ?";
	        PreparedStatement stmtBuscar = conn.prepareStatement(sqlBuscar);

	        stmtBuscar.setString(1, emailPac);
	        stmtBuscar.setString(2, "USUARIO");
	        ResultSet rs = stmtBuscar.executeQuery();

	        if (rs.next()) 
	            idPaciente = rs.getInt("id");
	         else 
	            System.out.println(" No se encontró al paciente.");

	    } catch (SQLException e) {
	        e.printStackTrace();
		}

       	return idPaciente;
	}

	public void asignarActividad(Scanner src, int idPaciente) {

	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

            System.out.print("Introduce la descripción de la tarea: ");
            String descripcion = src.nextLine();

            System.out.print("Introduce la fecha programada (YYYY-MM-DD): ");
            String fechaStr = src.nextLine();

            String sqlInsert = "INSERT INTO tareas (usuario_id, descripcion, fecha_programada) VALUES (?, ?, ?)";

            PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);
            stmtInsert.setInt(1, idPaciente);
            stmtInsert.setString(2, descripcion);
            stmtInsert.setDate(3, java.sql.Date.valueOf(fechaStr));

            int filas = stmtInsert.executeUpdate();
            if (filas > 0) {
                System.out.println(" \n -- Tarea asignada correctamente al paciente. --");
            } else {
                System.out.println(" -- No se pudo asignar la tarea. -- ");
            }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    } catch (IllegalArgumentException e) {
	        System.out.println(" -- Formato de fecha inválido. Usa YYYY-MM-DD. -- ");
	    }
	}

	public void verPacientes(){

		try(Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")){	
			
			String sqlPacientes = "SELECT * FROM usuarios WHERE tipo_usuario = ?";
			
		    // PREPARA LAS SENTENCIAS A INTRODUCIR Y HACER QUE DEVUELVA LAS CLAVES ID GENERADAS
			PreparedStatement stmtPacientes = conn.prepareStatement(sqlPacientes);

			stmtPacientes.setString(1,"USUARIO");

			ResultSet rsPacientes = stmtPacientes.executeQuery();
			    
		   	System.out.println("\n ---- Lista de Pacientes ---- \n");

			while (rsPacientes.next()) {

			    int id_pac = rsPacientes.getInt("id");
			    String nom = rsPacientes.getString("nombre");
			    int ed = rsPacientes.getInt("edad");
			    String crr = rsPacientes.getString("email");
			    String pl = rsPacientes.getString("plan");

			    System.out.println("Nombre: " + nom + " , Id : " +id_pac);
			    System.out.println("Edad: " + ed + " , Plan: " + pl);
   			    System.out.println("Correo: " + crr);
			    System.out.println("------------------------------------");
			}

		} catch(SQLException e) {
    		e.printStackTrace();
		}
	}

	public void Receta(Scanner src){
		int op1;
		int id_paciente;

		id_paciente = identificaUsr(src);

        if (id_paciente == -1)
			return;
 
		System.out.println("\n1.Ver Recetas \n2.Nueva Receta \n0.Salir");
		op1 = src.nextInt();
		src.nextLine();

		while(op1!= 0){

			switch(op1){
				case 1 :
					verRecetas(id_paciente);
					break;
				case 2 :
					nuevaReceta(src,id_paciente);
					break;
			}

			System.out.println("\n1.Ver Recetas \n2.Nueva Receta \n0.Salir ");
			op1 = src.nextInt();
			src.nextLine();
		}
	}	

	public void verRecetas(int id_paciente) {
	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	        String sql = """
	            SELECT r.id AS receta_id, u.nombre AS paciente_nombre, u.edad, u.email, u.plan, r.descripcion, r.fecha
	            FROM recetas r
	            JOIN usuarios u ON r.paciente_id = u.id
	            WHERE u.tipo_usuario = 'USUARIO' AND u.id = ?
	        """;

	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, id_paciente);
	        ResultSet rs = stmt.executeQuery();

	        System.out.println("\n ---- Lista de Recetas para el Paciente ---- \n");

	        while (rs.next()) {
	            int recetaId = rs.getInt("receta_id");
	            String nombre = rs.getString("paciente_nombre");
	            int edad = rs.getInt("edad");
	            String email = rs.getString("email");
	            String plan = rs.getString("plan");
	            String descripcion = rs.getString("descripcion");
	            Date fecha = rs.getDate("fecha");

	            System.out.println(" Receta ID: " + recetaId);
	            System.out.println(" Paciente: " + nombre + " | Edad: " + edad + " | Plan: " + plan);
	            System.out.println(" Correo: " + email);
	            System.out.println(" Descripción: " + descripcion);
	            System.out.println(" Fecha: " + fecha);
	            System.out.println("--------------------------------------------");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void nuevaReceta(Scanner src, int pacienteId) {
	    System.out.print("Introduce el nombre de la receta: ");
	    String nombreReceta = src.nextLine();

	    System.out.print("Introduce la descripción de la receta: ");
	    String descripcionReceta = src.nextLine();

	    String descripcionCompleta = "Nombre: " + nombreReceta + " | Detalles: " + descripcionReceta;

	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	        String sql = "INSERT INTO recetas (medico_id, paciente_id, descripcion, fecha) VALUES (?, ?, ?, NOW())";

	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, this.id); // ID del médico
	        stmt.setInt(2, pacienteId);
	        stmt.setString(3, descripcionCompleta);

	        int filasAfectadas = stmt.executeUpdate();

	        if (filasAfectadas > 0) {
	            System.out.println(" Receta creada correctamente.");
	        } else {
	            System.out.println(" No se pudo crear la receta.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void HistoralSalud(Scanner src){

		int op1;
		String email_Pac;
		int id_paciente;


		id_paciente = identificaUsr(src);

        if (id_paciente == -1)
			return;

		System.out.println("\n1.Ver Historial de Salud \n2.Nuevo Registro de Salud");
		op1 = src.nextInt();
		src.nextLine();

		while(op1!= 0){

			switch(op1){
				case 1 :
					verHistorial(id_paciente);
					break;
				case 2 :
					actualizarHistorialSalud(src,id_paciente);
					break;
			}

			System.out.println("\n1.Ver Historial de Salud \n2.Nuevo Registro de Salud \n0.Volver");
			op1 = src.nextInt();
			src.nextLine();
		}
	}	

	public void verHistorial(int id_paciente){

		try(Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")){	
			
			String sqlHistorial = """
            SELECT hs.fecha, hs.descripcion, u.nombre AS nombre_medico
            FROM historial_salud hs
            JOIN usuarios u ON hs.medico_id = u.id
            WHERE hs.usuario_id = ?
            ORDER BY hs.fecha DESC
        	""";
			
		    // PREPARA LAS SENTENCIAS A INTRODUCIR Y HACER QUE DEVUELVA LAS CLAVES ID GENERADAS
			PreparedStatement stmtHistorial = conn.prepareStatement(sqlHistorial);

			stmtHistorial.setInt(1,id_paciente);

			ResultSet rsHistorial = stmtHistorial.executeQuery();
			    
		   	System.out.println("\n ---- Historial del paciente ---- \n");

			boolean hayRegistros = false;

			while (rsHistorial.next()) {
			    hayRegistros = true;

			    String fecha = rsHistorial.getString("fecha");
			    String descripcion = rsHistorial.getString("descripcion");
			    String medico = rsHistorial.getString("nombre_medico");

			    System.out.println("Fecha: " + fecha);
			    System.out.println("Médico: " + medico);
			    System.out.println("Descripción: " + descripcion);
			    System.out.println("------------------------------------");
			}

			if (!hayRegistros) {
			    System.out.println("El paciente no tiene historial médico registrado.");
			}

		} catch(SQLException e) {
    		e.printStackTrace();
		}
	}

	public void actualizarHistorialSalud(Scanner src,int id_paciente){

		String desc_expediente;

		System.out.println("Introduce la descripcion de salud del usuario");
		desc_expediente=src.nextLine();
		
		try(Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")){
				
		    // SENTENCIA SQL PARA INSERTAR PARAMETROS
			String sql = "INSERT INTO historial_salud (usuario_id, medico_id, fecha, descripcion) VALUES (?, ?, NOW(), ?)";
			
		    // PREPARA LAS SENTENCIAS A INTRODUCIR Y HACER QUE DEVUELVA LAS CLAVES ID GENERADAS
			PreparedStatement stmt = conn.prepareStatement(sql);
			    
			//ASIGNAMOS LOS VALORES
		   	stmt.setInt(1,id_paciente);
		   	stmt.setInt(2,this.id);
		   	stmt.setString(3,desc_expediente);

			//HACEMOS LA INSERCION EN LA BASE DE DATOS
			int filas = stmt.executeUpdate();

			if (filas > 0)
				System.out.println("Historial actualizado correctamente");
			else
				System.out.println("No se puedo actualizar el Historial de salud");

		} catch(SQLException e) {
    		e.printStackTrace();
		}
	}
}


//CLASE QUE HEREDA DE USUARIO, APLICANDO SUS METODOS
class Paciente extends Usuario{

	public enum Plan{GRATIS , PREMIUM}

	protected Plan plan;

	public Paciente(int id , String nombre , int edad , String email , String password , Plan plan){

		super(id ,  nombre ,  edad ,  email ,  password );
		this.plan = plan;		
	}

	public void mostrarInfo(){
		System.out.println("  \nPaciente : " + nombre + " , id : " + id + " , email : " + email + " , Plan : " + plan + "\n ------------------------------------- ");
	}

	public boolean mostrarMenu(Scanner src){
	
		int opcion;

		System.out.println( " Menú de Paciente : "+ this.nombre +" \n1.Llamar a familiar \n2.Chatear \n3.Ver Tareas \n4.Ver Salud \n5.CerrarSesion " );
		opcion = src.nextInt();
		src.nextLine();

		while(opcion!=0){
			switch(opcion){
				case 1 :
					llamaFamilia(); break;
				case 2 :
					chat();break;
				case 3 :
					verTareas();break;
				case 4 : 
					informacionSalud();break;
				case 5 : 
					return true;
				default :
                	System.out.println("Opción inválida.");
			}
	
		System.out.println( " Menú de Paciente : "+ this.nombre +" \n1.Llamar a familiar \n2.Chatear \n3.Ver Tareas \n4.Ver Salud \n5.CerrarSesion " );
		opcion = src.nextInt();
		src.nextLine();

		}

		return false;
	}

	public void llamaFamilia() {
		
	    System.out.println("\n- Llamando a Familiar");

	    try {
	        Thread.sleep(1000);
	        System.out.println("\n      (---)\n");
	        Thread.sleep(1000);
	        System.out.println("- Llamada Fallida\n");

	        try (Connection conn = DriverManager.getConnection(
	                "jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	            // Registrar llamada fallida
	            String sqlInsert = "INSERT INTO llamadas (emisor_id, receptor_id, fecha, duracion, tipo) VALUES (?, ?, NOW(), ?, ?)";
	            PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);

	            stmtInsert.setInt(1, this.id);
	            stmtInsert.setInt(2, this.id);
	            stmtInsert.setInt(3, 0);
	            stmtInsert.setString(4, "normal");
	            stmtInsert.executeUpdate();

	            // Mostrar historial de llamadas
	            System.out.println("\n--- Historial de Llamadas ---\n");

	            String sqlSelect = "SELECT id, emisor_id, receptor_id, fecha, duracion, tipo FROM llamadas WHERE emisor_id = ? ORDER BY fecha DESC";
	            PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect);
	            stmtSelect.setInt(1, this.id);

	            ResultSet rs = stmtSelect.executeQuery();

	            while (rs.next()) {
	                int idLlamada = rs.getInt("id");
	                int emisor = rs.getInt("emisor_id");
	                int receptor = rs.getInt("receptor_id");
	                String fecha = rs.getString("fecha");
	                int duracion = rs.getInt("duracion");
	                String tipo = rs.getString("tipo");

	                System.out.println("ID Llamada: " + idLlamada);
	                System.out.println("Emisor ID: " + emisor);
	                System.out.println("Receptor ID: " + receptor);
	                System.out.println("Fecha: " + fecha);
	                System.out.println("Duración: " + duracion + " segundos");
	                System.out.println("Tipo: " + tipo);
	                System.out.println("-------------------------------");
	            }

	        } catch (SQLException e) {
	            System.err.println("Error con la base de datos:");
	            e.printStackTrace();
	        }

	    } catch (InterruptedException e) {
	        System.err.println("Error en el hilo de espera:");
	        e.printStackTrace();
	    }
	}

	public void chat() {
    System.out.println("\n╔════════════════════════════════════════════╗");
    System.out.println("║         Chat con Juan (Amigo)              ║");
    System.out.println("╚════════════════════════════════════════════╝");

    System.out.println("\n[Yo]                                      [Juan]");

    try {
        Thread.sleep(1000);
        System.out.println("\n✔️✔️ Enviado: ¿Cómo estás, Juan?");
        Thread.sleep(1000);
        System.out.println("                                (escribiendo...)");
        Thread.sleep(1000);
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           Fin del chat simulado            ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
    } catch (InterruptedException e) {
        System.err.println("Error al simular el chat.");
    }
	}

	public void informacionSalud() {

	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	        String sql =
	            "SELECT h.fecha AS fecha, h.descripcion AS descripcion, u.nombre AS medico, 'Historial' AS tipo " +
	            "FROM historial_salud h " +
	            "JOIN usuarios u ON h.medico_id = u.id " +
	            "WHERE h.usuario_id = ? " +
	            "UNION " +
	            "SELECT r.fecha AS fecha, r.descripcion AS descripcion, u.nombre AS medico, 'Receta' AS tipo " +
	            "FROM recetas r " +
	            "JOIN usuarios u ON r.medico_id = u.id " +
	            "WHERE r.paciente_id = ? " +
	            "ORDER BY fecha DESC";

	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, this.id);
	        stmt.setInt(2, this.id);

	        ResultSet rs = stmt.executeQuery();

	        System.out.println("\n--- Tu Historial Médico y Recetas --- "+this.email+ "\n ");

	        while (rs.next()) {
	            String fecha = rs.getString("fecha");
	            String desc = rs.getString("descripcion");
	            String medico = rs.getString("medico");
	            String tipo = rs.getString("tipo");

	            System.out.println("[" + tipo + "] - Fecha: " + fecha);
	            System.out.println("Médico: " + medico);
	            System.out.println("Descripción: " + desc);
	            System.out.println("----------------------------------------\n");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	public void verTareas() {
	    System.out.println("\n-- Tareas asignadas --");

	    try (Connection conn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/CareLink", "1DAM", "1DAM")) {

	        String sql = "SELECT descripcion, fecha_programada, completada FROM tareas WHERE usuario_id = ?";
	        PreparedStatement stmt = conn.prepareStatement(sql);
	        stmt.setInt(1, this.id); // Usamos el ID del paciente actual

	        ResultSet rs = stmt.executeQuery();

	        boolean hayTareas = false;
	        while (rs.next()) {
	            hayTareas = true;
	            String descripcion = rs.getString("descripcion");
	            Date fecha = rs.getDate("fecha_programada");
	            boolean completada = rs.getBoolean("completada");

	            System.out.println("\nDescripción: " + descripcion);
	            System.out.println("Fecha Programada: " + fecha);
	            System.out.println("Completada: " + (completada ? "Sí " : "No "));
	            System.out.println("------------------------------");
	        }

	        if (!hayTareas) {
	            System.out.println("No hay tareas asignadas.");
	        }

	    } catch (SQLException e) {
	        System.out.println("Error al recuperar las tareas.");
	        e.printStackTrace();
	    }
	}
}




