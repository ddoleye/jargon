package org.irods.jargon.datautils.image;

import java.io.File;
import java.io.InputStream;

import org.irods.jargon.core.exception.JargonException;

/**
 * Manage the creation and maintenance of thumbnail images for image files
 * stored in iRODS.
 * 
 * Note that local processing of tiff files requires JAI - see http://java.net/projects/jai-core/
 * and see http://java.sun.com/products/java-media/jai/downloads/download-1_1_2.html for download
 * The JAI libraries may need to be installed on the client or mid-tier machine 
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface ThumbnailService {
	
	public static final String THUMBNAIL_RULE_DATA_PARAMETER = "*StdoutStr";

	/**
	 * Given a <code>File</code> that represents a local working directory, ask iRODS to generate 
	 * a thumbnail image for the iRODS data object at the given absolute path.   This temporary thumbnail
	 * file is then returned to the caller.
	 * <p/>
	 * This method will call an image processing routine on the iRODS server, and the thumbnail data will be streamed back 
	 * to this method.  The resulting thumbnail is stored underneath the temporary thumbnail directory based on an internal
	 * scheme.  The <code>File</code> that is returned from this method points to this thumbnail image. 
	 * 
	 * @param workingDirectory <code>File</code> with the path to the top level of a working directory to hold the
	 * thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor <code>String</code> that is the absolute path to the iRODS file
	 * for which a thumbnail will be generated.
	 * @return <code>File</code> that points to the thumbnail image.
	 * @throws IRODSThumbnailProcessUnavailableException if thumbnail processing is not set up on iRODS (imagemagik services)
	 * @throws JargonException
	 */
	File generateThumbnailForIRODSPathViaRule(final File workingDirectory,
			final String irodsAbsolutePathToGenerateThumbnailFor)
	throws IRODSThumbnailProcessUnavailableException, JargonException;

	/**
	 * Given an iRODS absolute path to a data object, retrieve an <code>InputStream</code> which is a thumbnail of
	 * the given file at the iRODS path.
	 * <p/>
	 * Currently, this is done by generating the thumbnail when requested, later, this can include a caching scheme,
	 * and alternative cache locations (local verus in iRODS AVU, etc).  Consider this a first approximation.
	 *
	 * @param irodsAbsolutePathToGenerateThumbnailFor <code>String</code> that is the absolute path to the iRODS file
	 * for which a thumbnail will be generated.
	 * @return <code>InputStream</code> that is the thumbnail image data.  No buffering is done to the stream that is returned.  
	 * @throws JargonException
	 */
	InputStream retrieveThumbnailByIRODSAbsolutePathViaRule(
			
			final String irodsAbsolutePathToGenerateThumbnailFor)
			throws JargonException;

	/**
	 * Create a thumb-nail by down-loading the file and processing the image locally.  This version uses the AWT image
	 * processing code, and creates a .jpg thumbnail.
	 * @param workingDirectory <code>File</code> with the path to the top level of a working directory to hold the
	 * thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor <code>String</code> that is the absolute path to the iRODS file
	 * for which a thumbnail will be generated.
	 * @param thumbWidth <code>int</code> with the desired image width
	 * @param thumbHeight <code>int</code> with the desired image height
	 * @return {@link File} with the thumbnail image
	 * @throws Exception
	 */
	File createThumbnailLocally(File workingDirectory,
			String irodsAbsolutePathToGenerateThumbnailFor, int thumbWidth,
			int thumbHeight) throws Exception;

	/**
	 * Do a check to see whether the thumbnail service is available on the iRODS server.  If it is not available, the mid-tier fallback can be used
	 * <code>createThumbnailLocally()</code>.
	 * <p/>
	 * Note that it is not efficient to call this method repeatedly, rather, a client service should call once for an iRODS server and cache the result.
	 * @return <code>true</code> if the iRODS server has support for imagemagik thumbnail generation.  If the hueristic cannot determine, it will
	 * return false.  The current heuristic is to use the listCommands.sh script, which must be added to the /server/bin/cmd directory, along with the 
	 * makeThumbnail.py script.  
	 * @throws JargonException
	 */
	boolean isIRODSThumbnailGeneratorAvailable() throws JargonException;

	/**
	 * Create a thumb-nail by down-loading the file and processing the image
	 * locally using the JAI image library, which can process a TIFF file. The
	 * JAI version will create a .PNG thumbnail.
	 * 
	 * @param workingDirectory
	 *            <code>File</code> with the path to the top level of a working
	 *            directory to hold the thumbnail image.
	 * @param irodsAbsolutePathToGenerateThumbnailFor
	 *            <code>String</code> that is the absolute path to the iRODS
	 *            file for which a thumbnail will be generated.
	 * @param maxEdge
	 *            <code>int</code> with the desired max edge length
	 * @return {@link File} with the thumbnail image. Note that it is the
	 *         responsibility of the caller to clean up this image after
	 *         processing, either by deleting the returned file or by adding a
	 *         reaper process. This might be added to the API later.
	 * @throws Exception
	 */
	File createThumbnailLocallyViaJAI(File workingDirectory,
			String irodsAbsolutePathToGenerateThumbnailFor, int maxEdge) throws Exception;

}