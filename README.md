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

- **Waiting List:** Allows the bot to periodically (1x/hr) check to see if a move is now avaialble for download. A movie is automatically added to the waiting list if a user uses the request command and the bot is unable to find the movie at the time. During its hourly check, if a movie is found to have become available, the bot will automatically download the movie that was requested and then send the user that requested it a message stating the movie was added.
- **Resolution Upgrader:** Simply adding a movie is often not the end of the road for a movie. Occassionally a new version of a movie that has already been downloaded to the server becomes available. If a new version that has a higher resolution that what is current downloaded does become available, the bot is capable of informing the administator that there an upgrade is available. If the administrator replies with a :thumbsup: reaction, the bot will then download the new version of the movie to replace the old one.

<br>

### Required Channels

Below is a table that describes all the channels that Plexbot requires to function. You can change the name of the channels if you wish, since when you configure the plexbot, you will use the ID of the channel and put it in the bots configuration file. Also described are the suggested permissions for the channels. The server administator can be a role or an individual user and represent the person who can make decisions about what can go in the media library and administer the bot.

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

  # The folder that the movies will be downloaded to
  movieDownloadFolder: /path/to/movie/folder

  # URL of Image displayed in place of movies that do not have a poster image
  noPosterImageUrl: https://upload.wikimedia.org/wikipedia/commons/thumb/a/ac/No_image_available.svg/1024px-No_image_available.svg.png

  # Base URL of the current YTS domain to look at for API requests
  # At the time of this commit, this is the current domain so this can be left as is
  currentYtsDomain: yts.mx

  # The ID of the channel for listing movies that can be upgraded to a better resolution
  upgradableMoviesChannelId: 012345678901234567

  # The ID of the channel for notifications about movies that have been upgraded to a better resolution
  upgradedMoviesChannelId: 012345678901234567

  # The ID of the channel where the bot's status message will be located
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
  ipAddress: 127.0.01

  # The database server port
  port: 3306

  # The name of the database the bot should use
  dbName: Plexbot

  # The username the bot will use to access the database
  username: Plexbot

  # The password the bot will use to access the database
  password: somepassword


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

In order to run the plexbot, you must have a database created for the bot to utulize. Currently the bot does not generate the database itself, but requires it to be created for it. This may change in the future, but for now you will need to execute the SQL script below on your database server to generate the database. Once the database is generated, create an account for the bot to access the database with, give it permissions to the database *(everything except grant permissions)* and add the database information to the configuration file created above.

This script is also saved in the repository as `create-db.sql`

You can change the name of the database that will be created by modifying lines 15 and 16 of this script to replace the `Plexbot` text with the new name of the database.

```mysql
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `Plexbot`
--
CREATE DATABASE IF NOT EXISTS `Plexbot` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `Plexbot`;

-- --------------------------------------------------------

--
-- Table structure for table `Guilds`
--

CREATE TABLE `Guilds` (
  `guild_ID` bigint(20) NOT NULL,
  `guild_name` varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guild_prefix` varchar(1) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guild_creation_date` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `guild_join_date` varchar(60) COLLATE utf8mb4_unicode_ci NOT NULL,
  `owner_id` bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Movies`
--

CREATE TABLE `Movies` (
  `movie_id` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `movie_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `movie_year` varchar(4) COLLATE utf8mb4_unicode_ci NOT NULL,
  `movie_resolution` int(4) DEFAULT NULL,
  `movie_filename` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Upgrades`
--

CREATE TABLE `Upgrades` (
  `movie_id` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `upgraded_resolution` int(11) NOT NULL,
  `message_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Users`
--

CREATE TABLE `Users` (
  `user_ID` bigint(20) NOT NULL,
  `discriminated_name` varchar(254) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Waitlist`
--

CREATE TABLE `Waitlist` (
  `movie_id` varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
  `movie_title` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `movie_year` varchar(4) COLLATE utf8mb4_unicode_ci NOT NULL,
  `requester_id` bigint(20) NOT NULL,
  `message_id` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `Guilds`
--
ALTER TABLE `Guilds`
  ADD PRIMARY KEY (`guild_ID`),
  ADD KEY `fkIdx_36` (`owner_id`);

--
-- Indexes for table `Movies`
--
ALTER TABLE `Movies`
  ADD PRIMARY KEY (`movie_id`);

--
-- Indexes for table `Upgrades`
--
ALTER TABLE `Upgrades`
  ADD PRIMARY KEY (`movie_id`),
  ADD KEY `fkIdx_42` (`movie_id`);

--
-- Indexes for table `Users`
--
ALTER TABLE `Users`
  ADD PRIMARY KEY (`user_ID`);

--
-- Indexes for table `Waitlist`
--
ALTER TABLE `Waitlist`
  ADD PRIMARY KEY (`movie_id`),
  ADD KEY `fkIdx_33` (`requester_id`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `Guilds`
--
ALTER TABLE `Guilds`
  ADD CONSTRAINT `FK_36` FOREIGN KEY (`owner_id`) REFERENCES `Users` (`user_ID`);

--
-- Constraints for table `Upgrades`
--
ALTER TABLE `Upgrades`
  ADD CONSTRAINT `FK_42` FOREIGN KEY (`movie_id`) REFERENCES `Movies` (`movie_id`);

--
-- Constraints for table `Waitlist`
--
ALTER TABLE `Waitlist`
  ADD CONSTRAINT `FK_33` FOREIGN KEY (`requester_id`) REFERENCES `Users` (`user_ID`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
```

<br>

### Running the bot

When running the bot you have several options. The first option is to simply run it manually by starting the JAR file. This is the simplest option, but if the bot crashes or the user account logs out the bot will be stopped as well. The second option is to create and register a service for the bot.

###### System Requirements:

The plexbot was designed to be as lightweight as possible when it comes to resources, and contains all libraries it requires to run packaged within itself except from the database. In order to run, you need the following installed.

- Java Runtime Enviroment *(11 or above)*
- MariaDB Database Server *(can be a remote server)*

###### Running as a JAR:

Running the bot as a JAR file is simple and straight forward. Simply make sure the bot and the `config.yaml` file are located within the same directory and then run the following command *(replace the version if necessary)*:

```bash
java -jar plexbot-1.0.jar
```

###### Running as a service:

The second method of running the bot is as a service. There are different ways to do this for each operating system, so for the sake of simplicity this document covers registering a systemd service for operating systems such as Ubuntu and Debian.

1. Navigate to your computer/servers systemd service folder:

   ```bash
   cd /etc/systemd/system/
   ```

2. Create a the service file for the bot: 

   ```bash
   nano plexbot.service
   ```

3. Paste the following content into the file. Make sure to update the **WorkingDirectory** field to match the root folder of your bot. When you are finished, save and close the file.

   ```bash
   [Unit]
   Description=Plexbot Discord Bot
   After=network-online.target
   
   [Service]
   ExecStart=/usr/bin/java -jar plexbot-1.0.jar
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

   