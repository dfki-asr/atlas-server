ATLAS (webapp)
==============

ATLAS (which stands for "Advanced Three-dimensional Large-scale Asset Server") is a system for
storage and conversion of polygonal 3D models. It offers a REST interface to add and retrieve
models, where HTTP content negotiation is used to determine the input and output format(s).

Its current primary use is to import 3D models into its own storage format, and export them
to something usable on the web.


Architecture
------------

Here is a brief overview diagram of ATLAS' architecture: 

          +--------+    +-------------+     +---------+
          |        |    |             |     |         |
    ------> Upload +---->             +----->  Export +----->
          |        |    |             |     |         |
          +--------+    |             |     +----^----+
                        |   Storage   |          |
          +--------+    |             |     +- - + - -+
          |        |    |             |     |         |      
          | Import <---->             +-----> Filter
          |        |    |             |     |         |
          +--------+    +-------------+     +- - - - -+


The Upload and Export steps are handled by a Java Enterprise web application. The rationale behind
this is that web services and serialization are very easy to handle in Java through well-standardized
interfaces (JAX-RS, JAXB, etc.), and that it should be easy to add export processing. For the latter
aspect Java fit well, because it is a well-known language with a large ecosystem to draw from.

The web application communicates (via JMS/STOMP) with an import worker application, written in C++.
The worker places 3D models in ATLAS' internal data format into the storage via another REST interface. 
The import part lives separated from the web application since many (especially proprietary) 3D data
formats have SDKs or libraries available with a C/C++ interface. Thus, integration is much easier into
the worker application which is not constrained (with respect to temporary files, for example) 
by running inside a JavaEE container.


Features
--------

Import formats:

* COLLADA
* AutomationML (incomplete)

Export formats:

* COLLADA
* XML3D 4.2 Asset format


Repository structure
--------------------

This repository houses the Java Enterprise web application part of ATLAS.
It follows the usual structure of a maven-built Java project.

    src/main/java   - Java Code (Exporters, Upload, and JAX-RS classes)
    src/main/webapp - Website frontend for the REST interface


Building
--------

You sould just be able to say

    mvn package

from this directory (with `README` and `pom.xml`), and an atlas-[version].war should be built.


Running
-------

Currently, ATLAS' web app is supported on the WildFly 8 JavaEE container. You will need to configure WildFly appropriately to run the web app, however. Details can be found in the CONTAINER.md document next to this README.md.


Contributing
------------

Contributions are very welcome. We're using the well-known [git branching model](http://nvie.com/posts/a-successful-git-branching-model/), supported by [git flow](https://github.com/nvie/gitflow). We recommend reading the [git flow cheatsheet](http://danielkummer.github.io/git-flow-cheatsheet/).
Please use the github workflow (i.e., pull requests) to get your features and/or fixes to our attention.


License
-------

Our code (and documentation) is licensed under the Apache License version 2.0. You should have received an approriate LICENSE.txt file with this distribution. At any rate, you can get the official license text from http://www.apache.org/licenses/LICENSE-2.0.txt

### Third party contents ###

Our source distribution includes the following third party items with respective licenses:

    jquery-2.1.1     - MIT License
    bootstrap-3.2.0  - MIT License
    handlebars-1.0.0 - MIT License
    spin.js          - MIT License
    xml3d.js         - MIT License
    xml3d.tools.js   - MIT License
