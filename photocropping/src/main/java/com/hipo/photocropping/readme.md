# File Provider Documentation

 This ia a documentation about photo capturing without getting write/read permission

### 1. Create file provider in res/xml. If App has multiple variant, file provider should be created for each variant under related path.

 Example, has 2 variants called Prod & Staging;
```
 +--- app
      +--- src
           +--- main
           +--- prod
                +--- res
                     +--- xml
                          \--- file_provider.xml
           +--- staging
                +--- res
                     +--- xml
                          \--- file_provider.xml
```                          
```
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path
    name="files"
    path="Android/data/APP_PACKAGE_NAME_HERE/files/Pictures" />
</paths>
```

 ### 2. Add BuildConfig & ManifestPlaceholder variables into App build.gradle;
```
 android {
     defaultConfig {
     ...
         manifestPlaceholders = [FILES_AUTHORITY:"${applicationId}.provider"]
         buildConfigField "String", "FILES_AUTHORITY", "\"${applicationId}.provider\""
     ...
     }
 }
 ```

 ### 3. Add provider into AndroidManifest.xml
 ```
 <application>
    ...
    <provider
        android:name="androidx.core.content.FileProvider"
        android:authorities="${FILES_AUTHORITY}"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths"/>
     </provider>
    ...
 </application>
```


