# Macaron

Android base project and lib center.

## Setting up new library module

After creating new module, also create property files below;

### developer.properties
Create new properties file for developer details called `developer.properties` in **library module**. Then copy, paste and fill variables below; (These values can be get from Bintray under `View Profile`)

```
developerId=
developerName=
developerEmail=
developerOrganisation = 'hipo'
```

### library.properties
Create new properties file for library details called `library.properties` in **library module**. Then copy, paste and fill variables below;

```
artifact=
libraryName=
libraryVersion=
libraryDescription=
```
* **artifact**: Second part of implementation Ex: com.hipo.macaron:[ARTIFACT]:1.0.0. Usually same as library name.
* **libraryName**: Module name of the library.
* **libraryVersion**: Library version, ex: 1.0.0
* **libraryDescription**: Library description that will be shown in Bintray

### local.properties

Add bintray username and bintray api key to `local.properties` as shown below;

```
bintrayUser=
bintrayApiKey=
```

### build.gradle (library module)
Add snippet below at the end of the build.gradle file in library module.

```
// Uncomment the line below if you get Javadoc error while running `./gradlew install`
// tasks.withType(Javadoc).all { enabled = false }
apply from: 'https://raw.githubusercontent.com/Hipo/macaron/46272b328713c80bcbbf25e97431e647a16836de/publish/publish.gradle'
```

[publish.gradle](https://raw.githubusercontent.com/Hipo/macaron/46272b328713c80bcbbf25e97431e647a16836de/publish/publish.gradle) contains everything to check, install and upload to bintray easily. It uses properties defined in `developer.properties`, `library.properties`, `macaron.properties`, `license.properties` and `local.properties`

## Publishing

After settings up the module, run 3 commands below to publish it on Bintray;

* `./gradlew [MODULE_NAME]:checkProperties`: Checks if there is any missing property in created property files.
* `./gradlew [MODULE_NAME]:install`: Generates `aar`, `pom`, `sources.jar` and `javadoc.jar` files.
* `./gradlew [MODULE_NAME]:bintrayUpload`: Uploads generated files to bintray.

## Structure

```
Project
...
 |
 +-- library_module_1
 |   |
 |   |-- library.properties
 |   \-- developer.properties
 |
 +-- library_module_2
 |   |
 |   |-- library.properties
 |   \-- developer.properties
 |
 +-- macaron.properties
 |
 +-- license.properties
 |
 +-- local.properties
...
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)