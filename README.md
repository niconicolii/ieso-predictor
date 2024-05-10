# ieso-predictor

### How to run:
1. git clone https://github.com/niconicolii/ieso-predictor.git
2. open `ieso-predictor` project using IntelliJ
3. run `docker-compose.yaml`, neccessary images will be downloadd if not already, and start running
   - monitor message broker on `http://localhost:15672/`
     - username = `guest`; password =`guest`
   - monitor mongodb on `http://localhost:8081/`
     - username = `admin`; password = `pass`
4. run `sink-save-to-db/src/main/java/com/nico/sink/SaveToDBApplication.java`
5. run `processor-parse-xml/src/main/java/com/nico/processor/ParseXmlApplication.java`
6. run `source-from-ieso/src/main/java/com/nico/source/SourceApplication.java`
7. DemandData objects will be stored in `iesodemand` database's `demandData` collection


### Notes & Planning on Notion
``` https://www.notion.so/IESO-Predictor-Notes-Planning-c5ada95c7ac54a9f8a9845368f9866c3?pvs=4 ```


### Project TODO list
- [ ] complete mongodb
  - [x] timestamp should be unique
  - [ ] how to minimize number of inserts AND make sure no value is missing, at the same time
    - idea: only check data for the whole day when getting the very last data for the day, pulish if it's the same day as maximu, let sink do the search work
    - need to think more about this b/c data not updated one by one (maybe a few for once, and then no update for 15 mins)
  - [ ] different DB users for different purposes? SCS user for adding new datas in DB vs. Website accessing datas
- [ ] React website UI
  - [ ] setup React website
  - [x] how to display demand chart
  - [x] toggles to select different data granularity/frequency
  - [x] get demand data corresponding to user inquiry
  - [x] get notified when DB updated
- [ ] API for getting demand values
  - [ ] what are the selections/categories?
    - 5 minute / hourly / daily
    - start date <==> end date; default duration
  - [x] 5 minute
  - [x] hourly
  - [ ] daily peak for different time duration
  - [ ] monthly day peak
- [ ] AI modeling works
  - [ ] add DB to AWS S3
  - [ ] sagemaker modeling
  - [ ] display predicted demand data on website
- [ ] Weather data integration
  - [ ] get data using API/someway
  - [ ] add weather display on website
  - [ ] integrate weather data into DB into AWS S3
  - [ ] integrate weather data into sagemaker
  - [ ] display predicted weather data on website

### March 18, 2024 notes
- created toggles for start/end date, and check box for different granularities
- App.jsx is able to get checkbox and date selections from Toggle.jsx
- next steps:
  - API need to allow input of date and granularities
  - react send request according to date and gran selections
    - default request on mount
    - send new request onChange
    - repeat request every 5 minutes (?)
  - more granularities including data analysis


### March 19, 2024 notes
- [x] ** planning to switch from springMVC to Spring WebFlux
  -  it makes more sense to use reactive approach b/c website should update when new data in DB appears
- [x] studied Spring WebFlux

### March 20, 2024 notes
- [x] learn about Reactive MongoDB
  - [x] learnt ChangeStreamRequest - but found out not what I wanted -> event-driven but still imperative programming
  - [x] learnt MongoDB Reative Stream Driver, create publisher through collection.watch(filteringPipeline)
- [x] modify mongodb container setup into running as part of replica set => required by MongoDB reactive stream
- [x] setup mongodb listener to publish database update notifications
- [x] push SSE to react app for continuous data sending

### March 21, 2024 notes
- [ ] need to cleanup webflux/website packages  => left for learning purposes
- [x] API add date input
- [x] react functions add date in request address
  - [x] default request
  - [x] new request on change
  - [x] repeat request every 5 minutes (?) => NOPE! totally on reactive approach 
    - backend will notify UI when DB updated
- [x] formalized line chart display

### March 22, 2024 notes
- [x] start dealing with weather data
  - [x] choose weather api - OpenWeatherMap
  - [x] learn about weather api

### March 23, 2024 notes
- [x] create app for getting historical hourly data  ==> need to get few (40 days for one country) every day b/c 1000 calls limit

### March 24, 2024 notes
- [ ] need to save mongodb to S3 every day
  - [ ] start looking at AWS (S3 & Sagemaker)
- [ ] connection to sagemaker
- [ ] how to get IESO historical data


