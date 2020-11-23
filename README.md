# Plexbot - A discord bot for automating movie libraries

Plexbot was originally created for the purpose of automating the process of downloading movies from the Yify torrent group to the local file system by allowing a user to use a command to search for and request a movie. Since then the bot has had additional features integrated so that it has become a full movie library management utility in and of itself while still allowing for users to automate the process of adding movies they want without administrator involvement.

<br>

#### Table of Contents

1. [Features](#features)

2. [Required Channels](#required-channels)

3. [Bot Configuration](#bot-configuration)

4. [Database Creation](#database-creation)

5. [Running the bot](#running-the-bot)

   a. [System Requirements](#system-requirements)

   b. [Running as a JAR](#running-as-a-jar)

   c. [Running as a service](#running-as-a-service)

<br>

### Features

- **User requested movies:** Allow users within your discord server to add a movie to your movie library. This is handled through the request command where a user can search for a movie by its title, the year it was released, the IMDB ID of the movie, or a combination of all three.

  ```shell
  !r Iron Man
  ------ OR ------
  !r Iron Man --year=2008
  ------ OR ------
  !r --id=tt0371746
  ```

- **Waiting List:** Allows the bot to periodically (1x/hr) check to see if a movie is now available to be downloaded. A movie gets added to the waiting list if a user uses the request movie command, but the bot is unable to find the movie at the time. If a movie becomes available during one of the bots hourly checks, the bot will automatically download the movie that was requested and then send the user that requested it a message stating the movie has been added to the server.
- **Resolution Upgrader:** Simply adding a movie is often not the end of the road for a movie. Occasionally a new version of a movie that has already been downloaded to the server becomes available. If a new version that has a higher resolution that what is current downloaded does become available, the bot is capable of informing the administrator that there an upgrade is available. If the administrator replies with a :thumbsup: reaction, the bot will then download the new version of the movie to replace the old one.

<br>

### Required Channels

Below is a table that describes all the channels that Plexbot requires in order to properly function. You can change the name of the channels if you wish, since when you configure the plexbot, you will use the ID of the channel and put it in the configuration file for the bot. Also, listed below are the suggested permissions for each channel. The server administrator can be a role or an individual user and represent the person who can make decisions about what can go in the media library and administer the bot.

| **Channel Name**  | **Channel Description**                                      | **Suggested Permissions**                                    |
| :---------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| upgradable-movies | This channel is used to list the movies that can be upgraded to a new resolution. | Server Administrator:<br />`Read, Send, Edit, React, Delete`<br /><br />Plexbot:<br />`Read, Edit, Send, React, Delete`<br /><br />Others:<br />`None` |
| upgraded-movies   | This channel is used to notify users that a movie has been upgraded to a better resolution. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Edit, Send, Delete`<br /><br />Others:<br />`Read` |
| bot-status        | This channel is used by the bot to display its current status message. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| new-movies        | This channel is used to notify users that a movie has been added to the library. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| waiting-list      | This channel is used to list the movies that are in the waiting list. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| movie-request*    | This channel is used by users to request that movies be downloaded. | Server Administrator:<br />`Read, Send, React, Edit, Delete`<br /><br />Plexbot:<br />`Read, Send, React, Edit, Delete`<br /><br />Others:<br />`Read, Send, React, Edit (self), Delete (self)` |

*= *This channel is optional, if you have other text channels that a user can make requests in or such, simply make sure the bot has the permissions listed for this channel on whatever channel(s) you want to use. Additionally, the bot does not need to know the ID of this channel.*

<br>

### Bot Configuration

The bot requires a `config.yaml` to be located in the same folder as the bot JAR file in order to run. If running this from an IDE, you should have the file located within the root of the project folder. Below is an example configuration file. Please note that the values provided are **examples** and will not work. You will need to customize them yourself. 

```yaml
####################
### Bot Settings ###
####################
BotSettings:
  # The bot token, this is obtained from the Discord Developer Portal
  token: thisissomerandomediscordbotapitokendonotuseit

  # The name of the bot displayed in all messages
  botName: Plexbot
  
  # The prefix the bot will use to listen for commands. Must be contained within ""
  botPrefix: "!"

  # The folder that the movies will be downloaded to
  movieDownloadFolder: /path/to/movie/folder

  # URL of Image displayed in place of movies that do not have a poster image
  noPosterImageUrl: https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/1024px-No_image_available.svg.png

  # Base URL of the current YTS domain to look at for API requests
  # At the time of this commit, this is the current domain so this can be left as is
  currentYtsDomain: https://yts.mx

  # The ID of the channel for listing movies that can be upgraded to a better resolution
  upgradableMoviesChannelId: 012345678901234567

  # The ID of the channel for notifications about movies that have been upgraded to a better resolution
  upgradedMoviesChannelId: 012345678901234567

  # The ID of the channel where the bots status message will be located
  botStatusChannelId: 012345678901234567

  # The ID of the channel for notifications about movies that have been added to the library
  newMoviesChannelId: 012345678901234567

  # The ID of the channel for notifications about movies that have been added to the waiting list
  waitlistChannelId: 012345678901234567


#########################
### Database Settings ###
#########################
DatabaseSettings:
  # The IP or URL of the server hosting the bots database
  ipAddress: 127.0.0.1

  # The database server port
  port: 3306

  # The name of the database the bot should use
  dbName: Plexbot

  # The username the bot will use to access the database
  username: Plexbot

  # The password the bot will use to access the database
  password: somepassword


##################################
### Plex Media Server Settings ###
##################################
PlexServerSettings:
  # The IP address of the Plex Media Server. If the bot is on the same network as the Plex server,
  # this can be the private/local IP address, otherwise this should be the public IP address of the server
<<<<<<< Updated upstream
  ipAddress: 192.168.1.50
=======
  ipAddress: 127.0.0.1
>>>>>>> Stashed changes

  # The port of the Plex Media Server. Plex defaults to 32400 unless it is manually changed
  port: 32400

  # The username to your plex.tv account
  username: username

  # The password to your plex.tv account.
  password: Password1234

  # The Client Identifier used to identify individual devices on a Plex account. This needs to be a unique
  # UUID. You can generate one with the following commands:
  #   Windows (Powershell): [guid]::NewGuid()
  #   macOS and Linux: uuidgen
  clientIdentifier: 8A3029AF-05EC-488B-91F5-FBA66706000F


################
### API Keys ###
################
ApiKeys:
  # The API key for all requests to the OMDb API for getting IMDB OmdbMovie ID's.
  # You can get an API ket for this at http://www.omdbapi.com/ however it is recommended to purchase
  # at least the Basic plan through https://www.patreon.com/join/omdb to avoid API key limits.
  omdbApiKey: somekey

  # Private API key for real-debrid which is where the movies get downloaded from
  # In order to get an API key, you will need to register an account at https://real-debrid.com
  # and purchase a premium offer. Generally the 180 day plan is the best deal. Once purchased, go
  # to https://real-debrid.com/apitoken to retrieve your token. You will need to purchase more time
  # before your plan expires otherwise the bot will not function.
  realDebridKey: somerandomeapikeygoeshereotherwiseitwontwork
```

<br>

### Database Creation

In order to run the plexbot, you must have a MariaDB database created for the bot to utilize. Make sure the account you will use for the bot to access the database has all permissions except the grant permission. You do not need to populate the database, as that will be done for you the first time the bot starts up. The database can either be run locally on the same machine as the bot or on an external server.

<br>

### Running the bot

When running the bot you have several options. The first option is to simply run it manually by starting the JAR file. This is the simplest option, but if the bot crashes, or the user account logs out the bot will be stopped as well. The second option is to create and register a service for the bot.

###### System Requirements:

The plexbot was designed as lightweight as possible when it comes to resources, and contains all libraries it requires are packaged within itself except from the database. In order to run, you need the following installed.

- Java Runtime Environment *(11 or above)*
- MariaDB Database Server *(can be a remote server)*

###### Running as a JAR:

Running the bot as a JAR file is simple and straight forward. Simply make sure the bot and the `config.yaml` file reside within the same directory and then run the following command *(replace the version if necessary)*:

```bash
java -jar plexbot.jar
```

###### Running as a service:

The second method of running the bot is as a service. There are different ways to do this for each operating system, so for the sake of simplicity this document covers registering a systemd service for operating systems such as Ubuntu and Debian.

1. Navigate to your computer/servers systemd service folder:

   ```bash
   cd /etc/systemd/system/
   ```

2. Create the service file for the bot: 

   ```bash
   nano plexbot.service
   ```

3. Paste the following content into the file. Make sure to update the **WorkingDirectory** field to match the root folder of your bot. When you are finished, save and close the file.

   ```bash
   [Unit]
   Description=Plexbot Discord Bot
   After=network-online.target
   
   [Service]
   ExecStart=/usr/bin/java -jar plexbot.jar
   WorkingDirectory=/path/to/bot
   Restart=on-failure
   RestartSec=10
   TimeoutStopSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

4. Next, register the service with the following command:

   ```bash
   systemctl enable plexbot.service
   ```

5. Finally, start the bot:

   ```bash
   systemctl start plexbot
   ```

   