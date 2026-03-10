ALTER TABLE respuestas
    ADD COLUMN padre_id BIGINT NULL,
ADD CONSTRAINT fk_respuesta_padre
FOREIGN KEY (padre_id) REFERENCES respuestas(id);

