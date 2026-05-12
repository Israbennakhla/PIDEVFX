# 🐾 SitMyPet - Application Desktop

SitMyPet est une application Desktop développée en **Java** avec **JavaFX**. Elle est conçue pour gérer de manière centralisée une plateforme de mise en relation entre propriétaires d'animaux de compagnie et gardiens.

L'application intègre un espace d'administration (Dashboard Admin) et une interface côté client (Front-Office) pour les utilisateurs (Propriétaires et Gardiens).

---

## 🚀 Fonctionnalités Principales

- **👥 Gestion des Utilisateurs :** Inscription, authentification (avec hachage BCrypt et intégration Google OAuth), rôles (`ADMIN`, `PROPRIETAIRE`, `GARDIEN`), blocage de compte après plusieurs tentatives échouées.
- **🐾 Gestion des Animaux :** Ajout, modification, et suivi des profils des animaux de compagnie avec leurs dossiers médicaux.
- **📢 Gestion des Annonces & Postulations :** Publication d'annonces de garde par les propriétaires et postulation par les gardiens.
- **📅 Gestion des Événements :** Création d'événements liés aux animaux et gestion des participations.
- **📩 Messagerie & Notifications :** Système de messagerie interne entre utilisateurs pour faciliter l'échange et alertes en temps réel.
- **⚠️ Réclamations :** Système de tickets pour signaler des problèmes ou des conflits, avec une gestion complète côté administrateur (réponses, suivi du statut).

---

## 🛠️ Technologies Utilisées

- **Langage :** Java 21+
- **Interface Graphique :** JavaFX (FXML, CSS)
- **Base de Données :** MySQL / MariaDB (via JDBC)
- **Sécurité :** jBCrypt pour le hachage des mots de passe.
- **Outils de Build :** Maven (`pom.xml`)

---

## ⚙️ Installation & Configuration

### 1. Prérequis
- Avoir le JDK (Java Development Kit) 21 ou supérieur installé.
- Avoir un serveur MySQL local (comme XAMPP, WAMP ou MAMP).
- Un IDE comme IntelliJ IDEA ou Eclipse.

### 2. Configuration de la Base de Données
1. Lancez votre serveur MySQL (ex: via le panneau de contrôle XAMPP).
2. Créez une base de données nommée `sitmypet` :
   ```sql
   CREATE DATABASE sitmypet;
   ```
3. *(Optionnel)* Modifiez les identifiants de la base de données dans le fichier `src/main/java/com/sitmypet/utils/MyDatabase.java` si vous utilisez un mot de passe spécifique pour MySQL (par défaut, l'utilisateur est `root` avec un mot de passe vide).
4. **Auto-génération :** Lors du premier lancement de l'application, les tables de la base de données seront créées automatiquement grâce à la méthode d'initialisation intégrée dans `MyDatabase.java`.

### 3. Exécution de l'Application
- Ouvrez le projet dans IntelliJ IDEA ou votre IDE préféré.
- Mettez à jour les dépendances Maven.
- Exécutez la classe principale **`Main.java`** (`src/main/java/com/sitmypet/Main.java`).

### 4. Insérer des Données de Test (Dummy Data)
Pour peupler la base de données afin de tester l'affichage, vous pouvez exécuter la classe utilitaire **`InsertDummyData.java`**. 
Faites un clic droit sur ce fichier dans votre IDE et sélectionnez `Run 'InsertDummyData.main()'`.

---

## 🔐 Accès Administrateur par Défaut

Lors de l'initialisation, un compte administrateur par défaut est généré pour vous permettre d'accéder au Dashboard.

- **Email :** `admin@sitmypet.com`
- **Mot de passe :** `Admin@1234` (ou le mot de passe d'urgence `admin`)

---

## 🤝 Contribution & Équipe

Ce projet a été développé dans le cadre d'un module d'intégration. Chaque membre a travaillé sur la gestion de différentes entités (Utilisateurs, Événements, Annonces, etc.) pour finalement les fusionner dans ce projet principal unifié.

---
*Fait avec ❤️ pour nos amis à quatre pattes.*
