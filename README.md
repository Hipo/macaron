# Macaron

Android base project and lib center.

## Setting up new library module

After creating new module, also create property files below;

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
* **libraryDescription**: Library description that will be shown in GitHub Packages

### local.properties

To publish manually, add GitHub username and GitHub Personal Access Token which permissions have `read:packages` and `write:packages` to `local.properties` as shown below;

```
gpr.usr=
gpr.key=
```


**OR**

Add `GPR_USER` and `GPR_API_KEY` secret variables to Bitrise if they are not already added and let the Bitrise handle publishing.



### build.gradle (library module)
Add snippet below at the end of the build.gradle file in library module.

```
apply from: 'https://raw.githubusercontent.com/Hipo/macaron/master/publish/publish.gradle'
```

[publish.gradle](https://github.com/Hipo/macaron/blob/master/publish/publish.gradle) contains everything to check, build and publish to GitHub Packages easily. It uses properties defined in `library.properties`, `macaron.properties`, `license.properties` and `local.properties`

## Publishing

To publish on GitHub Packages, push a tag to master as `[VERSION]-[MODULE_NAME]`

Example; `1.0.0-alpha03-biometricutils` will publish `biometricutils` module.

**Last part of the tag must be the module name**

Bitrise will run commands below based on the module name at the end of the tag;

* `./gradlew [MODULE_NAME]:checkProperties`: Checks if there is any missing property in created property files.
* `./gradlew [MODULE_NAME]:build`: Generates `aar`, `pom` files.
* `./gradlew [MODULE_NAME]:publish`: Uploads generated files to GitHub Packages.

## Structure

```
Project
...
 |
 +-- library_module_1
 |   |
 |   \-- library.properties
 |
 +-- library_module_2
 |   |
 |   \-- library.properties
 |
 +-- publish
 |   |
 |   |-- github-packages-publish-script.sh
 |   |-- license.properties
 |   |-- macaron.properties
 |   \-- publish.gradle
 |
 +-- local.properties
...
```

## Installing

To install one of these libraries, you must add this snippet below before module level `build.gradle`.

```
apply from: 'https://raw.githubusercontent.com/Hipo/macaron/master/packages.gradle'
```

Example implementation:

```
implementation com.hipo.macaron:[ARTIFACT]:<version>
```

[publish.gradle](https://raw.githubusercontent.com/Hipo/macaron/master/packages.gradle) contains required `gprReadApiKey` to need to able to be used these libraries.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
