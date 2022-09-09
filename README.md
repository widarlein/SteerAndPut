# SteerAndPut
Android application for Styr&amp;Ställ, a system of rental bikes in Gothenburg, Sweden

# Deprecation Notice
This app isn't deprecated per se, but development has stopped for sure. When the system moved to Nextbike and the new bikes, the open API from Gothenburg was updated to version 2, but with a few problems. The v2 of the API is just a lazily updated v1 that doesn't really fit the domain data anymore. Some bureaucrat has decided that the Open Data API must persist and then the people responsible for the IT solution pulls some data from Nextbike and smashes it into the existing API. I was in contact with the project manager of the migration but it was clear that they had no idea what data came and was relevant from Nextbike. 

TL;DR the app still talks to the API, but the API is bad with no intention to change so the app is de facto deprecated. The Nextbike app is pretty good though, so the original reason for making SteerAndPut is moot.

## Building 
Build with gradle as a standard android app
```
./gradlew :steerandput:assembleDebug
```
Or from Android Studio as usual.

## API Keys
Insert your API keys in the fields `BICYCLESERVICE_API_KEY`and `MAPS_API_KEY`in *gradle.properties* or override them in *~/.gradle/gradle.properties*

## Contributing
Please feel free to fork the project and send me a pull request, which I will review!
