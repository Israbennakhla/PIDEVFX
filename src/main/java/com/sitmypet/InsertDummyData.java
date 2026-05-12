package com.sitmypet;

import com.sitmypet.utils.MyDatabase;
import java.sql.Connection;
import java.sql.Statement;

public class InsertDummyData {

    public static void main(String[] args) {
        Connection cnx = MyDatabase.getInstance().getConnection();
        
        try (Statement st = cnx.createStatement()) {
            System.out.println("Début de l'insertion des données de test...");
            
            // 1. Utilisateurs
            st.executeUpdate("INSERT IGNORE INTO utilisateurs (id, nom, prenom, email, telephone, adresse, role, is_active, password, roles) VALUES "
                    + "(2, 'Dupont', 'Jean', 'jean@example.com', '0102030405', 'Paris', 'ROLE_PROPRIETAIRE', 1, 'mdp123', '[\"ROLE_PROPRIETAIRE\"]'),"
                    + "(3, 'Martin', 'Sophie', 'sophie@example.com', '0607080910', 'Lyon', 'ROLE_GARDIEN', 1, 'mdp123', '[\"ROLE_GARDIEN\"]'),"
                    + "(4, 'Durand', 'Paul', 'paul@example.com', '0708091011', 'Marseille', 'ROLE_USER', 1, 'mdp123', '[\"ROLE_USER\"]')");
            
            // 2. Animaux (Pets)
            st.executeUpdate("INSERT IGNORE INTO pet (id, name, birth_date, type_pet, breed, weight, description, gender, owner_id) VALUES "
                    + "(1, 'Rex', '2020-05-10', 'Chien', 'Berger Allemand', 25.5, 'Chien très joueur', 'Male', 2),"
                    + "(2, 'Mimi', '2021-08-15', 'Chat', 'Siamois', 4.2, 'Chat calme et affectueux', 'Femelle', 2)");
            
            // 3. Annonces (Announcements)
            st.executeUpdate("INSERT IGNORE INTO announcement (id, address, care_type, date_debut, date_fin, visit_per_day, renumeration_min, renumeration_max, services, pet_id, user_id) VALUES "
                    + "(1, 'Paris 15e', 'Garde à domicile', '2026-06-01', '2026-06-10', 2, 10.0, 20.0, 'Promenade, Nourriture', 1, 2),"
                    + "(2, 'Paris 15e', 'Promenade', '2026-07-01', '2026-07-05', 1, 5.0, 15.0, 'Promenade', 2, 2)");
            
            // 4. Postulations
            st.executeUpdate("INSERT IGNORE INTO postulation (id, announcement_id, gardien_id, date_postulation, statut) VALUES "
                    + "(1, 1, 3, '2026-05-12', 'ACCEPTEE'),"
                    + "(2, 2, 3, '2026-05-12', 'EN_ATTENTE')");
            
            // 5. Messages
            st.executeUpdate("INSERT IGNORE INTO message (id, expediteur_id, destinataire_id, postulation_id, contenu) VALUES "
                    + "(1, 3, 2, 1, 'Bonjour, je suis très intéressé par la garde de Rex.'),"
                    + "(2, 2, 3, 1, 'Bonjour Sophie, merci pour votre message !')");
            
            // 6. Notifications
            st.executeUpdate("INSERT IGNORE INTO notification (id, destinataire_id, expediteur_id, postulation_id, message, type) VALUES "
                    + "(1, 2, 3, 1, 'Sophie a postulé à votre annonce pour Rex', 'INFO'),"
                    + "(2, 3, 2, 1, 'Jean a accepté votre postulation', 'SUCCESS')");
            
            // 7. Événements (Events)
            st.executeUpdate("INSERT IGNORE INTO event (id, name, date, heure, addresse, description) VALUES "
                    + "(1, 'Rencontre Canine au Parc', '2026-06-15', '14:00', 'Parc des Buttes-Chaumont', 'Une grande rencontre pour tous les chiens !'),"
                    + "(2, 'Atelier Félin', '2026-06-20', '10:00', 'Salle des Fêtes, Lyon', 'Apprenez à mieux comprendre votre chat.')");
            
            // 8. Participants aux Événements
            st.executeUpdate("INSERT IGNORE INTO event_participants (event_id, user_id) VALUES "
                    + "(1, 2),"
                    + "(1, 3),"
                    + "(2, 2)");
            
            // 9. Réclamations
            st.executeUpdate("INSERT IGNORE INTO reclamation (id, sujet, description, statut, priorite, nom_client, email_client, user_id) VALUES "
                    + "(1, 'Problème de messagerie', 'Je n\\'arrive pas à envoyer de messages.', 'en_attente', 'haute', 'Jean Dupont', 'jean@example.com', 2),"
                    + "(2, 'Signaler un profil', 'Le gardien n\\'est pas venu.', 'traitee', 'moyenne', 'Paul Durand', 'paul@example.com', 4)");
            
            // 10. Réponses aux Réclamations
            st.executeUpdate("INSERT IGNORE INTO reponse (id, contenu, auteur, reclamation_id) VALUES "
                    + "(1, 'Nous sommes sur le coup. Merci de patienter.', 'Admin', 1),"
                    + "(2, 'Action prise contre le profil concerné.', 'Admin', 2)");
            
            System.out.println("✅ Données de test insérées avec succès dans tous les tableaux !");
            System.exit(0);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
