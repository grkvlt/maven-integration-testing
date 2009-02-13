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
import org.apache.maven.it.util.ResourceExtractor;

import java.io.File;

/**
 * This is a test set for <a href="http://jira.codehaus.org/browse/MNG-3217">MNG-3217</a>.
 * 
 * @author Benjamin Bentmann
 * @version $Id$
 */
public class MavenITmng3217InterPluginDependencyTest
    extends AbstractMavenIntegrationTestCase
{

    public MavenITmng3217InterPluginDependencyTest()
    {
        super( "[3.0-alpha-1,)" );
    }

    /**
     * Verify that the dependency of plugin A on some plugin B does not influence the build of another module in the
     * reactor that uses a different version of plugin B for normal build tasks.
     */
    public void testitMNG3217()
        throws Exception
    {
        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/mng-3217" );

        Verifier verifier = new Verifier( testDir.getAbsolutePath() );
        verifier.setAutoclean( false );
        verifier.deleteDirectory( "sub-1/target" );
        verifier.deleteDirectory( "sub-2/target" );
        verifier.deleteArtifacts( "org.apache.maven.its.mng3217" );
        verifier.filterFile( "pom.xml", "pom.xml", "UTF-8", verifier.newDefaultFilterProperties() );
        verifier.executeGoal( "validate" );
        verifier.verifyErrorFreeLog();
        verifier.resetStreams();

        verifier.assertFilePresent( "sub-1/target/touch-1.txt" );
        verifier.assertFilePresent( "sub-2/target/touch-2.txt" );
    }

}