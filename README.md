# ElderlyTrack-Android

## Design 

### Beacon detection

1. App ranges for beacons when app is in the foreground.
2. App monitors a few general regions when app is in the background. Upon detection, it will range for beacons int that region.  


### Regular downloading of missing beacons

1. App uses scheduled service to download missing beacons information in every 30 minutes. 


### Reporting of detected beacons

1. Upon detection of nearby beacons, app will filter them against missing beacons. It uses Map to keep unique beacon ID.
2. App appends newly-detected nearby missing beacons to a list waiting for reporting to server. And trigger app to get current GPS locaiton. 
3. Upon receiving GPS location, app will start processing waiting list by adding job (beacon info + GPS) to a queue. 
4. The queue will be processed in the background to report detected location to server. 


## To-do

1. Local notification upon detection of nearby missing resident
2. Flash screen upon starting of app
3. About app and About us screen

## App Release

App is signed by android_npsoeapps.keystore


## Key 3rd Party Libraries

1. AltBeacon library `org.altbeacon:android-beacon-library`
    * Used for beacon detection
2. Priority Queue `com.birbit:android-priority-jobqueue`
    * Detected beacons are reported to server one by one. They are queued in the background before sending to server.
3. ButterKnife `com.jakewharton:butterknife`
    * For easy injection of UI components
4. Retrofit `com.squareup.retrofit2`
    * For working with API
    * Used together with EventBus for async comm with other Android components

