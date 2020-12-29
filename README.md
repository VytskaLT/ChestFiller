# ChestFiller
A library for filling chests with specified loot.
## Config
A config is JSON data that contains entries with items. Here's an example:
```json
[
  {
    "min": 8, // The minimum amount of items there will be in the chests. Can be negative. If not set, defaults to 0.
    "max": 32, // The maximum amount of items there will be in the chests. If not set, defaults to 1.
    "items": [
      "SNOW_BALL",
      "EGG"
    ]
  }
]
```
If you want to add enchantments or set the damage value of items, you can do this:
```json
[
  {
    "items": [
      "BOW",
      {
        "id": "BOW",
        "data": 0, // Damage value
        "enchantments": {
          "ARROW_DAMAGE": 1
        }
      }
    ]
  }
]
``` 
## Parse a config
There are a few ways of parsing a config:
```java
JsonElement element = ...;
Entry[] entries = ChestFiller.parse(element);
```
```java
Entry[] entries = ChestFiller.parse("json data here");
```
If the returned value is null, that means that the config was invalid.
```java
if (entries == null) {
    // Config is invalid
} else {
    // Config is valid
}
```
## Filling a chest
```java
boolean result = ChestFiller.fill(entries, listOfChests);
if (result) {
    // The fill was successful
} else {
    // The fill was unsuccessful. May happen due to the chest being full.
}
```
It automatically clears the inventories of the chests before filling them. The first fill can take up to 3 milliseconds because it will initialise SplittableRandom. If that is a problem for you, the random can be initialised at any time by:
```java
ChestFiller.initRandom();
```
I recommend you call that when your plugin is enabled.
## Installation
I don't have a public maven repository, so just copy [the class](https://github.com/VytskaLT/ChestFiller/blob/master/src/main/java/net/VytskaLT/ChestFiller/ChestFiller.java) to your project. You'll also need the Lombok dependency, so install it if you didn't have it already.
