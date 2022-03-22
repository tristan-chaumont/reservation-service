# Projet API - Tristan Chaumont - M2 ACSI

Le projet est composé de deux services
- [reservation-service](https://github.com/tristan-chaumont/reservation-service) qui est le service principal
- [bank-service](https://github.com/tristan-chaumont/bank-service) qui est l'appel à la banque pour l'autorisation de paiement

## Choix de conception

Le service **reservation** est composé de 3 entités :
- *Trip* représente un voyage entre deux villes
- *Traveler* représente un voyageur, donc un utilisateur du service
- *Reservation* représente une réservation faite par un voyageur pour un voyage

Chaque entité est composé des éléments suivants (en ignorant l'id de chaque entité) :

| Trip | Traveler | Reservation |
| --- | --- | --- |
| **departureCity** : la ville de départ | **name** : le nom du voyageur | **traveler** : le voyageur qui a effectué cette réservation |
| **arrivalCity** : la ville d'arrivée | | **trip** : le voyage qui a été réservé |
| **departureTime** : la date de départ (jour + heure) | | **windowSeat** : si la place réservée est côté fenêtre ou non |
| **arrivalTime** : la date d'arrivée (jour + heure) | | **status** : l'état de la réservation (*PENDING*, *CONFIRMED*, *PAID*) |
| **price** : le prix du voyage | | |
| **numCorridor** : le nombre de places côté couloir | | |
| **numWindow** : le nombre de places côté fenêtre | | |

## Database

La DB utilisée pour le service est une base PostgreSQL sous Docker. Le fichier `docker-compose.yml` est à la racine du projet. Pour la créer :
``` docker-compose up --build -d```

Je ne crée pas de volume donc la container est entièrement supprimé à chaque arrêt de Docker.

Pour les tests, j'utilise simplement une base H2, donc il n'y a rien à faire.

## API

### Traveler

```GET /travelers``` : renvoie la liste des voyageurs
- `200` : la liste des voyageurs

```GET /travelers/{travelerId}``` : renvoie un voyageur en particulier
- `200` : le voyageur dont l'id correspond
- `404` : si le voyageur n'existe pas

```GET /travelers/{travelerId}/reservations``` : renvoie la liste des réservations d'un voyageur en particulier
- `200` : la liste des réservations du voyageur
- `404` : si le voyageur n'existe pas

```GET /travelers/{travelerId}/favorites?date=yyyy-MM-ddTHH:mm``` : renvoie les trajets préférés du voyageur, uniquement s'il existe un voyage à la date spécifiée en *RequestParam*. Cette recherche se base sur le nombre de voyage fait par un voyageur entre deux villes, triéé par ordre décroissant.
- `200` : la liste des trajets préférés
- `400` : si le format de la date de convient pas (yyyy-MM-ddTHH:mm -> 2022-03-25T11:00)
- `404` : si le voyageur n'existe pas ou s'il n'y a pas de voyage disponible pour cette date


- `200` : 
