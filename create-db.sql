SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
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

CREATE TABLE `Guilds`
(
    `guild_ID`            bigint(20)                              NOT NULL,
    `guild_name`          varchar(254) COLLATE utf8mb4_unicode_ci NOT NULL,
    `guild_prefix`        varchar(1) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `guild_creation_date` varchar(60) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `guild_join_date`     varchar(60) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `owner_id`            bigint(20)                              NOT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Movies`
--

CREATE TABLE `Movies`
(
    `movie_id`         varchar(12) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `movie_title`      varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_year`       varchar(4) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `movie_resolution` int(4) DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Upgrades`
--

CREATE TABLE `Upgrades`
(
    `movie_id`            varchar(12) COLLATE utf8mb4_unicode_ci NOT NULL,
    `upgraded_resolution` int(11)                                NOT NULL,
    `message_id`          varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Users`
--

CREATE TABLE `Users`
(
    `user_ID`            bigint(20) NOT NULL,
    `discriminated_name` varchar(254) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Table structure for table `Waitlist`
--

CREATE TABLE `Waitlist`
(
    `movie_id`     varchar(12) COLLATE utf8mb4_unicode_ci  NOT NULL,
    `movie_title`  varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
    `movie_year`   varchar(4) COLLATE utf8mb4_unicode_ci   NOT NULL,
    `requester_id` bigint(20)                              NOT NULL,
    `message_id`   varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

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

/*!40101 SET CHARACTER_SET_CLIENT = @OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS = @OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION = @OLD_COLLATION_CONNECTION */;
