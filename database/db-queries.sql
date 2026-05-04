CREATE DATABASE IF NOT EXISTS esprit;
USE esprit;

CREATE TABLE personne (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          nom VARCHAR(50),
                          prenom VARCHAR(50),
                          age INT
) ENGINE=InnoDB;

CREATE TABLE pet (
                     id INT AUTO_INCREMENT PRIMARY KEY,
                     name VARCHAR(255) NOT NULL,
                     birth_date DATE NOT NULL,
                     type_pet VARCHAR(50) NOT NULL,
                     breed VARCHAR(255) NOT NULL,
                     weight FLOAT NOT NULL,
                     description TEXT NOT NULL,
                     gender VARCHAR(50) NOT NULL,
                     has_contagious_disease BOOLEAN NOT NULL,
                     has_medical_record BOOLEAN NOT NULL,
                     has_critical_condition BOOLEAN NOT NULL,
                     is_vaccinated BOOLEAN NOT NULL,
                     image_name VARCHAR(255),
                     owner_id INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE announcement (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              address VARCHAR(255) NOT NULL,
                              visit_hours JSON,
                              care_type VARCHAR(50) NOT NULL,
                              date_debut DATE NOT NULL,
                              date_fin DATE NOT NULL,
                              visit_per_day INT,
                              renumeration_min FLOAT,
                              renumeration_max FLOAT,
                              services VARCHAR(255),
                              pet_id INT NOT NULL,
                              user_id INT NOT NULL,
                              CONSTRAINT fk_announcement_pet FOREIGN KEY (pet_id)
                                  REFERENCES pet(id) ON DELETE CASCADE
) ENGINE=InnoDB;


CREATE TABLE IF NOT EXISTS postulation (
                                           id               INT AUTO_INCREMENT PRIMARY KEY,
                                           announcement_id  INT         NOT NULL,
                                           gardien_id       INT         NOT NULL,
                                           date_postulation DATE        NOT NULL,
                                           statut           VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
                                           FOREIGN KEY (announcement_id) REFERENCES announcement(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS notification (
                                            id              INT AUTO_INCREMENT PRIMARY KEY,
                                            destinataire_id INT          NOT NULL,  -- user qui reçoit la notif
                                            expediteur_id   INT          NOT NULL,  -- user qui a déclenché l'action
                                            postulation_id  INT          NOT NULL,
                                            message         TEXT         NOT NULL,
                                            type            VARCHAR(30)  NOT NULL,  -- 'NOUVELLE_POSTULATION' ou 'POSTULATION_ACCEPTEE'
                                            lu              BOOLEAN      NOT NULL DEFAULT FALSE,
                                            date_creation   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS message (
                                       id           INT AUTO_INCREMENT PRIMARY KEY,
                                       expediteur_id   INT      NOT NULL,
                                       destinataire_id INT      NOT NULL,
                                       postulation_id  INT      NOT NULL,
                                       contenu         TEXT     NOT NULL,
                                       date_envoi      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       lu              BOOLEAN  NOT NULL DEFAULT FALSE
);