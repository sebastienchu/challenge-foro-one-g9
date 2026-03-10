CREATE TABLE respuestas (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            topico_id BIGINT NOT NULL,
                            autor_id BIGINT NOT NULL,
                            mensaje TEXT NOT NULL,
                            fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            solucion BOOLEAN DEFAULT FALSE,
                            FOREIGN KEY (topico_id) REFERENCES topicos(id),
                            FOREIGN KEY (autor_id) REFERENCES users(id)
);
