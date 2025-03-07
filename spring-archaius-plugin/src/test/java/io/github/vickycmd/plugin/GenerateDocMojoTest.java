package io.github.vickycmd.plugin;


import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.WithoutMojo;

import org.junit.Rule;
import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

public class GenerateDocMojoTest
{
    @Rule
    public MojoRule rule = new MojoRule()
    {
        @Override
        protected void before() throws Throwable
        {
        }

        @Override
        protected void after()
        {
        }
    };

    /**
     * @throws Exception if any
     */
    @Test
    public void testSomething()
            throws Exception
    {
        File pom = new File( "target/test-classes/project-to-test/" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        GenerateDocMojo generateDocMojo = (GenerateDocMojo) rule.lookupConfiguredMojo( pom, "generate-doc" );
        assertNotNull(generateDocMojo);
        generateDocMojo.execute();

        String outputDirectory = ( String ) rule.getVariableValueFromObject(generateDocMojo, "outputDirectory" );
        assertNotNull( outputDirectory );
        assertTrue( (new File(outputDirectory)).exists() );

        File outputFile = new File( outputDirectory );
        assertNotNull(outputFile);
        assertTrue( outputFile.exists() );

    }

    /** Do not need the MojoRule. */
    @WithoutMojo
    @Test
    public void testSomethingWhichDoesNotNeedTheMojoAndProbablyShouldBeExtractedIntoANewClassOfItsOwn()
    {
        assertTrue( true );
    }

}

