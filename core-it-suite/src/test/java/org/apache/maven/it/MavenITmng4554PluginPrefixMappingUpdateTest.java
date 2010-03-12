package org.apache.maven.it;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.FileUtils;
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-4554">MNG-4554</a>.
 * 
 * @author Benjamin Bentmann
 */
public class MavenITmng4554PluginPrefixMappingUpdateTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng4554PluginPrefixMappingUpdateTest()
    {
        super( "[2.0.3,3.0-alpha-1),[3.0-alpha-7,)" );
    }

    /**
     * Test that the metadata holding the plugin prefix mapping is cached and not redownloaded upon each
     * Maven invocation.
     */
    public void testitCached()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-4554" );

        String metadataUri = "/repo-1/org/apache/maven/its/mng4554/maven-metadata.xml";

        final List requestedUris = new ArrayList();

        AbstractHandler logHandler = new AbstractHandler()
        {
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                requestedUris.add( request.getRequestURI() );
            }
        };

        ResourceHandler repoHandler = new ResourceHandler();
        repoHandler.setResourceBase( testDir.getAbsolutePath() );

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler( logHandler );
        handlerList.addHandler( repoHandler );
        handlerList.addHandler( new DefaultHandler() );

        Server server = new Server( 0 );
        server.setHandler( handlerList );
        server.start();

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        try
        {
            verifier.setAutoclean( false );
            verifier.deleteDirectory( "target" );
            try
            {
                verifier.deleteArtifacts( "org.apache.maven.its.mng4554" );
            }
            catch ( IOException e )
            {
                // expected when running test on Windows using embedded Maven (JAR files locked by plugin class realm)
                assertFalse( new File( verifier.localRepo, "org/apache.maven.its.mng4554/maven-metadata-mng4554.xml" ).exists() );
            }
            Properties filterProps = verifier.newDefaultFilterProperties();
            filterProps.setProperty( "@port@", Integer.toString( server.getConnectors()[0].getLocalPort() ) );
            filterProps.setProperty( "@repo@", "repo-1" );
            verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8", filterProps );
            verifier.getCliOptions().add( "-s" );
            verifier.getCliOptions().add( "settings.xml" );

            verifier.setLogFileName( "log-cached-1.txt" );
            verifier.executeGoal( "a:touch" );
            verifier.verifyErrorFreeLog();

            verifier.assertFilePresent( "target/touch.txt" );
            assertTrue( requestedUris.toString(), requestedUris.contains( metadataUri ) );

            requestedUris.clear();

            verifier.setLogFileName( "log-cached-2.txt" );
            verifier.executeGoal( "a:touch" );
            verifier.verifyErrorFreeLog();

            assertFalse( requestedUris.toString(), requestedUris.contains( metadataUri ) );
        }
        finally
        {
            verifier.resetStreams();
            server.stop();
        }
    }

    /**
     * Test that the local metadata holding the plugin prefix mapping can be forcefully updated via the command
     * line flag -U.
     */
    public void testitForcedUpdate()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-4554" );

        String metadataUri = "/repo-1/org/apache/maven/its/mng4554/maven-metadata.xml";

        final List requestedUris = new ArrayList();

        AbstractHandler logHandler = new AbstractHandler()
        {
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                requestedUris.add( request.getRequestURI() );
            }
        };

        ResourceHandler repoHandler = new ResourceHandler();
        repoHandler.setResourceBase( testDir.getAbsolutePath() );

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler( logHandler );
        handlerList.addHandler( repoHandler );
        handlerList.addHandler( new DefaultHandler() );

        Server server = new Server( 0 );
        server.setHandler( handlerList );
        server.start();

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        try
        {
            verifier.setAutoclean( false );
            verifier.deleteDirectory( "target" );
            try
            {
                verifier.deleteArtifacts( "org.apache.maven.its.mng4554" );
            }
            catch ( IOException e )
            {
                // expected when running test on Windows using embedded Maven (JAR files locked by plugin class realm)
                assertFalse( new File( verifier.localRepo, "org/apache.maven.its.mng4554/maven-metadata-mng4554.xml" ).exists() );
            }
            Properties filterProps = verifier.newDefaultFilterProperties();
            filterProps.setProperty( "@port@", Integer.toString( server.getConnectors()[0].getLocalPort() ) );
            filterProps.setProperty( "@repo@", "repo-1" );
            verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8", filterProps );
            verifier.getCliOptions().add( "-U" );
            verifier.getCliOptions().add( "-s" );
            verifier.getCliOptions().add( "settings.xml" );

            verifier.setLogFileName( "log-forced-1.txt" );
            verifier.executeGoal( "a:touch" );
            verifier.verifyErrorFreeLog();

            verifier.assertFilePresent( "target/touch.txt" );
            assertTrue( requestedUris.toString(), requestedUris.contains( metadataUri ) );

            requestedUris.clear();

            verifier.setLogFileName( "log-forced-2.txt" );
            verifier.executeGoal( "a:touch" );
            verifier.verifyErrorFreeLog();

            assertTrue( requestedUris.toString(), requestedUris.contains( metadataUri ) );
        }
        finally
        {
            verifier.resetStreams();
            server.stop();
        }
    }

    /**
     * Test that the local metadata holding the plugin prefix mapping is automatically refetched from the remote
     * repositories if the local metadata fails to resolve a new/other plugin prefix.
     */
    public void testitRefetched()
        throws Exception
    {
        requiresMavenVersion( "[3.0-alpha-3,)" );

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-4554" );

        String metadataUri = "/repo-it/org/apache/maven/its/mng4554/maven-metadata.xml";

        final List requestedUris = new ArrayList();

        AbstractHandler logHandler = new AbstractHandler()
        {
            public void handle( String target, HttpServletRequest request, HttpServletResponse response, int dispatch )
                throws IOException, ServletException
            {
                requestedUris.add( request.getRequestURI() );
            }
        };

        ResourceHandler repoHandler = new ResourceHandler();
        repoHandler.setResourceBase( testDir.getAbsolutePath() );

        HandlerList handlerList = new HandlerList();
        handlerList.addHandler( logHandler );
        handlerList.addHandler( repoHandler );
        handlerList.addHandler( new DefaultHandler() );

        Server server = new Server( 0 );
        server.setHandler( handlerList );
        server.start();

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        try
        {
            verifier.setAutoclean( false );
            verifier.deleteDirectory( "target" );
            try
            {
                verifier.deleteArtifacts( "org.apache.maven.its.mng4554" );
            }
            catch ( IOException e )
            {
                // expected when running test on Windows using embedded Maven (JAR files locked by plugin class realm)
                assertFalse( new File( verifier.localRepo, "org/apache.maven.its.mng4554/maven-metadata-mng4554.xml" ).exists() );
            }
            Properties filterProps = verifier.newDefaultFilterProperties();
            filterProps.setProperty( "@port@", Integer.toString( server.getConnectors()[0].getLocalPort() ) );
            filterProps.setProperty( "@repo@", "repo-it" );
            verifier.filterFile( "settings-template.xml", "settings.xml", "UTF-8", filterProps );
            verifier.getCliOptions().add( "-s" );
            verifier.getCliOptions().add( "settings.xml" );

            FileUtils.copyDirectoryStructure( new File( testDir, "repo-1" ), new File( testDir, "repo-it" ) );

            verifier.setLogFileName( "log-refetched-1.txt" );
            verifier.executeGoal( "a:touch" );
            verifier.verifyErrorFreeLog();

            verifier.assertFilePresent( "target/touch.txt" );
            assertTrue( requestedUris.toString(), requestedUris.contains( metadataUri ) );

            requestedUris.clear();

            // simulate deployment of new plugin which updates the prefix mapping in the remote repo
            FileUtils.copyDirectoryStructure( new File( testDir, "repo-2" ), new File( testDir, "repo-it" ) );

            verifier.setLogFileName( "log-refetched-2.txt" );
            verifier.executeGoal( "b:touch" );
            verifier.verifyErrorFreeLog();

            assertTrue( requestedUris.toString(), requestedUris.contains( metadataUri ) );
        }
        finally
        {
            verifier.resetStreams();
            server.stop();
        }
    }

}