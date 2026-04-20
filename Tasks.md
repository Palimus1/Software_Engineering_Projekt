

##### Test befehle
>sbt clean coverage test
> 
>sbt coverageReport

Dann in 
> projekt\target\scala-3.8.2\scoverage-report/index.html

öffnen für code coverage graph

https://docs.google.com/spreadsheets/d/1mnmKRb4mlEO6AwB1iM8ShA3tPNEqEnFToQXzvBjPGvQ/edit?gid=0#gid=0

## **Task 3: Write Tests 17.4.**

- Take the Classes from your domain model
- Start developing them in Scala
- Use Behaviour Driven Development
    - Use ScalaTest
    - Write a Spec using WordSpec
    - Use the sbt test Test Runner
- Achieve 100% code coverage. And maintain it!

## **Task 4: Build a Text UI 24.4.**

Start building the core data structures of your game.

Build a simple UI using text input and output.

    S: Design String Darstellung Home/Middle
    S: Farben für String Ausgabe (4 Farben)
    S: scalable für 1-4 players
    
    P: Middle (vier letzte Stufen/Positionen) aktuell immer [41 - 44]
        -evtl Scalable? [max-3 bis max]
    P: evtl je nach #Payers so viele Home/Häuschen [0]

## **Task 5: Build a MVC Architecture 8.5. (statt 1.5.)**

- Make sure that your game has a clean MVC architecture
- No import of classes from higher layers
- Use reorganization and observer pattern to resolve
    - Cycles
    - Layer conflicts

## **Task 6: Set up a Continuous Development Process 22.5. (statt 15.5)**

- Make your development independant of the local platform using a build mechanism like sbt
- Initiate a Continuous Integration Server on github
- Set up tests and code coverage using Coveralls
- Integrate Badges into your git repository

## **Task 7: Integrate Patterns into your code 5.6.(statt 29.5.)**

Increase the quality of your code by making it more extendable using patterns.

Try to integrate a pattern like Strategy, Factory, State, Template or others.

Try to implement 3 or more patterns.

## **Task 8: Integrate Undo, Make use of Try and Option 12.6.**

Implement an Undo mechanism using the Command Pattern.

Avoid the use of null and Null. Use Option instead. Also avoid using try-catch, use the Try-Monad instead.

## **Task 9: Build a Graphical UI 19.6.**

Build a GUI for your application.

You can use either ScalaSwing or ScalaFX.

GUI and TUI must operate together. For this to work, the TUI has to be Event-based. Both TUI and GUI need to be Observers of the controller.

## **Task 10: Introduce Interfaces and Components 26.6.**

Cut your application into Components

Encapsulate every Component with Interfaces

Make sure only the Interfaces are accessed

Cut all access to the inner workings of the Component

## **Task 11: Use Dependency Injection**

Define a Module

Bind your Interfaces of your Components to Instances here

Create an Injector in your main

Create Instances of your Components

Inject your Component Instances into the other Components

## **Task 12: Implement FileIO**

Implement FileIO using XML

Implement FileIO using Json

Hide both implementations behind the same Interface

Make use of Dependency Injection to switch between these two implementations

## **Task 13: Deploy on Docker**

Select a Docker Container template that supports your software well.

Write a Docker script to adjust it to your needs and

deploy your game on a Docker Container.

## **(Task 14:) Write Documentation**

- Write JavaDoc documentation for all Interfaces
- Write Mark Down documentation for the overall application and architecture in md and put it on github
- Prepare Slides for your presentation
- Hand in your github repository in moodle
- Hand in your Slides and link to them in github from md