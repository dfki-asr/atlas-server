Setting up the container
===================

Currently, we support [WildFly](http://www.wildfly.org) 8.2.0 Final as container only.
JavaEE portability is a difficult terrain, and we do not have the resources to manoeuvre around this.
We're sorry for the inconvenience.

Quick start
-----------

Not all of the information in this file is necessary to get started with ATLAS.
At the bare minimum, you need to take the following steps:

* Download WildFly
* [Download and integrate ModeShape](#download-and-integrate-modeshape)
* Use the supplied [example WildFly configuration](#using-the-example-wildfly-configuration)
* Add the [messaging user](#set-up-a-user-for-messaging)
* [Deploy ATLAS](#deploying-the-application)


Download and extract WildFly
----------------------------

Download [WildFly 8.2.0.Final](http://www.wildfly.org/downloads/) and extract the archive to a folder on your disk.

Using the example WildFly configuration
---------------------------------------

To shorten startup times we've included a set of configuration files in this repository.
It includes the data storage and messaging configuration detailed in the rest of this document.

Copy the contents of the `example-configuration` directory to your WildFly `standalone/configuration/` directory.
You may need to overwrite the existing file there.

If you need to run something else apart from ATLAS on your WildFly, you probably need to configure some other things.
The detailed changes necessary can be found in the rest of this document.

Adding users to WildFly
-----------------------

### Set up a management user

In order to manage the server via the web interface, add a new user via the wildfly CLI, as follows.
Execute the `add-user` script from the `bin` folder, and feed it the following answers:

* __Type:__ Management user
* __Name:__ (the username you want to use for the web admin panel)
* __Password:__ (some safe password)
* __Groups:__ (leave empty)
* __Used to connect to another process:__ no

### Set up a user for Messaging

ATLAS' import worker connects to the container through HornetQ's STOMP interface.
Since this requires authentication, you need to add another user using the `add-user` script:

* __Type:__ Application user
* __Name:__ (the username you want to use for STOMP)
* __Password:__ (some safe password)
* __Groups:__ guest

Note: if you are going to use COMPASS along with ATLAS, you might want to use the stomp user created for COMPASS for ATLAS as well.

Install modeshape
-------------------

To store asset data, ATLAS uses the JCR (Java Content Repository) interface.
The JCR implementation for WildFly is called ModeShape, which is what we are going to install and configure here.

Detailed instructions for [installation](https://docs.jboss.org/author/display/MODE40/Installing+ModeShape+into+Wildfly) and [configuration](https://docs.jboss.org/author/display/MODE40/Configuring+ModeShape+in+Wildfly) can be found on the ModeShape site.
However, we will summarize the essentials here.

### Download and integrate ModeShape

Download the [ModeShape subsystem for Wildfly 8.0 - Version 4.1.0.Final](http://modeshape.jboss.org/downloads/downloads4-1-0-final.html).
Unzip the archive, it should contain `docs`, `domain`, `modules` and `standalone` directories.
Copy these directories into the root of your Wildfly installation.
This should add the files of ModeShape into the already existing directories of WildFly.
No overwriting of files should be necessary.

### Add modeshape user configuration

In the `standalone/configuration` folder, add a file `modeshape-roles.properties`, containing the following user role properties:

	#<userName>=[readonly[.<workspaceName>] | readwrite[.<workspaceName>]][, [readonly[.<workspaceName>] | readwrite[.<workspaceName>]]]*
	admin=admin,connect,readonly,readwrite
	guest=connect,readonly


Analogously, create `modeshape-users.properties`, containing

	# A users.properties file for use with the UsersRolesLoginModule
	# username=password
	admin=admin
	guest=guest

### Configure cache containers

Add the necessary *cache containers* for atlas in the infinispan subsystem defined in the `standalone.xml`:

	<cache-container name="modeshape" default-cache="data" module="org.modeshape">
		<local-cache name="atlas">
			<locking isolation="READ_COMMITTED"/>
			<transaction mode="NON_XA"/>
			<file-store passivation="false" purge="false" path="modeshape/store/atlas"/>
		</local-cache>
		<local-cache name="artifacts">
			<locking isolation="READ_COMMITTED"/>
			<transaction mode="NON_XA"/>
			<file-store passivation="false" purge="false" path="modeshape/store/artifacts"/>
		</local-cache>
	</cache-container>


### Setting up the security domain

Make sure the *security domain* you added while installing modeshape contains all necessary entries:

	<security-domain name="modeshape-security" cache-type="default">
		<authentication>
			<login-module code="UsersRoles" flag="required">
				<module-option name="usersProperties" value="${jboss.server.config.dir}/modeshape-users.properties"/>
				<module-option name="rolesProperties" value="${jboss.server.config.dir}/modeshape-roles.properties"/>
			</login-module>
		</authentication>
	</security-domain>

### Configure subsystem

Finally, the ModeShape subsystem needs to have (at least) these things configured:

* an `atlas` workspace
* access for the `admin` role to said workspace
* the bundled REST application needs to be enabled for the worker to function

This can be achieved using the following snippet:

    <subsystem xmlns="urn:jboss:domain:modeshape:2.0">
        <repository name="atlas" anonymous-roles="admin">
            <workspaces allow-workspace-creation="false">
                <workspace name="default"/>
            </workspaces>
        </repository>
        <webapp name="modeshape-rest.war"/>
    </subsystem>

### Setting the maximum upload size

Since assets to be imported can be relatively large, it may be necessary to increase the maximum POST request size.
This can be achieved by adding a `max-post-size` attribute to WildFly's http listener configuration tag, which is in the `urn:jboss:domain:undertow:1.2` subsystem.
For example, a maximum upload size of around 4.2 GB might look like this:

    <http-listener name="default" socket-binding="http" max-post-size="4515430400"/>


Messaging configuration
------------------------------

The messaging service makes use of the jboss messaging submodule. If you haven't configured this module yet, e.g. when setting up another project, do this as a first step before adding the ATLAS jms destinations. 

### Setting up the messaging submodule

Most of the configuration can be found in WildFly's `standalone-full.xml` example file, here we'll highlight the core elements necessary to allow ATLAS to communicate with the worker.

Add the module for the messaging service to the list of extension modules:

	<extension module="org.jboss.as.messaging"/>


Add the Message-Driven Bean (MDB) resource adapter to the ejb subsystem:

	<mdb>
		<resource-adapter-ref resource-adapter-name="${ejb.resource-adapter-name:hornetq-ra.rar}"/>
		<bean-instance-pool-ref pool-name="mdb-strict-max-pool"/>
	</mdb>


Finally, add a new subsystem for HornetQ using the following settings (note the `<connector>` and `<acceptor>` tags for STOMP, the rest is pretty much copied and pasted from `standalone-full.xml`):

	<subsystem xmlns="urn:jboss:domain:messaging:2.0">
		<hornetq-server>
			<journal-file-size>102400</journal-file-size>
			<connectors>
				<http-connector name="http-connector" socket-binding="http">
					<param key="http-upgrade-endpoint" value="http-acceptor"/>
				</http-connector>
				<http-connector name="http-connector-throughput" socket-binding="http">
					<param key="http-upgrade-endpoint" value="http-acceptor-throughput"/>
					<param key="batch-delay" value="50"/>
				</http-connector>
				<in-vm-connector name="in-vm" server-id="0"/>
				<connector name="netty-connector">
					<factory-class>org.hornetq.core.remoting.impl.netty.NettyConnectorFactory</factory-class>
				</connector>
			</connectors>
			<acceptors>
				<http-acceptor http-listener="default" name="http-acceptor"/>
				<http-acceptor http-listener="default" name="http-acceptor-throughput">
					<param key="batch-delay" value="50"/>
					<param key="direct-deliver" value="false"/>
				</http-acceptor>
				<in-vm-acceptor name="in-vm" server-id="0"/>
				<acceptor name="stomp-websocket">
					<factory-class>org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory</factory-class>
					<param key="host" value="0.0.0.0"/>
					<param key="port" value="61614"/>
				</acceptor>
				<acceptor name="stomp">
					<factory-class>org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory</factory-class>
					<param key="host" value="0.0.0.0"/>
					<param key="port" value="61613"/>
				</acceptor>
			</acceptors>
			<security-settings>
				<security-setting match="#">
					<permission type="send" roles="guest"/>
					<permission type="consume" roles="guest"/>
					<permission type="createNonDurableQueue" roles="guest"/>
					<permission type="deleteNonDurableQueue" roles="guest"/>
				</security-setting>
			</security-settings>
			<address-settings>
				<address-setting match="#">
					<dead-letter-address>jms.queue.DLQ</dead-letter-address>
					<expiry-address>jms.queue.ExpiryQueue</expiry-address>
					<max-size-bytes>10485760</max-size-bytes>
					<page-size-bytes>2097152</page-size-bytes>
					<message-counter-history-day-limit>10</message-counter-history-day-limit>
				</address-setting>
			</address-settings>
			<jms-connection-factories>
				<connection-factory name="InVmConnectionFactory">
					<connectors>
						<connector-ref connector-name="in-vm"/>
					</connectors>
					<entries>
						<entry name="java:/ConnectionFactory"/>
					</entries>
				</connection-factory>
				<connection-factory name="RemoteConnectionFactory">
					<connectors>
						<connector-ref connector-name="http-connector"/>
					</connectors>
					<entries>
						<entry name="java:jboss/exported/jms/RemoteConnectionFactory"/>
					</entries>
				</connection-factory>
				<pooled-connection-factory name="hornetq-ra">
					<transaction mode="xa"/>
					<connectors>
						<connector-ref connector-name="in-vm"/>
					</connectors>
					<entries>
						<entry name="java:/JmsXA"/>
						<entry name="java:jboss/DefaultJMSConnectionFactory"/>
					</entries>
				</pooled-connection-factory>
			</jms-connection-factories>
			<jms-destinations>
				<jms-queue name="ExpiryQueue">
					<entry name="java:/jms/queue/ExpiryQueue"/>
				</jms-queue>
				<jms-queue name="DLQ">
					<entry name="java:/jms/queue/DLQ"/>
				</jms-queue>
			</jms-destinations>
		</hornetq-server>
	</subsystem>


### Add ATLAS jms queues

Add the ATLAS jms destinations to the JMS destinations of the HornetQ messaging subsystems `<jms-destinations>` tag defined (as per the example above) in `standalone.xml`:

	<jms-queue name="atlas.work.import">
		<entry name="queue/atlas/work/import"/>
		<entry name="/queue/atlas.work.import"/>
		<entry name="java:/jms/queue/atlas/work/import"/>
	</jms-queue>
	<jms-queue name="atlas.work.feedback">
		<entry name="queue/atlas/work/feedback"/>
		<entry name="/queue/atlas.work.feedback"/>
		<entry name="java:/jms/queue/atlas/work/feedback"/>
	</jms-queue>


Increasing the memory limit
-----------------------------

To increase the amount of memory available for ATLAS, open the configuration file `bin/standalone.conf` (or `bin/standalone.conf.bat` on Windows systems) located in you WildFly installation's. Go to the the options to be passed to the Java VM.
The memory maximum can be adjusted in the line

	JAVA_OPTS="-Xms64m -Xmx512m -XX:MaxPermSize=256m -Djava.net.preferIPv4Stack=true"

by increasing the `-Xmx`, `-Xms` and `-XX:MaxPermSize` value.

Since ATLAS probably needs to serialise large amounts of binary data to text, which requires a decent amount of memory, you are encouraged to adjust the default values. At any rate, the container log will tell you when you run out of memory.

Deploying the application
-------------------------

Once the container is configured, you can deploy the ATLAS WAR.
After compilation (see the [README file](README.md)), you can find this file in the `target/` directory in your source folder.

To deploy the application, WildFly needs to be running.
Unless it already is, start the `standalone` script in WildFly's `bin` folder.
Once the container is running, it should suffice to copy the EAR archive to the `standalone/deployments/` folder of WildFly.

After allowing the container some time to bring up all the classes and services needed for ATLAS,
it should be available from your container's `atlas/` context.
With the configuration supplied, you can reach this via `http://localhost:8080/atlas/`.