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
developerOrganisation=hipo
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

To publish manually, add bintray username and bintray api key to `local.properties` as shown below;

```
bintrayUser=
bintrayApiKey=
```


**OR**

Add `BINTRAY_USER` and `BINTRAY_API_KEY` secret variables to Bitrise if they are not already added and let the Bitrise handle publishing.



### build.gradle (library module)
Add snippet below at the end of the build.gradle file in library module.

```
apply from: 'https://raw.githubusercontent.com/Hipo/macaron/master/publish/publish.gradle'
```

[publish.gradle](https://github.com/Hipo/macaron/blob/master/publish/publish.gradle) contains everything to check, install and upload to bintray easily. It uses properties defined in `developer.properties`, `library.properties`, `macaron.properties`, `license.properties` and `local.properties`

## Publishing

To publish on Bintray, push a tag to master as `[VERSION]-[MODULE_NAME]`

Example; `1.0.0-alpha03-biometricutils` will publish `biometricutils` module.

**Last part of the tag must be the module name**

Bitrise will run commands below based on the module name at the end of the tag;

* `./gradlew [MODULE_NAME]:checkProperties`: Checks if there is any missing property in created property files.
* `./gradlew [MODULE_NAME]:install`: Generates `aar`, `pom` files.
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
 +-- publish
 |   |
 |   |-- bintray-publish-script.sh
 |   |-- license.properties
 |   |-- macaron.properties
 |   \-- publish.gradle
 |
 +-- local.properties
...
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)