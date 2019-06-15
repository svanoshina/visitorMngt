# visitor_mngt
Imagine you are developing visitor management system for public library

When people arrive to the library they need to check in on the reception. When they leave the library they need to check out. Every visitor receives permanent id so when he/she visits library again he/she retrieves the same ID as before.

 

Write down an application that implements three methods:

1. Visitor comes to the library (with some id that could be already registered in system or not)

`fun enter(visitorId: Long, arrivedAt: Date)`

2. Visitor leaves library

`fun leave(visitorId: Long, leftAt: Date)`

3. Count how many unique visitors were in the library between two dates

`fun calculateAttendance(from: Date, to: Date): Long`

 

Requirements:

1. Kotlin APP

2. No external storages (all in memory would be enough)

3. Pay attention on handling the cases when visitors throughput rises
