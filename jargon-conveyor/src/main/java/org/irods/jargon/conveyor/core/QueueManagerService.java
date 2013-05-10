/**
 * 
 */
package org.irods.jargon.conveyor.core;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.transfer.dao.domain.Transfer;
import org.irods.jargon.transfer.dao.domain.TransferType;

/**
 * Manages the persistent queue of transfer information
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public interface QueueManagerService {

	/**
	 * Cause a put operation (transfer to iRODS) to occur. This transfer will be
	 * based on the given iRODS account information.
	 * 
	 * @param transfer
	 *            {@link Transfer} to be executed
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the
	 * @throws ConveyorExecutionException
	 */
	void enqueueTransferOperation(final Transfer transfer,
			final IRODSAccount irodsAccount) throws ConveyorExecutionException;

	/**
	 * Signal that,if the queue is not busy, that the next pending operation
	 * should be launched
	 * 
	 * @throws ConveyerExecutionException
	 */
	void dequeueNextOperation() throws ConveyorExecutionException,
		
                JargonException, Exception;
        /**
	 * Convenience function for iDrop to start a transfer
	 * based on the given iRODS account information.
	 * 
	 * @param irodsFile
	 *            String full path of iRODS file/folder for get or put
         * @param localFile
	 *            String full path of local file/folder for get or put
	 * @param irodsAccount
	 *            {@link IRODSAccount} describing the IRODSAccount
         * @param TransferType
         *            {@link TransferType} type of transfer - GET, PUT, etc
	 * @throws ConveyorExecutionException
	 */
        void processTransfer(final String irodsFile,
                    final String localFile,
                    final IRODSAccount irodsAccount,
                    final TransferType type) throws ConveyorExecutionException;

}
