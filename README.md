# Macaron

Macaron library description


# TODO
* Explain `publish.gradle` & apply from: 'publish.gradle', Maybe create raw.githubusercontent for publish.gradle
* Explain checkProperties, install, [moduleName]:bintrayUpload commands
* Write doc about local properties


## Publishing new library/module

After creating new module, also create property files below;

### developer.properties
Create new properties file for developer infos called `developer.properties` in library module. Then copy, paste and fill variables below; (These values can be get from Bintray under `View Profile`)

```
developerId=
developerName=
developerEmail=
developerOrganisation = 'hipo'
```

### library.properties
Create new properties file for library infos called `library.properties` in library module. Then copy, paste and fill variables below;

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

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)