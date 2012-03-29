package org.irods.jargon.core.pub;

import org.irods.jargon.core.connection.ConnectionProgressStatus;
import org.irods.jargon.core.connection.ConnectionProgressStatus.CallbackType;
import org.irods.jargon.core.connection.ConnectionProgressStatusListener;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.transfer.TransferControlBlock;
import org.irods.jargon.core.transfer.TransferStatus;
import org.irods.jargon.core.transfer.TransferStatus.TransferType;
import org.irods.jargon.core.transfer.TransferStatusCallbackListener;

/**
 * Implementation of a listener for intra-file status call-backs. This object
 * receives call-backs from the lower-level connection, and can then generate
 * higher-level status call-backs as needed. The lifetime of this listener
 * should be for a transfer of one file.
 * <p/>
 * This object uses simple optimizations, such as aggregating multiple low-level
 * call-backs to fewer higher-level call-backs, to keep the chatter to a
 * minimum. Note that this is an initial implementation, and further
 * configuration and optimization strategies may be employed later. In other
 * words, this object may change.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DefaultIntraFileProgressCallbackListener implements
		ConnectionProgressStatusListener {

	private final TransferStatusCallbackListener transferStatusCallbackListener;
	private final TransferType transferType;
	private final long totalBytesToTransfer;
	private long totalBytesTransferred;
	private final int interval;

	private int countOfMessagesSinceLastSend = 0;
	public static final int BYTE_COUNT_MESSAGE_THRESHOLD = 25;

	/**
	 * Static initializer method to create an immutable call-back listener for
	 * intra-file status information.
	 * 
	 * @param transferType
	 *            {@link TransferType} enum value
	 * @param totalBytesToTransfer
	 *            <code>long</code> with the total size of the file under
	 *            transfer
	 * @param transferControlBlock
	 *            {@link TransferControlBlock} with information about the
	 *            current transfer
	 * @param transferStatusCallbackListener
	 *            {@link TransferStatusCallbackListener} that will recieve the
	 *            aggregated callbacks from this listener.
	 * @return
	 */
	public static ConnectionProgressStatusListener instance(
			final TransferType transferType, final long totalBytesToTransfer,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener) {
		return new DefaultIntraFileProgressCallbackListener(transferType,
				totalBytesToTransfer, transferControlBlock,
				transferStatusCallbackListener, BYTE_COUNT_MESSAGE_THRESHOLD);
	}

	/**
	 * Static initializer method to create an immutable call-back listener,
	 * specifiy a skip interval that can be used to control the frequency that
	 * this object will use to report up the callback chain.
	 * 
	 * @param transferType
	 * @param totalBytesToTransfer
	 * @param transferControlBlock
	 * @param transferStatusCallbackListener
	 * @return
	 */
	public static ConnectionProgressStatusListener instanceSettingInterval(
			final TransferType transferType,
			final long totalBytesToTransfer,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final int interval) {
		return new DefaultIntraFileProgressCallbackListener(transferType,
				totalBytesToTransfer, transferControlBlock,
				transferStatusCallbackListener, interval);
	}

	private DefaultIntraFileProgressCallbackListener(
			final TransferType transferType,
			final long totalBytesToTransfer,
			final TransferControlBlock transferControlBlock,
			final TransferStatusCallbackListener transferStatusCallbackListener,
			final int interval) {

		if (transferControlBlock == null) {
			throw new IllegalArgumentException("transferControlBlock is null");
		}

		if (transferStatusCallbackListener == null) {
			throw new IllegalArgumentException(
					"transferStatusCallbackListener is null");
		}

		if (transferType == null) {
			throw new IllegalArgumentException("null transferType");
		}

		this.transferStatusCallbackListener = transferStatusCallbackListener;
		this.transferType = transferType;
		this.totalBytesToTransfer = totalBytesToTransfer;
		this.interval = interval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.connection.ConnectionProgressStatusListener#
	 * connectionProgressStatusCallback
	 * (org.irods.jargon.core.connection.ConnectionProgressStatus)
	 */
	@Override
	public synchronized void connectionProgressStatusCallback(
			final ConnectionProgressStatus connectionProgressStatus) {

		// are there bytes to count?
		if (connectionProgressStatus.getCallbackType() == CallbackType.SEND_PROGRESS
				|| connectionProgressStatus.getCallbackType() == CallbackType.RECEIVE_PROGRESS) {
			accumulateAndSend(connectionProgressStatus);

		} else if (connectionProgressStatus.getCallbackType() == CallbackType.OPERATIONAL_MESSAGE) {
			// not yet implemented, would be a message like 'starting parallel
			// transfer', or 'computing checksum'
		}
	}

	private void accumulateAndSend(
			final ConnectionProgressStatus connectionProgressStatus) {
		countOfMessagesSinceLastSend++;
		totalBytesTransferred += connectionProgressStatus.getByteCount();
		if (countOfMessagesSinceLastSend > interval) {
			try {
				TransferStatus transferStatus = TransferStatus
						.instanceForIntraFileStatus(transferType,
								totalBytesToTransfer, totalBytesTransferred);
				transferStatusCallbackListener.statusCallback(transferStatus);
				countOfMessagesSinceLastSend = 0;
				// totalBytesTransferred = 0;
			} catch (JargonException e) {
				throw new JargonRuntimeException(
						"error sending status callback", e);
			}
		}
	}
}
