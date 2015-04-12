Eclipse Monto
=============

This project contains the eclipse plugin for
[monto](https://bitbucket.org/inkytonik/monto) and a few exemplary servers for
Java 8. This project is more a proof-of-concept and is **not** ready for use.

Development Instructions
------------------------

The eclipse plugin currently works only with my own broker and not with the
[official broker](https://bitbucket.org/inkytonik/monto). The following steps
explain how to get the development environment for the Eclipse-Monto plugin up
an running.

1. Get the [Monto broker](https://github.com/svenkeidel/monto-broker)
   and follow the installation instructions
2. In Eclipse, install `Eclipse plug-in development environment` from Eclipse's built-in update site.
3. Add the update site `http://update.rascal-mpl.org/unstable` and install `IMP runtime`
4. Import this repository as an eclipse project
5. Start the broker with `./start.sh`
6. Run the project as an `Eclipse Application`
7. The only supported language at the moment is Java, so create a new Java
   Project in the new eclipse instance, create a new Java class and play around.


Code Compass
------------

This section points to some interesting classes and briefly describes their role
in the project.

 * `de.tudarmstadt.stg.monto.MontoParseController`: Implements the
   `ParseController` of the IMP project. Is a Monto-Source and -Sink at the same
   time. This is a good starting point to understand the plugin.

 * `de.tudarmstadt.stg.monto.java8.JavaTokenizer`: Very simple Monto-Server.
   Tokenizes Java 8 Code with ANTLR.

 * `de.tudarmstadt.stg.monto.java8.JavaOutliner`: More complex Monto-server. Has
   a dependency on the AST product produced from
   `de.tudarmstadt.stg.monto.java8.JavaParser`.

 * `de.tudarmstadt.stg.monto.connection.AbstractServer`: Superclass of the both
   classes above. Implements connection handling and JSON parsing and encoding.

 * `de.tudarmstadt.stg.monto.connection.Activator`: Initializes and shuts down
   the plugin.  Contains a list of servers and the setup of the broker
   connection.

License
-------

* All code under the directory `src/de/tudarmstadt/stg/monto/` except
  `src/de/tudarmstadt/stg/monto/java8/Java8.g4` is licensed under the BSD3
  license (`LICENSE`).

* The file `src/de/tudarmstadt/stg/monto/java8/Java8.g4` is licensed under the
  BSD3 license (`LICENSE.java8grammar`).

* The file `src/com/tonian/director/dm/json/JSONWriter.java` is licensed under
  the Apache Version 2.0 License (`LICENSE.jsonwriter`).
  The file originates from the [courier project](https://github.com/JAIDE/courier).
