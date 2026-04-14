# 🎓 Explication du Code : Préparation aux Questions du Professeur

Voici un résumé technique parfait pour expliquer et défendre le code que vous venez de réaliser. Ce document contient les mots de vocabulaire technique que les professeurs aiment entendre.

---

## 1. La Navigation entre plusieurs fenêtres (FXMLLoader)
**❓ Question probable :** *"Comment as-tu fait pour naviguer et changer de page entre `EvenementView` et `ParticipantView` ?"*

**💡 La Réponse (Explication du code) :**
Dans JavaFX, la fenêtre principale de l'ordinateur s'appelle la **`Stage`** et le décor affiché dedans s'appelle la **`Scene`**.
*   Dans mon contrôleur (ex: `EvenementController.java`), j'ai créé une méthode liée au bouton.
*   J'utilise d'abord la classe `FXMLLoader.load(...)` pour charger et lire le 2ème fichier FXML en mémoire.
*   Ensuite, je récupère grâce au bouton cliqué (`event.getSource()`) la *Stage* (fenêtre) actuelle de l'utilisateur.
*   Enfin, je crée une nouvelle *Scene* avec mon 2ème fichier FXML et je remplace l'ancienne via `stage.setScene(...)` puis `stage.show()`.

---

## 2. Différence entre List / TableView et l'astuce de l'affichage
**❓ Question probable :** *"Pourquoi avoir utilisé un `ListView` au lieu d'un `TableView` et comment gères-tu l'affichage texte de chaque ligne ?"*

**💡 La Réponse (Explication du code) :**
*   J'ai remplacé le `TableView` par un `ListView` car c'est plus léger et adapté pour n'afficher qu'une seule colonne d'informations.
*   **L'Astuce :** Par défaut, un ListView affiche des adresses mémoires informatiques. Pour corriger ça, je suis allé dans mon Modèle (mes fichiers `Evenement.java` et `EventParticipant.java`) et j'ai procédé à la **Surcharge (Override)** de la fonction **`toString()`**. 
*   Ainsi, le ListView appelle automatiquement `toString()` pour obtenir la phrase claire (ex: "Nom (Date) - Adresse") et l'afficher correctement à l'écran.

---

## 3. Le CRUD Spécifique de la table `event_participants`
**❓ Question probable :** *"Pourquoi ton fichier ServiceEventParticipant n'a pas de fonction `update` (modifier) alors que le ServiceEvenement en a un ?"*

**💡 La Réponse (Explication du code) :**
*   C'est un choix d'**architecture de base de données**.
*   La table `event_participants` est ce qu'on appelle une **Table d'Association**, servant uniquement à faire le lien entre un Utilisateur et un Événement.
*   Dans ce cas métier, "Modifier" une inscription n'a pas de sens logique. On procède donc en Binaire : Soit un utilisateur s'inscrit (`INSERT`), soit il se désinscrit (`DELETE`). S'il se trompe d'événement, il doit se désinscrire puis s'inscrire au bon endroit.

---

## 4. L'utilisation d'une ObservableList
**❓ Question probable :** *"Dans la fonction `afficherEvenements()`, pourquoi utilises-tu un objet `ObservableList` et `FXCollections` au lieu d'une simple boucle For ou d'une liste ArrayList classique ?"*

**💡 La Réponse (Explication du code) :**
*   Mon ServiceSQL me renvoie bien une `List` standard (ArrayList java.util).
*   Seulement, JavaFX **refuse** d'insérer des listes classiques dans ses interfaces `ListView`.
*   J'utilise donc `FXCollections.observableArrayList()` pour convertir ma liste classique en `ObservableList`. 
*   **Pourquoi ?** Car une "ObservableList" est magique : c'est un Design Pattern (Le Pattern Observer). Si je modifie cette liste dans le code, elle prévient automatiquement l'interface graphique (le ListView) qu'il faut se mettre à jour sans que j'aie besoin de forcer un rafraîchissement d'écran manuellement.

---

## 5. La Faille de Sécurité SQL évitée (PreparedStatement)
**❓ Question probable :** *"Pourquoi tu utilises `PreparedStatement` au lieu d'un simple `Statement` pour ajouter un événement ?"*

**💡 La Réponse (Explication du code) :**
*   L'utilisation de `PreparedStatement` prévient les failles de sécurité **"SQL Injection"**.
*   Si j'utilise une simple concaténation de texte avec un `Statement` `("INSERT INTO... VALUES (" + tfName.getText() + ")")`, un hacker pourrait insérer des commandes SQL néfastes depuis les TextFields de l'interface (comme "DROP TABLE").
*   Avec `PreparedStatement`, j'utilise des points d'interrogation `?`. Le compilateur JDBC échappe et sécurise le texte entré empêchant ainsi qu'il soit analysé comme du code SQL. De plus, c'est mieux optimisé par MySQL !
