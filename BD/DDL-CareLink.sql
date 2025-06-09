CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100),
    edad INT,
    email VARCHAR(100) UNIQUE,
    password VARCHAR(255),
    plan ENUM('GRATIS', 'PREMIUM'),
    tipo_usuario ENUM('USUARIO', 'MEDICO') NOT NULL,
    especialidad VARCHAR(100)
);

CREATE TABLE llamadas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    emisor_id INT NOT NULL,
    receptor_nombre VARCHAR(100) NOT NULL,
    fecha DATETIME NOT NULL,
    duracion INT NOT NULL, 
    tipo ENUM('normal', 'emergencia') DEFAULT 'normal',
    FOREIGN KEY (emisor_id) REFERENCES usuarios(id)
);


CREATE TABLE tareas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    descripcion TEXT NOT NULL,
    completada BOOLEAN DEFAULT FALSE,
    fecha_programada DATE NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE historial_salud (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    medico_id INT NOT NULL,
    fecha DATE NOT NULL,
    descripcion TEXT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    FOREIGN KEY (medico_id) REFERENCES usuarios(id)
);

CREATE TABLE recetas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    medico_id INT NOT NULL,
    paciente_id INT NOT NULL,
    descripcion TEXT NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    FOREIGN KEY (medico_id) REFERENCES usuarios(id),
    FOREIGN KEY (paciente_id) REFERENCES usuarios(id)
);



