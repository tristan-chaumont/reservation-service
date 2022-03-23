# Projet API - Tristan Chaumont - M2 ACSI - Reservation service

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

Pour les réservations *aller-retour*, puisque l'on a pas d'interface graphique à disposition et que les entrées de l'utilisateur ne sont pas sauvegardées, il faut d'abord réserver son voyage aller, puis son voyage retour. Le paramètre `type=aller-retour` pour la requête qui cherche les voyages ne sert donc qu'à vérifier s'il existe effectivement des voyages retour la semaine suivant le trajet aller.

De même, ma conception ne différencie pas les voyages aller des voyages retour, car je les sauvegarde de la même manière au sein de la DB, il n'y a pas d'attribut qui indique que le voyage est un aller ou un retour dans la DB, car je n'en voyais pas l'utilité ici.

## Database

La DB utilisée pour le service est une base PostgreSQL sous Docker. Le fichier `docker-compose.yml` est à la racine du projet. Pour la créer :
``` docker-compose up --build -d```

Je ne crée pas de volume donc le container est entièrement supprimé à chaque arrêt de Docker.

Pour les tests, j'utilise simplement une base H2, donc il n'y a rien à faire.

## Swagger

La documentation Swagger est basique et est disponible au lien suivant : `localhost:<PORT>/swagger-ui/index.html`

## Communication avec le service Bank

La communication avec le service Bank fonctionne de la même manière que l'exemple conversion-bourse que l'on a étudié durant le cours. J'utilise une classe `BankResponse` pour transférer les données d'un service à un autre et qui contient les éléments suivants :
- `paymentAuthorized` : un booléen qui indique si le paiement est autorisé ou non
- `username` : le nom de l'utilisateur (je n'utilise pas l'id de l'utilisateur pour stocker les données dans la banque mais son nom)
- `port` : le port utilisé

Pour le CircuitBreaker, la fonction fallBack renvoie une interdiction de paiement (donc le booléen à `false`) 

## Données de test

Des données de test sont pré-insérées dans la base pour pouvoir utiliser le service.

### Traveler

| | Traveler 1 | Traveler 2 |
| --- | --- | --- |
| `traveler_id` | '1' | '2' |
| `name` | 'Chaumont' | 'Noirot' |

### Reservation

| | Reservation 1 | Reservation 2 | Reservation 3 |
| --- | --- | --- | --- |
| `reservation_id` | '1' | '2' | '3' |
| `traveler_id` | '1' | '1' | '2' |
| `trip_id` | '1' | '2' | '3' |
| `window_seat` | true | false | true |
| `status` | 'PAID' | 'CONFIRMED' | 'PENDING' |

### Trip

| | Trip 1 | Trip 2 | Trip 3 | Trip 4 | Trip 5 | Trip 6 |
| --- | --- | --- | --- | --- | --- | --- |
| `trip_id` | '1' | '2' | '3' | '4' | '5' | '6' |
| `departure_city` | 'NANCY' | 'METZ' | 'METZ' | 'PONT-A-MOUSSON' | 'METZ' | 'NANCY' |
| `arrival_city` | 'METZ' | 'NANCY' | 'THIONVILLE' | 'NANCY' | 'NANCY' | 'METZ' |
| `departurue_time` | 2022-03-25T12:50:00 | 2022-03-25T16:32:00 | 2022-03-25T13:33:00 | 2022-03-25T14:23:00 | 2022-07-25T09:07:00 | 2022-07-26T14:23:00 |
| `arrival_time` | 2022-03-25T13:28:00 | 2022-03-25T17:10:00 | 2022-03-25T13:56:00 | 2022-03-25T14:40:00 | 2022-07-25T09:45:00 | 2022-07-26T14:40:00 |
| `price` | 10 | 9 | 8 | 5 | 8 | 5 |
| `num_corridor` | 50 | 50 | 0 | 50 | 50 | 50 |
| `num_window` | 50 | 50 | 50 | 0 | 50 | 50 |

Le voyage 3 n'a pas de place côté couloir, le voyage 4 pas de place côté fenêtre. Les 4 premiers trajets sont prévus pour le 25 mars 2022, les 2 derniers pour le 25 juillet.

## API

### Traveler

`GET /travelers` : renvoie la liste des voyageurs
- `200` : la liste des voyageurs

`GET /travelers/{travelerId}` : renvoie un voyageur en particulier
- PathVariable
  - `travelerId` : l'id du voyageur
- Codes de réponse 
  - `200` : le voyageur dont l'id correspond
  - `404` : si le voyageur n'existe pas

`GET /travelers/{travelerId}/reservations` : renvoie la liste des réservations d'un voyageur en particulier
- PathVariable
  - `travelerId` : l'id du voyageur
- Codes de réponse
  - `200` : la liste des réservations du voyageur
  - `404` : si le voyageur n'existe pas

`GET /travelers/{travelerId}/favorites?date=yyyy-MM-ddTHH:mm` : renvoie les trajets préférés du voyageur, uniquement s'il existe un voyage à la date spécifiée en *RequestParam*. Cette recherche se base sur le nombre de voyage fait par un voyageur entre deux villes, triéé par ordre décroissant.
- PathVariable
  - `travelerId` : l'id du voyageur
- RequestParam
  - `date (required)` : la date de départ au format yyyy-MM-ddTHH:mm
- Codes de réponse
  - `200` : la liste des trajets préférés
  - `400` : si le format de la date de convient pas (yyyy-MM-ddTHH:mm -> 2022-03-25T11:00)
  - `404` : 
    - si le voyageur n'existe pas
    - s'il n'y a pas de voyage disponible pour cette date

#### Liens HETOAS de Traveler

- **self** : `GET /travelers/{travelerId}`
- **myReservations** : `GET /travelers/{travelerId}/reservations`
- **collection** : `GET /travelers`

### Trip

`GET /trips` : renvoie la liste de tous les voyages
- `200` : la liste des voyages

`GET /trips/{tripId}` : renvoie un voyage en particulier
- PathVariable
  - `tripId` : l'id du voyage 
- Codes de réponse
  - `200` : le voyage dont l'id correspond
  - `404` : si le voyage n'existe pas

`GET /trips/{cityA}/{cityB}?date=yyyy-MM-ddTHH:mm&type=aller&windowSeat=true&sortByPrice=true` : renvoie la liste des voyages qui remplissent les critères de sélection (aller/aller-retour, trié par prix, place côté fenêtre ou couloir). Un voyage est renvoyé uniquement si son jour de départ correspond au jour de la date renseignée par le voyageur.
- PathVariable
  - `cityA` : la ville de départ (exemple : Metz)
  - `cityB` : la ville d'arrivée (exemple : Nancy)
- RequestParam
  - `date (required)` : la date de départ au format yyyy-MM-ddTHH:mm
  - `windowSeat (required)` : place côté fenêtre si `true`, côté couloir sinon
  - `type` : `aller` ou `aller-retour`, par défaut `aller`
  - `sortByPrice` : voyages triés par prix si `true`. Par défaut trié par date.
- Codes de réponse
  - `200` : la liste des voyages
  - `400` :
    - le type du trajet ne correspond pas (`aller` ou `aller-retour`)
    - la date de départ n'est pas au bon format
  - `404` :
    - s'il n'y a pas de trajet de `cityA` vers `cityB` (ou, par extension, si une des deux villes n'existe pas)
    - s'il n'y a pas de trajet de `cityA` vers `cityB` pour `date` et `windowSeat`
    - si type = `aller-retour`, s'il n'y a pas de trajet retour dans la semaine qui suit le trajet cherché

#### Liens HETOAS de Trip

- **self** : `GET /trips/{tripId}`
- **book** : `POST /reservations` (il faut compléter le RequestBody ensuite)
- **collection** : `GET /trips`

### Reservation

`GET /reservations` : renvoie la liste de toutes les réservations
- `200` : la liste des reservations

`GET /reservations/{reservationId}` : renvoie une réservation en particulier
- PathVariable
  - `reservationId` : l'id de la réservation
- Codes de réponse
  - `200` : la réservation dont l'id correspond
  - `404` : si la réservation n'existe pas

`POST /reservations` : crée une nouvelle réservation
- RequestBody
  - ReservationInput
    - `travelerId` : l'id du voyageur
    - `tripId` : l'id du voyage
    - `windowSeat` : `true` si place côté fenêtre, `false` sinon
- Codes de réponse
  - `201` : réservation créée -> renvoie l'URI de la réservation (`GET /reservations/{reservationId}`)
  - `400` : 
    - si une réservation pour le voyageur et le trajet existe déjà
    - il n'y a plus de place côté fenêtre ou couloir (selon le choix de l'utilisateur)
  - `404` :
    - si l'id du voyage n'existe pas
    - si l'id du voyageur n'existe pas

`DELETE /reservations/{reservationId}/cancel` : annule une réservation (se traduit par la suppression de la réservation dans la db)
 - PathVariable
  - `reservationId` : l'id de la réservation
 - Codes de réponse
  - `204` : la réservation est annulée (donc supprimée)
  - `400` : si la réservation est déjà confirmée ou payée
  - `404` : si la réservation n'existe pas

`PATCH /reservations/{reservationId}/confirm` : confirme une réservation passée (se traduit par la changement de l'état de *PENDING* à *CONFIRMED*)
- PathVariable
  - `reservationId` : l'id de la réservation
- Codes de réponse
  - `200` : la réservation est confirmée et l'état est modifié
  - `400` : si la réservation a déjà été confirmée ou payée
  - `404` : si la réservation n'existe pas

`PATCH /reservations/{reservationId}/pay` : paie une réservation confirmée (appelle le service Bank pour autoriser le paiement). L'état passe de *CONFIRMED* à *PAID*.
- PathVariable
  - `reservationId` : l'id de la réservation
- Codes de réponse
  - `200` : la réservation est payée et l'état est modifié
  - `400`
    - si la réservation n'a pas été confirmée ou est déjà payée
    - si le service Bank n'a pas autorisé le paiement
  - `404` : si la réservation n'existe pas

#### Liens HETOAS de Reservation

- **self** : `GET /reservations/{reservationId}`
- **collection** : `GET /reservations`
- Si l'état est *PENDING*
  - **cancel** : `DELETE /reservations/{reservationId}/cancel`
  - **confirm** : `PATCH /reservations/{reservationId}/confirm`
- Si l'état est *CONFIRMED*
  - **pay** : `PATCH /reservations/{reservationId}/pay` 
