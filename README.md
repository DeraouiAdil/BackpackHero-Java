# Projet Java William Adil

Pour jouer au jeu, vous devez compiler le main.

Organisation / hiérarchie des classes:
les items sont dans fr.uge.items
les personnages sont dans fr.uge.characters
les salles sont dans fr.uge.dungeon
les class pour l'affichage dans fr.uge.view
les class pour les cliques de souris dans fr.uge.controller
les enums des items dans fr.uge.items.enums
les enums des personnages sont dans fr.uge.characters.enums

Problèmes rencontrés:
-On a du mal à faire le choix entre une class et un record au début du projet.
-Le projet stocke les information d'un item ou d'un personnage depuis des 
json, on a dû se renseigner pour savoir comment faire.
-On a passer pas mal de temps à améliorer notre code pour ne pas faire de l'exterieur.
-Ouvrir un fichier pour afficher le user.pdf lorsqu'on choisi règle dans
le menu principal.
-On a fait au mieux pour ne pas faire de class fourre-tout, et que le tout soit 
bien organisé.
-On a du mal à utiliser la bibliothèque zen.

Choix techniques:
-Floor.java:
Cette class fait un parcours en profondeur pour generer un étage du donjon, et elle utilise 
l'algorithme de Dijkstra, pour trouver le plus court chemin.
-BackPack.java:
On a choisi une hashmap pour modeliser le sac à dos, pour avoir plus de liberté sur la forme du sac.
-BattleRoom:
Pour les combats normaux et pour les combats de boss, on utilise un seul et même objet BattleRoom,
car ces deux là représente un objet qui fait la même chose. Pas besoin de créer un objet BattleRoomBoss.
Les enemies sont stockés dans une liste, on accède à l'enemi à partir de son index.
-Enemy:
Les boss sont aussi dans la class Enemy car ils font la même chose que les enemies normaux c'est-à-dire
attaquer et prendre des dégâts. On peut se passer d'un objet en plus, pas besoin d'un boss.


