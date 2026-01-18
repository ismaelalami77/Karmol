-- MySQL dump 10.13  Distrib 8.0.43, for macos15 (arm64)
--
-- Host: localhost    Database: karmol
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `productName` varchar(150) NOT NULL,
  `productPrice` decimal(10,2) NOT NULL,
  `category_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,'Black Coffee',12.00,3,139),(2,'White Coffee',15.00,3,1),(4,'Pistachio',16.00,4,96),(5,'Almonds',5.00,5,69),(6,'Cashews',7.00,5,87),(7,'Peanuts',4.50,5,110),(8,'Mixed Nuts',12.00,5,49),(9,'Sunflower',7.50,7,13),(10,'Pumpkin',16.00,7,92),(11,'Watermelon',16.00,7,34),(12,'Dates',35.00,8,7),(13,'Stuffed Dates',50.00,8,17),(14,'banana',8.00,9,43),(15,'Apple',19.00,9,42),(16,'Strawberry',22.00,9,99),(17,'Kiwi',35.00,9,91),(18,'White Chocolate',17.00,10,99),(19,'Dark Chocolate',18.00,10,98),(20,'Beans',12.00,3,49),(21,'Ground',16.00,3,40),(22,'Nescafe',17.00,3,53),(23,'instant coffe',2.00,3,529),(24,'candy',12.00,11,149),(25,'Toffee',17.00,11,91),(26,'Black Tea',8.00,12,12),(27,'Green Tea',15.00,12,15),(28,'Herbal Tea',17.00,12,21),(29,'White Tea',20.00,12,13),(30,'Rooibos Tea',26.00,12,9),(31,'Oolong Tea',36.00,12,5),(32,'AllSpice',14.48,13,62),(33,'Anise',13.09,13,55),(34,'Asafoetida',22.49,13,68),(35,'Bay Leaves',6.24,13,51),(36,'Caraway Seed',10.25,13,20),(37,'Cardamom',34.50,13,44),(38,'Cayenne Pepper',10.45,13,45),(39,'Chia Seeds',8.31,13,151),(40,'Cinnamon',11.09,13,67),(41,'Garlic Powder',5.11,13,62),(42,'Mustard Seeds',4.87,13,21),(43,'Loomi',16.25,13,45),(44,'Chili Flakes',6.68,13,43),(45,'Peanuts',8.05,4,168),(46,'Pecans',33.78,4,83),(47,'Cashews',25.68,4,24),(48,'Chestnuts',16.89,4,124),(49,'Almonds',24.35,4,103),(50,'Soy Nuts',7.22,4,67),(51,'Walnuts',18.10,4,130),(52,'Macadamia',41.47,4,33),(53,'Hazelnuts',18.46,4,29);
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-01-18 18:57:17
