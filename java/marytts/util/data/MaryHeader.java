/**
 * Portions Copyright 2006 DFKI GmbH.
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute, 
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * 
 * Permission is hereby granted, free of charge, to use and distribute
 * this software and its documentation without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of this work, and to
 * permit persons to whom this work is furnished to do so, subject to
 * the following conditions:
 * 
 * 1. The code must retain the above copyright notice, this list of
 *    conditions and the following disclaimer.
 * 2. Any modifications must be clearly marked as such.
 * 3. Original authors' names are not deleted.
 * 4. The authors' names are not used to endorse or promote products
 *    derived from this software without specific prior written
 *    permission.
 *
 * DFKI GMBH AND THE CONTRIBUTORS TO THIS WORK DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL DFKI GMBH NOR THE
 * CONTRIBUTORS BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package marytts.util.data;

import java.io.BufferedInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Common helper class to read/write a standard Mary header to/from the various
 * Mary data files.
 * 
 * @author sacha
 *
 */
public class MaryHeader 
{ 
    /* Global constants */
    private final static int MAGIC = 0x4d415259; // "MARY"
    private final static int VERSION = 40; // 4.0
    
    /* List of authorized file type identifier constants */
    public final static int UNKNOWN = 0;
    public final static int CARTS = 100;
    public final static int DIRECTED_GRAPH = 110;
    public final static int UNITS = 200;
    public final static int LISTENERUNITS = 225;
    public final static int UNITFEATS = 300;
    public final static int LISTENERFEATS = 325;
    public final static int HALFPHONE_UNITFEATS = 301; 
    public final static int JOINFEATS = 400;
    public final static int SCOST = 445;
    public final static int PRECOMPUTED_JOINCOSTS = 450;
    public final static int TIMELINE = 500;
    
    
    /* Private fields */
    private int magic = MAGIC;
    private int version = VERSION;
    private int type = UNKNOWN;

    
    // STATIC CODE

    /**
     * For the given file, look inside and determine the file type.
     * @param fileName
     * @throws IOException if the file is not a MARY file.
     */
    public static int peekFileType(String fileName) throws IOException
    {
        DataInputStream dis = null;
        dis = new DataInputStream( new BufferedInputStream( new FileInputStream( fileName ) ) );
        /* Load the Mary header */
        MaryHeader hdr = new MaryHeader( dis );
        if ( !hdr.isMaryHeader() ) {
            throw new IOException( "File [" + fileName + "] is not a valid Mary format file." );
        }
        int type = hdr.getType();
        dis.close();
        return type;

    }
    
    
    /****************/
    /* CONSTRUCTORS */
    /****************/
    
    /**
     * Plain constructor
     * 
     * @param newType The standard type of the Mary file, to be chosen among:
     * MaryHeader.CARTS, MaryHeader.UNITS, MaryHeader.UNITFEATS, MaryHeader.JOINFEATS, MaryHeader.TIMELINE.
     * 
     * @throws RuntimeException if the input type is unknown.
     */
    public MaryHeader( int newType ) {
        if ( (newType > TIMELINE) || (newType < UNKNOWN) ) {
            throw new RuntimeException( "Unauthorized Mary file type [" + type + "]." );
        }
        type = newType;    
    }
    
    /**
     * File constructor
     * 
     * @param input a DataInputStream or RandomAccessFile to read the header from.
     * 
     * @throws IOException if the input type is unknown.
     */
    public MaryHeader( DataInput input ) throws IOException {
        this.load( input );
        if ( !isMaryHeader() ) { throw new RuntimeException( "Ill-formed Mary header!" ); }
    }

    /**
     * File constructor
     * 
     * @param input a byte buffer to read the header from.
     * 
     * @throws RuntimeException if the data read does not correspond to a mary header.
     */
    public MaryHeader( ByteBuffer input ) {
        this.load( input );
        if ( !isMaryHeader() ) { throw new RuntimeException( "Ill-formed Mary header!" ); }
    }

    /*****************/
    /* OTHER METHODS */
    /*****************/
    
    /** Static Mary header writer
     * 
     * @param output The DataOutputStream or RandomAccessFile to write to
     * 
     * @return the number of written bytes.
     * 
     * @throws IOException if the file type is unknown.
     * 
     * @author sacha
     */
    public long writeTo( DataOutput output ) throws IOException {
        
        long nBytes = 0;
        
        if ( !this.hasLegalType() ) {
            throw new RuntimeException( "Unknown Mary file type [" + type + "]." );
        }
        
        output.writeInt( magic );   nBytes += 4;
        output.writeInt( version ); nBytes += 4;
        output.writeInt( type );    nBytes += 4;
        
        return( nBytes );
    }
        
    /** Static Mary header writer
     * 
     * @param input The data input (DataInputStream or RandomAccessFile) to read from.
     * 
     * @return the number of read bytes.
     * 
     * @throws IOException (forwarded from the random access file read operations)
     * 
     * @author sacha
     */
    public void load( DataInput input ) throws IOException {
        
        magic = input.readInt();
        version = input.readInt();
        type = input.readInt();
    }
    
    /**
     * Read header from byte buffer
     * @param input
     */
    public void load(ByteBuffer input) {
        magic = input.getInt();
        version = input.getInt();
        type = input.getInt();
    }
    
    /* Accessors */
    public int getMagic() { return(magic); }
    public int getVersion() { return(version); }
    public int getType() { return(type); }

    /* Checkers */
    public boolean hasLegalMagic() { return( magic == MAGIC ); }
    public boolean hasCurrentVersion() { return( version == VERSION ); }
    public boolean hasBadType() { return( (type > TIMELINE) || (type <= UNKNOWN) ); }
    public boolean hasLegalType() { return( !hasBadType() ); }
    public boolean isMaryHeader() { return( hasLegalMagic() && hasLegalType() ); }

}
