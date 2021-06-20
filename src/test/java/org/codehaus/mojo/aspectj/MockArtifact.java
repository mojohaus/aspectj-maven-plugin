package org.codehaus.mojo.aspectj;

/**
 * The MIT License
 *
 * Copyright 2005-2006 The Codehaus.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.OverConstrainedVersionException;
import org.apache.maven.artifact.versioning.VersionRange;

/**
 * Fake implementation of artifict to test with
 * 
 * @author <a href="mailto:tel@objectnet.no">Thor Age Eldby</a>
 */
public class MockArtifact
    implements Artifact
{

    private String groupId;

    private String artifactId;

    private String type = "jar";

    private String classifier;

    private ArtifactHandler artifactHandler;

    /**
     * Constructor
     * 
     * @param groupId group ID
     * @param artifactId artifact ID
     */
    public MockArtifact( String groupId, String artifactId )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    /**
     * Constructor with type and classifier
     *
     * @param groupId group ID
     * @param artifactId artifact ID
     * @param classifier artifact classifier
     * @param type artifact type
     */
    public MockArtifact( String groupId, String artifactId, String classifier, String type )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.classifier = classifier;
        this.type = type;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public ArtifactHandler getArtifactHandler()
    {
        return artifactHandler;
    }

    public void setArtifactHandler( ArtifactHandler artifactHandler )
    {
        this.artifactHandler = artifactHandler;
    }

    public String getVersion()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setVersion( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getScope()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getType()
    {
        return this.type;
    }

    public String getClassifier()
    {
        return this.classifier;
    }

    public boolean hasClassifier()
    {
        return this.classifier != null;
    }

    /**
     * @return very stupid file name for artifact
     */
    public File getFile()
    {
        String path = getGroupId() + '/' + getArtifactId();
        if ( getClassifier() != null )
            path += "-" + getClassifier();
        path += "." + getType();
        return new File( path );
    }

    public void setFile( File arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getBaseVersion()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setBaseVersion( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getId()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getDependencyConflictId()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void addMetadata( ArtifactMetadata arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public ArtifactMetadata getMetadata( Class aClass )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public Collection getMetadataList()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setRepository( ArtifactRepository arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public ArtifactRepository getRepository()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void updateVersion( String arg0, ArtifactRepository arg1 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public String getDownloadUrl()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setDownloadUrl( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public ArtifactFilter getDependencyFilter()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setDependencyFilter( ArtifactFilter arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public List getDependencyTrail()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setDependencyTrail( List arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setScope( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public VersionRange getVersionRange()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setVersionRange( VersionRange arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void selectVersion( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setGroupId( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setArtifactId( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public boolean isSnapshot()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setResolved( boolean arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public boolean isResolved()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setResolvedVersion( String arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public boolean isRelease()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setRelease( boolean arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public List getAvailableVersions()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setAvailableVersions( List arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public boolean isOptional()
    {
        throw new RuntimeException( "Not implemented" );
    }

    public void setOptional( boolean arg0 )
    {
        throw new RuntimeException( "Not implemented" );
    }

    public ArtifactVersion getSelectedVersion()
        throws OverConstrainedVersionException
    {
        throw new RuntimeException( "Not implemented" );
    }

    public boolean isSelectedVersionKnown()
        throws OverConstrainedVersionException
    {
        throw new RuntimeException( "Not implemented" );
    }

    /**
     * Very simple compareTo implementation
     *
     * @param a other artifact to compare to
     * @return compare value
     */
    public int compareTo( Artifact a )
    {
        int val = getGroupId().compareTo( a.getGroupId() );
        if ( val != 0 )
            return val;
        val = getArtifactId().compareTo( a.getArtifactId() );
        return val;
    }
}
