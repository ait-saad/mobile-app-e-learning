# SkillLearn - Plateforme d'Apprentissage Mobile

<div align="center">
  <img src="C:\Users\saad\AndroidStudioProjects\SkillLearn\app\src\main\ic_launcher-playstore.png" alt="SkillLearn Logo" width="120" height="120"/>
  <h3>Développez vos compétences où que vous soyez</h3>
</div>

## 📱 À propos

SkillLearn est une application Android qui permet aux utilisateurs d'accéder à une variété de cours en ligne dans différents domaines. L'application propose une interface conviviale pour explorer, suivre et compléter des cours, avec des fonctionnalités telles que des quiz interactifs, des notes personnelles, et un système de suivi des progrès.

## ✨ Fonctionnalités principales

- **Catalogue de cours** : Parcourez une vaste sélection de cours dans différents domaines
- **Filtrage et recherche** : Trouvez rapidement des cours par catégorie, niveau ou mots-clés
- **Lecteur vidéo intégré** : Visionnez les contenus des cours directement dans l'application
- **Quiz interactifs** : Testez vos connaissances avec des quiz à la fin des sections
- **Notes personnelles** : Prenez des notes pendant vos sessions d'apprentissage
- **Système de progression** : Suivez votre avancement dans chaque cours
- **Système de badges** : Gagnez des badges en complétant des cours et des quiz
- **Mode hors-ligne** : Accédez à vos cours même sans connexion internet
- **Profil personnalisable** : Personnalisez votre profil avec des avatars

## 🔧 Technologies utilisées

- **Langage** : java
- **Architecture** : MVVM (Model-View-ViewModel)
- **Base de données** : Firebase Realtime Database
- **Authentification** : Firebase Authentication
- **Storage** : Firebase Storage
- **UI Components** : Material Design
- **Image Loading** : Picasso, Glide
- **Media Player** : ExoPlayer, YouTube Player
- **UI/UX** : Animations fluides, transitions et interfaces intuitives

## 🚀 Installation

1. Clonez ce dépôt
   ```bash
   git clone https://github.com/username/skilllearn.git
   ```

2. Ouvrez le projet dans Android Studio

3. Synchronisez le projet avec les fichiers Gradle

4. Exécutez l'application sur un émulateur ou un appareil physique

## 📂 Structure du projet

```
app/
├── build.gradle.kts        # Configuration Gradle du module
├── google-services.json    # Configuration Firebase
├── src/
│   └── main/
│       ├── java/com/projet/skilllearn/
│       │   ├── model/       # Classes de données
│       │   ├── repository/  # Couche d'accès aux données
│       │   ├── utils/       # Classes utilitaires
│       │   ├── view/        # Activités et fragments
│       │   │   └── adapters/ # Adaptateurs RecyclerView
│       │   │   └── fragments/ # Fragments
│       │   └── viewmodel/   # ViewModels pour MVVM
│       └── res/             # Ressources (layouts, drawables, etc.)
└── ...
```

## 🔐 Configuration Firebase

Pour que l'application fonctionne correctement, vous devez configurer Firebase pour votre projet :

1. Créez un projet Firebase sur la [console Firebase](https://console.firebase.google.com/)
2. Ajoutez une application Android avec le package `com.projet.skilllearn`
3. Téléchargez le fichier `google-services.json` et placez-le dans le répertoire `app/`
4. Activez Firebase Authentication, Realtime Database et Storage dans la console Firebase
5. Configurez les règles de sécurité pour la base de données et le stockage

## 📱 Captures d'écran

<div align="center">
  <img src="screenshots/login.png" width="200" alt="Écran de connexion"/>
  <img src="screenshots/home.png" width="200" alt="Écran d'accueil"/>
  <img src="screenshots/course_detail.png" width="200" alt="Détail d'un cours"/>
  <img src="screenshots/course_player.png" width="200" alt="Lecteur de cours"/>
</div>

## 📄 Structure de la base de données

L'application utilise Firebase Realtime Database avec la structure suivante :

```
- courses/              # Catalogue de cours
  - course1/            # ID du cours
    - title: string
    - description: string
    - author: string
    - category: string
    - ...
- sections/             # Sections de cours
  - section1/           # ID de la section
    - courseId: string
    - title: string
    - content: string
    - videoUrl: string
    - ...
- quizzes/              # Quiz
  - quiz1/              # ID du quiz
    - questions: array
    - ...
- user_progress/        # Progression des utilisateurs
  - userId/
    - courseId/
      - percentage: number
      - sections: object
      - ...
- user_achievements/    # Badges des utilisateurs
  - userId/
    - achievementId/
      - title: string
      - description: string
      - ...
```

## 🔄 Fonctionnalités à venir

- [ ] Mode sombre
- [ ] Partage de notes entre utilisateurs
- [ ] Système de commentaires et discussions
- [ ] Téléchargement des cours pour un accès hors-ligne complet
- [ ] Notifications pour rappels d'apprentissage
- [ ] Intégration de paiements pour les cours premium

## 🤝 Contribuer

Les contributions sont les bienvenues ! Si vous souhaitez contribuer à ce projet, veuillez suivre ces étapes :

1. Forkez le projet
2. Créez votre branche de fonctionnalité (`git checkout -b feature/amazing-feature`)
3. Committez vos changements (`git commit -m 'Add some amazing feature'`)
4. Poussez vers la branche (`git push origin feature/amazing-feature`)
5. Ouvrez une Pull Request

## 📝 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 👥 Auteurs

- **Votre Nom** - *Développeur principal* - [GitHub](https://github.com/username)

## 🙏 Remerciements

- [Material Design](https://material.io/design) pour les composants d'interface
- [Firebase](https://firebase.google.com/) pour les services backend
- [ExoPlayer](https://exoplayer.dev/) pour la lecture média
- [Picasso](https://square.github.io/picasso/) et [Glide](https://github.com/bumptech/glide) pour le chargement d'images
