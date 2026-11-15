-- =====================================================
-- Base de datos: publicaciones
-- Proyecto EC3 - Programación II (UCSUR)
-- =====================================================

DROP DATABASE IF EXISTS publicaciones;

CREATE DATABASE publicaciones
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE publicaciones;

-- -----------------------------------------------------
-- Tabla: sede
-- -----------------------------------------------------
CREATE TABLE sede (
    id_sede     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (id_sede),
    UNIQUE KEY uq_sede_nombre (nombre)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: categoria
-- -----------------------------------------------------
CREATE TABLE categoria (
    id_categoria INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(255) NULL,
    PRIMARY KEY (id_categoria),
    UNIQUE KEY uq_categoria_nombre (nombre)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: usuario
-- (contrasena guarda el hash BCrypt, por eso 255)
-- -----------------------------------------------------
CREATE TABLE usuario (
    id_usuario     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    apellido       VARCHAR(80)  NOT NULL,
    correo         VARCHAR(120) NOT NULL,
    contrasena     VARCHAR(255) NOT NULL,
    rol            ENUM('ESTUDIANTE','ADMINISTRADOR') NOT NULL DEFAULT 'ESTUDIANTE',
    fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_correo (correo)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: publicacion
-- -----------------------------------------------------
CREATE TABLE publicacion (
    id_publicacion    INT UNSIGNED NOT NULL AUTO_INCREMENT,
    titulo            VARCHAR(150) NOT NULL,
    descripcion       TEXT         NOT NULL,
    imagen            VARCHAR(255) NULL,
    fecha_publicacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado            ENUM('VIGENTE','ATENDIDA','FINALIZADA') NOT NULL DEFAULT 'VIGENTE',
    id_usuario        INT UNSIGNED NOT NULL,
    id_categoria      INT UNSIGNED NOT NULL,
    id_sede           INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_publicacion),
    CONSTRAINT fk_publicacion_usuario
        FOREIGN KEY (id_usuario)   REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_publicacion_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria (id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_publicacion_sede
        FOREIGN KEY (id_sede)      REFERENCES sede (id_sede)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    INDEX idx_publicacion_fecha (fecha_publicacion),
    INDEX idx_publicacion_estado (estado),
    INDEX idx_publicacion_categoria (id_categoria),
    INDEX idx_publicacion_sede (id_sede),
    FULLTEXT KEY ft_publicacion_busqueda (titulo, descripcion)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Tabla: reporte
-- -----------------------------------------------------
CREATE TABLE reporte (
    id_reporte     INT UNSIGNED NOT NULL AUTO_INCREMENT,
    motivo         VARCHAR(255) NOT NULL,
    fecha_reporte  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado         ENUM('PENDIENTE','REVISADO','DESCARTADO') NOT NULL DEFAULT 'PENDIENTE',
    id_usuario     INT UNSIGNED NOT NULL,
    id_publicacion INT UNSIGNED NOT NULL,
    PRIMARY KEY (id_reporte),
    CONSTRAINT fk_reporte_usuario
        FOREIGN KEY (id_usuario)     REFERENCES usuario (id_usuario)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_reporte_publicacion
        FOREIGN KEY (id_publicacion) REFERENCES publicacion (id_publicacion)
        ON UPDATE CASCADE ON DELETE CASCADE,
    -- un usuario no puede reportar dos veces la misma publicación
    UNIQUE KEY uq_reporte_usuario_publicacion (id_usuario, id_publicacion),
    INDEX idx_reporte_estado (estado)
) ENGINE=InnoDB;

-- -----------------------------------------------------
-- Datos iniciales
-- -----------------------------------------------------
INSERT INTO sede (nombre, descripcion) VALUES
    ('Villa',    'Sede Villa'),
    ('Norte',    'Sede Norte'),
    ('Aramburú', 'Sede Aramburú'),
    ('Ate',      'Sede Ate');

INSERT INTO categoria (nombre, descripcion) VALUES
    ('Emprendimientos',   'Negocios y proyectos emprendedores de estudiantes'),
    ('Ventas',            'Venta de productos y artículos'),
    ('Actividades',       'Actividades y talleres de la comunidad universitaria'),
    ('Anuncios',          'Avisos e información general'),
    ('Eventos',           'Eventos académicos, culturales y deportivos'),
    ('Búsqueda de objetos','Objetos perdidos o buscados');


SHOW TABLES;
SELECT * FROM sede;
SELECT * FROM sede;
SELECT * FROM publicacion;

USE publicaciones;


INSERT INTO publicacion (titulo, descripcion, estado, id_usuario, id_categoria, id_sede, fecha_publicacion) VALUES
('Vendo laptop Lenovo', 'Laptop casi nueva, con cargador, ideal para programar', 'VIGENTE', 2, 2, 1, '2026-11-10 09:00:00'),
('Taller de Spring Boot', 'Evento gratuito sobre desarrollo backend con Java', 'VIGENTE', 2, 5, 2, '2026-11-12 15:30:00'),
('Perdí mi calculadora', 'Calculadora científica negra, la perdí cerca de la biblioteca', 'ATENDIDA', 2, 6, 1, '2026-11-13 11:00:00'),
('Emprendimiento de postres', 'Vendo brownies y tortas por encargo en el campus', 'VIGENTE', 2, 1, 3, '2026-11-14 08:00:00');
