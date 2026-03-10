-- Crear tabla de cursos IT
CREATE TABLE IF NOT EXISTS cursos_it (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre_visible VARCHAR(100) NOT NULL UNIQUE,
    area VARCHAR(50) NOT NULL,
    nivel VARCHAR(20) NOT NULL
    );

-- Datos iniciales compatibles con los tópicos
INSERT INTO cursos_it (codigo, nombre_visible, area, nivel) VALUES
                                                                ('CURSO_LARAVEL',   'Curso Laravel Profesional',  'Backend / Full Stack',   'Intermedio'),
                                                                ('CURSO_JAVA_WEB',  'Curso Java Web',             'Backend / Full Stack',   'Intermedio'),
                                                                ('CURSO_SPRING',    'Curso Spring Boot',          'Backend / Full Stack',   'Avanzado'),
                                                                ('CURSO_REACT_FE',  'Curso React Frontend',       'Frontend',               'Intermedio'),
                                                                ('CURSO_JS_INI',    'Curso JavaScript desde Cero','Frontend',               'Inicial'),
                                                                ('CURSO_HTML_CSS',  'Curso HTML y CSS',           'Frontend',               'Inicial'),
                                                                ('CURSO_PYTHON',    'Curso Python Inicial',       'Programación general',   'Inicial'),
                                                                ('CURSO_DATA_SQL',  'Curso SQL y Bases de Datos', 'Data / Bases de datos',  'Inicial'),
                                                                ('CURSO_DATA_ANL',  'Curso Data Analytics',       'Data / Analytics',       'Intermedio'),
                                                                ('CURSO_ML_BASICO', 'Curso Machine Learning',     'IA / Machine Learning',  'Intermedio'),
                                                                ('CURSO_DEVOPS',    'Curso DevOps y CI/CD',       'DevOps / Cloud',         'Intermedio'),
                                                                ('CURSO_DOCKER',    'Curso Docker y Kubernetes',  'DevOps / Cloud',         'Avanzado'),
                                                                ('CURSO_SECURITY',  'Curso Ciberseguridad',       'Ciberseguridad',         'Inicial'),
                                                                ('CURSO_TEST_QA',   'Curso Testing QA',           'Testing / QA',           'Inicial'),
                                                                ('CURSO_ALGOS',     'Curso Algoritmos y Lógica',  'Intro programación',     'Inicial');
