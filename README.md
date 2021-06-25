# Plexbot - A discord bot for automating movie libraries

Plexbot was originally created for the purpose of automating the process of downloading movies from the Yify torrent group to the local file system by allowing a user to use a command to search for and request a movie. Since then the bot has had additional features integrated so that it has become a full movie library management utility in and of itself while still allowing for users to automate the process of adding movies they want without administrator involvement.

This project originally started in the summer of 2017 as a way for me to manage my growing collection of media files and provide a way for my family members who use my Plex server to add media without my involvement. Since then I have completed a complete re-write of the project using a new framework called [Quarkus](https://quarkus.io/) in order to utilize reactive and asynchronous programming paradigms. This bot uses the following technologies in order to perform its core functions:

- [Quarkus](https://quarkus.io/) - Provides the core foundation and all CDI and REST API clients
- [Javacord](https://javacord.org/) - Provides the java implementation of the Discord API for bots
- MariaDB - The core database driver used by the bot for data storage

<br>

#### Table of Contents

1. [Features](#features)

2. [Command Documentation](#command-documentation)

3. [Naming Import Media](#naming-import-media)]

   a. [Deciding which ID to use](#deciding-which-id-to-use)

   b. [Naming Video Files](#naming-video-files)

   c. [Naming Subtitle File](#naming-subtitle-files)

4. [Required Channels](#required-channels)

5. [Bot Configuration](#bot-configuration)

6. [Database Creation](#database-creation)

7. [Running the bot](#running-the-bot)

   a. [System Requirements](#system-requirements)

   b. [Running as a JAR](#running-as-a-jar)

   c. [Running as a service](#running-as-a-service)

<br>

### 1. Features

- **User requested movies:** Allow users within your discord server to add a movie to your movie library. This is handled through the request command where a user can search for a movie by its title, the year it was released, the IMDB ID of the movie, or a combination of all three.

  ```shell
  !rm Iron Man
  ------ OR ------
  !rm Iron Man --year=2008
  ------ OR ------
  !rm --id=tt0371746
  ```

- **Waiting List:** Allows the bot to periodically (1x/hr) check to see if a movie is now available to be downloaded. A movie gets added to the waiting list if a user uses the request movie command, but the bot is unable to find the movie at the time. If a movie becomes available during one of the bots hourly checks, the bot will automatically download the movie that was requested and then send the user that requested it a message stating the movie has been added to the server.

- **Resolution Upgrader:** Simply adding a movie is often not the end of the road for a movie. Occasionally a new version of a movie that has already been downloaded to the server becomes available. If a new version that has a higher resolution that what is current downloaded does become available, the bot is capable of informing the administrator that there an upgrade is available. If the administrator replies with a :thumbsup: reaction, the bot will then download the new version of the movie to replace the old one.

- **Manual Import Command:** Simply is not all this bot can do. In order to allow you to use other media not sourced by the bot, an import command has been created allowing you to take external media sources such as movies, episodes, and their corresponding subtitles and have them be imported into the bots database and renamed to match the rest of the library. Simply 

<br>

### 2. Command Documentation

Below is a table containing all the commands the bot currently provides along with a description, and their usage. Please note that each command is triggered by using the bot's configured prefix directly before the command. For example, to use the ping command with a configured prefix of `$` a user would type the following command: `$ping` into a text channel or private message.

*The below usage assumes a prefix of `$`* has been configured. Please replace the `$` symbol with whatever prefix has been configured for your instance of the bot.

| Command Name | Command Description                                          | Command Usage                                                |
| :----------: | :----------------------------------------------------------- | ------------------------------------------------------------ |
|     ping     | Return information about how long the bot takes to recieve the command and send a message back to discord. | `$ping`                                                      |
|    stats     | Return information about the media stored on the server such as the numbers of each type of media, the total file size used, and the total playback duration of all media. | `$stats`                                                     |
|      rm      | Request a movie to be downloaded to the server. This will search on IMDb to locate a list of movies that match the search and then prompt the user to select the correct movie. If the movie the user selected is available to download, it will be downloaded otherwise it will be added to the waiting list. | `$rm movie name`<br /><br />Optionally add <br />`--year=####` to filter results by year.<br /><br />In place of the movie title, you can also pass an IMDb ID. See [Deciding which ID to use](#deciding-which-id-to-use) to learn how to find the IMDb ID.<br />`$rm --id=tt0371746` |
|    import    | **Available to users authorized in configuration only!**<br/>This command is used to import media from the import folder on the filesystem into the media library and add it to the database. Please see the section on naming import media for information on how to name media to be imported by this command. | `$import`<br /><br />Optionally if you do not want to wait for Syncthing to finish syncing (if syncthing integration is enabled) the pass the skip-sync flag:<br />`$import --skip-sync`<br /><br />Additionally, if a media file already exists in the media library, you can overwrite the file by passing the overwrite flag:<br />`$import --overwrite` |
|   shutdown   | **Available only to the bot owner as defined in the configuration only!**<br/>Perform a clean shutdown of the bot by ending all tasks and disconnecting from the Discord API gateways. Please not that this does not cleanly stop any files that are being transferred so use with caution! | `$shutdown`                                                  |

<br>

### 3. Naming import media

The import folder must contain two sub-folders, `movies` and `episodes`. Files related to TV shows/episodes must be placed inside the episodes file while files related to movies must be placed within the movies folder.

###### 3-a. Deciding which ID to use:

When working with the bot for importing media (or requesting movies by their ID), it is important to make sure you have the correct ID for that type of media. For anything related to movies (both video and subtitle files), it is important to use ID's retrieved from [IMDb](https://www.imdb.com). If you are working with episode (video and subtitle) files for a TV series, you must use the ID retrieved from [TVDB](https://www.thetvdb.com/).

To fetch a ID from IMDb, first you must locate the movie you are searching for. Once you have located the movie on IMDb, you must go to its listing page. From there you can locate the ID between the two `/` symbols following the word `title`. You must use everything between those two symbols. For example, if you have the following link for **Iron Man**: `https://www.imdb.com/title/tt0371746/?ref_=fn_al_tt_1` then your ID would be `tt0371746`.

To locate a ID from TVDB, you must first locate the TV show you are searching for. Once you have located the TV show, scroll down to the seasons section on the right navigation bar and select the season the episode you are searching for is in. Finally, click on the link to the episode you are whishing to obtain an ID for. The ID for the episode should be the last item in the URL bar. For example, if you have the following link for season 1, episode 1 of Arrow: `https://www.thetvdb.com/series/arrow/episodes/4325893` then your episode ID will be `4325893`.

###### 3-b. Naming video files:

When naming your video files to put into the import folder, you need to know what type of video file you have (is it an episode or a movie). If the file is for a movie, then simply remove everything in front of the file extension and replace it with its IMDb id. Make sure you relocate the file to the movie folder within the import folder.

If your file is for a episode, then simply remove everything in front of the file extension and replace it with its TVDB id. Make sure you relocate the file to the episode folder within the import folder.

###### 3-c. Naming subtitle files:

Naming subtitle files is a bit more involved than naming your movie or episode video files. Like th above, it is important to know what type of media the subtitle is for (episode versus movies). Once you know the type of file, you can obtain the propler ID for the file based on its IMDb or TVDB entry.

Once you know the ID of the file, you must next determine the language of the subtitle file. Once you know what language it is, lookup its [ISO-639-2/B code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) (3 letters).

Next, you need to determine if the file is a forced subtitle, CC subtitle, or SDH subtitle or a combination of forced and CC or SDH. A forced subtitle is generally a subtitle that will only contain text for parts of a video that is outside of the common language of the file. Think Jabba the Hut in Star Wars. If a subtitle is SDH or CC it will often contain extra information about what is occuring such as descriptions of music playing. These are subtiles that are for those who are deaf or hard of hearing.

Once you know all the above information it is time to assemble the file name. Each section of the filename is seperated by a `.` character. The first section is going to be the ID of the media. Next, you need to add the three letter language code. Next, if the subtitle is forced, you need to add the word `forced`. Finally, if the subtitle is a SDH subtitle or a CC subtitle, you need to add `shd` or `cc` accordingly. Don't forget to add the files original file extension as well.

Below are some example subtitles for the 2021 movie Luca:

- `tt12801262.eng.srt`
- `tt12801262.eng.forced.srt`
- `tt12801262.eng.sdh.srt`
- `tt12801262.spa.forced.cc.srt`

<br>

### 4. Required Channels

Below is a table that describes all the channels that Plexbot requires in order to properly function. You can change the name of the channels if you wish, since when you configure the plexbot, you will use the ID of the channel and put it in the configuration file for the bot. Also, listed below are the suggested permissions for each channel. The server administrator can be a role or an individual user and represent the person who can make decisions about what can go in the media library and administer the bot.

| **Channel Name**  | **Channel Description**                                      | **Suggested Permissions**                                    |
| :---------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| upgradable-movies | This channel is used to list the movies that can be upgraded to a new resolution. | Server Administrator:<br />`Read, Send, Edit, React, Delete`<br /><br />Plexbot:<br />`Read, Edit, Send, React, Delete`<br /><br />Others:<br />`None` |
| upgraded-movies   | This channel is used to notify users that a movie has been upgraded to a better resolution. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Edit, Send, Delete`<br /><br />Others:<br />`Read` |
| bot-status        | This channel is used by the bot to display its current status message. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| new-movies        | This channel is used to notify users that a movie has been added to the library. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| new-episodes      | This channel is used to notify users that a new TV episode has been added to the library. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| waiting-list      | This channel is used to list the movies that are in the waiting list. | Server Administrator:<br />`Read`<br /><br />Plexbot:<br />`Read, Send, Edit, Delete`<br /><br />Others:<br />`Read` |
| movie-request*    | This channel is used by users to request that movies be downloaded. | Server Administrator:<br />`Read, Send, React, Edit, Delete`<br /><br />Plexbot:<br />`Read, Send, React, Edit, Delete`<br /><br />Others:<br />`Read, Send, React, Edit (self), Delete (self)` |

*= *This channel is optional, if you have other text channels that a user can make requests in or such, simply make sure the bot has the permissions listed for this channel on whatever channel(s) you want to use. Additionally, the bot does not need to know the ID of this channel.*

<br>

### 5. Bot Configuration

The bot requires a `application.yaml` to be located in a folder called `config` which should be next to the bot binary file in order to run. If running this from an IDE in development mode, the file should instead be placed inside the resources folder as `application.yaml`. Below is an example configuration file. Please note that the values provided are **examples** and will not work. You will need to customize them yourself.

Example file structure of bot installation to the root users home directory on linux:

```
/
|- root
   |- plexbot-runner
   |- config
      |- application.yaml
```



***This file should be automatically generated the first time you run the bot. Once it is generated you will need to modify the values in the file to match your environment before you can run the bot.***

```yaml
####################
### Bot Settings ###
####################
BotSettings:
  # The bot token, this is obtained from the Discord Developer Portal
  token: thisissomerandomediscordbotapitokendonotuseit

  # The name of the bot displayed in all messages
  name: Plexbot

  # The prefix the bot will use to listen for commands. Must be contained within ""
  prefix: "!"

  # The ID of the user that owns the bot
  ownerID: 012345678901234567

  # URL of Image displayed in place of movies that do not have a poster image
  noPosterImageUrl: https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/1024px-No_image_available.svg.png

  # Level at of which the bot will start displaying logging information. Valid logging
  # levels are as follows in order from most output to least output:
  #  - ALL
  #  - TRACE
  #  - DEBUG
  #  - INFO
  #  - WARN
  #  - ERROR
  #  - FATAL
  #  - OFF
  logLevel: INFO


#########################
### Database Settings ###
#########################
DatabaseSettings:
  # The IP or URL of the server hosting the bots database
  address: 127.0.0.1

  # The database server port
  port: 3306

  # The name of the database the bot should use
  name: plexbot

  # The username the bot will use to access the database
  username: plexbot

  # The password the bot will use to access the database
  password: somepassword

  # Configure how to handle updating and creating the database scheme. Valid options
  # are as follows. Please make sure you understand the implications of your selected option.
  #  - none: Take no action, this will require you to manually load a database
  #          schema if one does not exist in the database.
  #
  #  - create: Create a blank database scheme if one does not exist. This will
  #            only occur if the database is currently empty.
  #
  #  - drop-and-create: Drop the existing database and create a new empty one. Use this
  #                     with caution as it will remove all data in an existing database.
  #
  #  - update: Use a best-effort to update the existing database to match what is defined in the
  #            application code. If a change that could cause data-loss is defined, the application
  #            will fail to startup and the database will require manual migrations to the expected schema.
  generationStrategy: update


###########################
### Channel ID Settings ###
###########################
ChannelSettings:
  # The ID of the channel for listing movies that can be upgraded to a better resolution
  upgradeApprovalChannel: 012345678901234567

  # The ID of the channel for notifications about movies that have been upgraded to a better resolution
  upgradeNotificationChannel: 012345678901234567

  # The ID of the channel where the bots status message will be located
  botStatusChannel: 012345678901234567

  # The ID of the channel for notifications about movies that have been added to the library
  newMovieNotificationChannel: 012345678901234567

  # The ID of the channel for notifications about new TV episodes that have been added to the library
  newEpisodeNotificationChannel: 012345678901234567

  # The ID of the channel for notifications about movies that have been added to the waiting list
  movieWaitlistChannel: 012345678901234567


#######################
### Folder Settings ###
#######################
# IMPORTANT: All file paths must end with a trailing / in order for the bot to properly work
FolderSettings:
  # The folder that the movies are stored in
  movieFolder: /path/to/movie/folder/

  # The folder that the TV shows are stored in
  tvFolder: /path/to/tv/folder/

  # The folder where media to be imported into the libraries is stored in
  # The import folder should be empty except two folders inside:
  #   - movies
  #   - episodes
  # Place movies that need to get imported into the movies folder and episodes
  # that need to get imported within the episodes folder.
  importFolder: /path/to/import/folder/

  # The folder that is used for temporary file operations
  tempFolder: /path/to/temp/folder/

  # Allows you to customize what is allowed in filenames or not. Items listed under the remove
  # section are simply removed. Items under the replace section are replaced with the content of
  # the associated replacement string.
  blacklistedCharacters:
    remove:
      - ">"
      - "<"
      - ":"
      - "\""
      - "/"
      - "\\"
      - "|"
      - "?"
      - "*"
      - "."
      - "’"
      - "…"
    replace:
      - original: "·"
        replacement: "-"
      - original: "–"
        replacement: "-"
      - original: "Æ"
        replacement: "Ae"
      - original: "Я"
        replacement: "R"
      - original: "æ"
        replacement: "ae"


###############################
### Import Manager Settings ###
###############################
ImportSettings:
  # List of users (by ID) that are allowed to use the import command
  authorizedUsers:
    - "012345678901234567"
    - "012345678901234567"

  # List of files to ignore in the import folder during the import process
  # This is matched by the end of a filename, so an entry of ".srt" below would
  # remove any file that ends with ".srt' from the import process. Additionally,
  # hidden files on a system are automatically ignored.
  ignoredFiles:
    - "some-file.mp4"
    - ".stignore"


##################################
### Plex Media Server Settings ###
##################################
PlexSettings:
  # The Client Identifier used to identify individual devices on a Plex account. This needs to be a unique
  # UUID. You can generate one with the following commands:
  #   Windows (Powershell): [guid]::NewGuid()
  #   macOS and Linux: uuidgen
  clientIdentifier: 034CFB4E-F1C6-4518-A162-C4DB9E00E363

  # The protocol (http/https), IP/DNS address, and port number of the Plex server to use
  # connect to when refreshing media. Example address: http://127.0.0.1:32400
  address/mp-rest/url: "http://127.0.0.1:32400"

  # Username to your plex.tv account
  username: username

  # Password to your plex.tv account
  password: password


######################################
### SyncThing Integration Settings ###
######################################
SyncthingSettings:
  # Should the SyncThing integration be enabled? This prevents the bot from modifying media
  # if the system is syncing data between folders. Currently this is only used to prevent the
  # bot from importing media from the import folder if a sync is in progress.
  enabled: true

  # The protocol (http/https), IP/DNS address, and port number of the SyncThing server to use as
  # an entrance to the SyncThing cluster. This can be any SyncThing node in the cluster.
  # Example address: http://127.0.0.1:8384
  address/mp-rest/url: "http://127.0.0.1:8384"

  # The ID of the import folder as displayed on SyncThing
  importFolderId: plexbot-import

  # The ID of the movies folder as displayed on SyncThing
  movieFolderId: plex-movies

  # The ID of the TV folder as displayed on SyncThing
  tvFolderId: plex-tv

  # The ID's of the devices that the bot should monitor for sync progress
  devices:
    - "0123456-0123456-0123456-0123456-0123456-0123456-0123456-0123456"
    - "9876543-9876543-9876543-9876543-9876543-9876543-9876543-9876543"


################
### API Keys ###
################
ApiKeys:
  # The API key for all requests to the OMDb API for getting IMDB OmdbMovie ID's.
  # You can get an API ket for this at http://www.omdbapi.com/ however it is recommended to purchase
  # at least the Basic plan through https://www.patreon.com/join/omdb to avoid API key limits.
  omdbApiKey: somerandomeapikeygoeshereotherwiseitwontwork

  # Private API key for real-debrid which is where the movies get downloaded from
  # In order to get an API key, you will need to register an account at https://real-debrid.com
  # and purchase a premium offer. Generally the 180 day plan is the best deal. Once purchased, go
  # to https://real-debrid.com/apitoken to retrieve your token. You will need to purchase more time
  # before your plan expires otherwise the bot will not function.
  realDebridKey: somerandomeapikeygoeshereotherwiseitwontwork

  # The API key to access the SyncThing server defined in the SyncThing settings block. This API key
  # can be found on your SyncThing server UI under the Advanced Settings menu (Actions -> Advanced -> API Key).
  syncthingApiKey: somerandomeapikeygoeshereotherwiseitwontwork

  # The API key for all requests to TheTVDB API. You can get this key from https://thetvdb.com and will
  # need to register to get a v4 API key. This is done once for the application.
  tvdbApiKey: somerandomeapikeygoeshereotherwiseitwontwork

  # The unique subscriber PIN code for TheTVDB API. This is retrieved from a users dashboard on https://thetvdb.com
  # and is required in addition to the API key above.
  tvdbSubscriberPin: somepin
```

<br>

### 6. Database Creation

In order to run the plexbot, you must have a MariaDB database created for the bot to utilize. Make sure the account you will use for the bot to access the database has all permissions except the grant permission. You do not need to populate the database, as that will be done for you the first time the bot starts up. The database can either be run locally on the same machine as the bot or on an external server.

<br>

### 7. Running the bot

When running the bot you have several options. The first option is to simply run it manually by starting the JAR file. This is the simplest option, but if the bot crashes, or the user account logs out the bot will be stopped as well. The second option is to create and register a service for the bot.

###### 7-a. System Requirements:

The plexbot was designed as lightweight as possible when it comes to resources, and contains all libraries it requires are packaged within itself except from the database. In order to run, you need the following installed.

- MariaDB Database Server *(can be a remote server)*

###### 7-b. Running manually:

Running the bot as a JAR file is simple and straight forward. Simply make sure the bot and the config directory containing the `application.yaml` file reside within the same directory and then run the following command *(replace the version if necessary)*:

```bash
./plexbot-runner
```

###### 7-c. Running as a service:

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
   ExecStart=/path/to/plexbot-runner
   WorkingDirectory=/path/to/directory
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

   