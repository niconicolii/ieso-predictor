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


### TODO list
- [ ] complete mongodb
  - [ ] timestamp should be unique
- [ ] React website UI
  - [ ] setup React website
  - [ ] how to display demand chart
  - [ ] toggles to select different categories
- [ ] API for getting demand values
  - [ ] 5 minute
  - [ ] hourly
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
