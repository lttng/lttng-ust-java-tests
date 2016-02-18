LTTng-UST Java Agent Test Package
=================================

This git tree contains integration tests and benchmarks for the
[LTTng-UST](https://lttng.org/) Java Agent. It requires many additional
dependencies compared to the library itself, so it is shipped as a separate
package for now.


Prerequisites
-------------

* Java 1.8
* [Apache Maven](https://maven.apache.org/) 3.0+
* A recent version of [LTTng-Tools](https://lttng.org/download/)
* A recent version of [Babeltrace](http://www.efficios.com/babeltrace)
* A git branch or version of LTTng-UST you want to test

For example, on Ubuntu you can use the
[LTTng PPA](https://launchpad.net/~lttng/+archive/ubuntu/ppa), then Maven and
OpenJDK from the main repository:

    sudo apt-add-repository ppa:lttng/ppa
    sudo apt-get update
    sudo apt-get install lttng-tools babeltrace maven openjdk-8-jre

Also make sure `mvn -version` reports a `Java version: 1.8` or higher. If it
does not, you may need to set your `JAVA_HOME` accordingly.


Usage
-----

First you need to `make install` the LTTng-UST git branch you want to test.
For example:

    git clone git://git.lttng.org/lttng-ust.git
    cd lttng-ust/
    (do some modifications, checkout a different branch, etc.)
    ./bootstrap
    ./configure --enable-java-agent-all
    make
    sudo make install

Then, `cd` back to the directory where you cloned the present git tree, and
issue a

    mvn clean verify

This will run all the tests on the UST agent that was `make install`'ed. Tests
will be skipped if they cannot find their required classes or native libraries,
so make sure the output mentions succesful tests and not skipped ones.

Please make sure you have no `lttng` session active prior to or during the
tests, or it might interfere with the test runs!

Detailed JUnit test reports will be available under
`lttng-ust-java-tests-{jul|log4j}/target/failsafe-reports/`


Setting library paths
---------------------

By default, the tests will look for the Java and JNI libraries in the default
locations of `make install` (`/usr/local/lib`, `/usr/local/share/java`, etc.)

If for example, you installed into the `/usr` prefix instead of `/usr/local`,
you can use the following properties to specify different locations for the
lttng-ust-agent-java jars:

    mvn clean verify
    -Dcommon-jar-location=/usr/share/java/lttng-ust-agent-common.jar
    -Djul-jar-location=/usr/share/java/lttng-ust-agent-jul.jar
    -Dlog4j-jar-location=/usr/share/java/lttng-ust-agent-log4j.jar

To specify a different locations for the JNI .so libraries, you can set the
`-Djava.library.path` property on the JVM:

    mvn (...) -DargLine=-Djava.library.path=/usr/lib

Note: do not use `MAVEN_OPTS` to set the library path, since the `argLine`
property defined in the build will overwrite it.


Running the benchmarks
----------------------

By default only the unit/integration tests are run. To also run the benchmarks,
run Maven with the `benchmark` profile, as follows:

    mvn clean verify -Pbenchmark

The benchmark results should be part of the standard output.

