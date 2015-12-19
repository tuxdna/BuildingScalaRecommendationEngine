# Building Recommendation Engine with Scala


## Description

This source-code contains all files for the book Building Recommendation Engine with Scala. The root folder of the codebase contains code examples as described below.


    README.txt -- this file
    build.sbt  -- SBT configuration file for this project
    datasets   -- a subset of dataset used in the book
    images     -- plots generated for the book
    output     -- output files generated for the book
    scripts    -- data manipulation scripts
    src        -- code examples for the book
    unified-recommender  -- Unfied Recommender example for PredictionIO
    webapp-recommender   -- Web-app using Amazon dataset 



## Requirements?

 * JDK 7 or later
 * Scala 2.10.4 or later
 * SBT 0.13 or later
 * Python 2.6 / 2.7 with Pandas and Matplotlib
 
## How to build?


Usually you only need to have above dependencies installed. Other dependencies are mentioned in the book as required.

For the most part you only need to invoke following command:

    sbt 'run-main <FULLY-QUALIFIED-CLASS-NAME> <ARGUMENTS>'


Where `<FULLY-QUALIFIED-CLASS-NAME>` should be replaced with complete class name and
`<ARGUMENTS>` should be replaced with the Command Line arguments.


