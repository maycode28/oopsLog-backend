CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    birth_date DATE,
    phone_number VARCHAR(20),
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE failures (
    failure_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_failures_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE ai_corrections (
    correction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    failure_id BIGINT NOT NULL UNIQUE,
    title VARCHAR(255),           -- AI가 지어준 분석 제목 추가
    core_interpretation TEXT,
    analysis_message TEXT,        -- gardener_message -> analysis_message로 변경
    facts TEXT,                   -- JSON 혹은 TEXT
    deconstruction_fact TEXT,
    deconstruction_interpretation TEXT,
    FOREIGN KEY (failure_id) REFERENCES failures(failure_id)
);

CREATE TABLE cognitive_distortions (
    distortion_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correction_id BIGINT NOT NULL,
    distortion_type VARCHAR(100),
    CONSTRAINT fk_cognitive_distortions_correction FOREIGN KEY (correction_id) REFERENCES ai_corrections(correction_id)
);

CREATE TABLE correction_perspectives (
    perspective_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    correction_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    CONSTRAINT fk_correction_perspectives_correction FOREIGN KEY (correction_id) REFERENCES ai_corrections(correction_id)
);

CREATE TABLE animals (
    animal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    image_url VARCHAR(255),
    required_save_count INT DEFAULT 5
);

CREATE TABLE user_animals (
    user_animal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    animal_id BIGINT NOT NULL,
    save_count INT DEFAULT 0,
    intimacy_score INT DEFAULT 0,
    is_my_pet BOOLEAN DEFAULT FALSE,
    last_interacted_at TIMESTAMP,
    CONSTRAINT fk_user_animals_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_animals_animal FOREIGN KEY (animal_id) REFERENCES animals(animal_id)
);

