# Running the application

1. Build the application: ```./gradlew build```
2. Build and run the container: ```docker-compose up -d```

# Running tests

* Run tests with this command:
```./gradlew check clean```
* Test summary can be found running this command:
```open build/reports/tests/test/index.html```

# Verifying

* The application has only one endpoint: `GET /v1/api/format/human-readable`
* There's a `requests.http` file containing a few example requests that can be ran in IntelliJ IDEA

# Task

## Assumptions
* We assume the data structure is valid, as in the example in the task is of valid structure and properly sorted.
    * If the data is not valid then it's quite unclear whether we can even handle that - how would we know when to open and close?
* It's unclear whether the restaurant can be open over the span of multiple days.
  * We assume it cant and trigger an error.    
* Restaurant opening sunday and closing monday could be considered a special case that wasn't mentioned. 
  * We're acting with this possibility in mind and make it work.

## Considerations
* When displaying the data should we consider daylight savings that might occur?
    * Probably not, because we don't know *when* the request is made and in *what* country
    * Might lead to more confusion than benefits.
    * Depending on the use context might want to bring this up.
* It's unclear what to do if the request came in with an empty list or is incomplete, so we fail gracefully.
 
## Improvement ideas
* Localisation should probably be considered. The app is more likely to be multilingual than not, hence we should consider locale when displaying day names and time format.
    * At least make this configurable in the API request. 

## Final thoughts

* The original format is human-friendly but quite unfriendly to work with.
* The format almost always requires some kind of normalisation before it can be worked with.
* A flatter structure would be a lot better.
* Overall the task was quite interesting and challenging, the assignment itself is written quite clear and understandable with clear goals and expectations - that's definitely a plus.
