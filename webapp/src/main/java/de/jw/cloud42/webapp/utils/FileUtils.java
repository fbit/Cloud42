/**
 * 
 */
package de.jw.cloud42.webapp.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Contains some convenience functions for handling file data.
 * 
 * @author fbitzer
 *
 */
public class FileUtils {
	
	/**
	 * Returns the contents of the file in a byte array.
	 * Source: http://www.exampledepot.com/egs/java.io/File2ByteArray.html
	 * @param file
	 * @return
	 * @throws IOException
	 */
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
    
        // Get the size of the file
        long length = file.length();
    
        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
    
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }
    
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
    
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }

	
    public static String extractFilename(String path){
    	
    	
    		
    	File f = new File(path);
    	return f.getName();
    	
    	
    	
    }

}
