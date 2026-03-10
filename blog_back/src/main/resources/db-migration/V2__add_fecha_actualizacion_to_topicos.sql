ALTER TABLE topicos ADD COLUMN fecha_actualizacion DATETIME(6);
UPDATE topicos SET fecha_actualizacion = fecha_creacion WHERE fecha_actualizacion IS NULL;
ALTER TABLE topicos MODIFY COLUMN fecha_actualizacion DATETIME(6) NOT NULL;
