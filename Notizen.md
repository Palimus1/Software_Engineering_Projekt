

##### Test befehle

>sbt clean coverage test coverageReport

Dann in 
> projekt\target\scala-3.8.2\scoverage-report/index.html

öffnen für code coverage graph


### Sonar Qube

Erst programm im terminal starten:
```shell
PS C:\Users\stell\Documents\GitHub\Software_Engineering_Projekt> cd C:\CodeTools\sonarqube-26.4.0.121862\sonarqube-26.4.0.121862\bin\windows-x86-64
>> 
PS C:\CodeTools\sonarqube-26.4.0.121862\sonarqube-26.4.0.121862\bin\windows-x86-64> .\StartSonar.bat
>> 
```
dann localhost öffnen:
http://localhost:9000

tests durchführen:
`sbt clean coverage test coverageReport`

bei success dann: `sbt sonarScan`

und localhost anschauen