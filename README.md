# Running the application

1. Build the application: `./gradlew build`
2. Build and run the container: `docker compose up -d`

# Running tests

* Run tests with this command:
`./gradlew check clean`
* Test summary can be found running this command:
`open build/reports/tests/test/index.html`

# Verifying

* The application has only one endpoint: `GET /api/v1/format/human-readable`
* There's a [`requests.http`](./requests.http) file containing a few example requests that can be ran in IntelliJ IDEA
* Alternatively:
  `curl -XPOST -H "Content-Type: application/json" -d @assignment-example.json http://localhost:8080/api/v1/format/human-readable`

# Task

## Assumptions

* We assume the data is valid, as in it contains only the mentioned keys (days) and the opening times are accurately sorted in an ascending order
    * If the data is not valid then it's quite unclear whether we can even handle that - how would we know when to open and close?
* It's unclear whether the restaurant can be open over the span of multiple days.
  * We assume it cant and trigger an error.    
* Restaurant opening sunday and closing monday could be considered a special case that wasn't mentioned. 
  * We're acting with this possibility in mind and make it work.
* It's unclear how the application should act when the request came in somewhat malformed: missing days or days are in an incorrect order
  * Perhaps the callee might benefit from receiving an error in this case... or not?
  * I believe it's better to fail gracefully unless otherwise requested/agreed upon.

## Considerations

* When displaying the data should we consider daylight savings that might occur?
    * Probably not, because we don't know *when* the request is made and in *what* country
    * Might lead to more confusion than benefits.
    * Depending on the use context might want to bring this up.
* It's unclear what to do if the request came in with an empty map or is incomplete, so we fail gracefully.
 
## Improvement ideas

* Localisation should probably be considered. The app is more likely to be multilingual than not, hence we should consider locale when displaying day names and time format.
    * US locale is hardcoded at the moment
    * At least make this configurable in the API request. 
* Formatter might follow a certain interface contract in the future -- the result from the `formatSchedule()` function might be something other than a string, then the interface should be a generic interface
  * At the moment that would unnecessarily overcomplicate the code, but it's a thought.

## Final thoughts

* The original format is friendlier to humans to read but quite unfriendly to work with.
* The format almost always requires some kind of normalisation before it can be worked with.
* A flatter structure would be a lot better.
* Overall the task was quite interesting and challenging, the assignment itself is written quite clear and understandable with clear goals and expectations - that's definitely a plus.
