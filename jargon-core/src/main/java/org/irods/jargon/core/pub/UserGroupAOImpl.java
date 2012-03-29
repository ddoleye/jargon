package org.irods.jargon.core.pub;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.packinstr.GeneralAdminInp;
import org.irods.jargon.core.pub.aohelper.UserAOHelper;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access operations for IRODS user groups.
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public final class UserGroupAOImpl extends IRODSGenericAO implements
		UserGroupAO {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private static final char COMMA = ',';
	private IRODSGenQueryExecutor irodsGenQueryExecutor = null;

	/**
	 * @param irodsSession
	 * @param irodsAccount
	 * @throws JargonException
	 */
	protected UserGroupAOImpl(final IRODSSession irodsSession,
			final IRODSAccount irodsAccount) throws JargonException {
		super(irodsSession, irodsAccount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserGroupAO#addUserGroup(org.irods.jargon.core
	 * .pub.domain.UserGroup)
	 */
	@Override
	public void addUserGroup(final UserGroup userGroup) throws JargonException {

		log.info("addUserGroup()");

		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		if (userGroup.getUserGroupName() == null
				|| userGroup.getUserGroupName().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no userGroupName");
		}

		if (userGroup.getZone() == null || userGroup.getZone().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no zone");
		}

		log.info("user group:{}", userGroup);

		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForAddUserGroup(userGroup);
		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserGroupAO#removeUserGroup(org.irods.jargon
	 * .core.pub.domain.UserGroup)
	 */
	@Override
	public void removeUserGroup(final UserGroup userGroup)
			throws JargonException {
		log.info("removeUserGroup()");
		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		if (userGroup.getUserGroupName() == null
				|| userGroup.getUserGroupName().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no userGroupName");
		}

		if (userGroup.getZone() == null || userGroup.getZone().isEmpty()) {
			throw new IllegalArgumentException("userGroup has no zone");
		}

		log.info("user group:{}", userGroup);

		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForRemoveUserGroup(userGroup);
		log.debug("executing admin PI");

		try {
			getIRODSProtocol().irodsFunction(adminPI);
		} catch (InvalidUserException e) {
			log.warn("user group {} does not exist, ignoring remove", userGroup);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.IRODSUserGroupAO#find(java.lang.String)
	 */
	@Override
	public UserGroup find(final String userGroupId) throws JargonException {

		if (userGroupId == null || userGroupId.length() == 0) {
			throw new JargonException("null or missing userGroupId");
		}

		log.info("finding user group with id: {}", userGroupId);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		query.append(" = '");
		query.append(userGroupId.trim());
		query.append("'");

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		if (resultSet.getResults().size() == 0) {
			log.info("no user group found");
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user group found for id:");
			messageBuilder.append(userGroupId);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user group

		IRODSQueryResultRow row;
		try {
			row = resultSet.getFirstResult();
		} catch (DataNotFoundException e) {
			return null;
		}
		return buildUserGroupFromResultSet(row);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.irods.jargon.core.pub.UserGroupAO#findAll()
	 */
	@Override
	public List<UserGroup> findAll() throws JargonException {

		log.info("findAll()");

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();
		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findByName(java.lang.String)
	 */
	@Override
	public UserGroup findByName(final String userGroupName)
			throws JargonException {

		if (userGroupName == null || userGroupName.length() == 0) {
			throw new JargonException("null or missing userGroupName");
		}

		log.info("finding user group with name: {}", userGroupName);
		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(" = '");
		query.append(userGroupName.trim());
		query.append("'");

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		if (resultSet.getResults().size() == 0) {
			log.info("no user group found");
			return null;
		}

		if (resultSet.getResults().size() > 1) {
			StringBuilder messageBuilder = new StringBuilder();
			messageBuilder.append("more than one user group found for name:");
			messageBuilder.append(userGroupName);
			String message = messageBuilder.toString();
			log.error(message);
			throw new JargonException(message);
		}

		// I know I have just one user group

		IRODSQueryResultRow row;
		try {
			row = resultSet.getFirstResult();
		} catch (DataNotFoundException e) {
			return null;
		}
		return buildUserGroupFromResultSet(row);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findWhere(java.lang.String)
	 */
	@Override
	public List<UserGroup> findWhere(final String whereClause)
			throws JargonException, JargonQueryException {

		if (whereClause == null || whereClause.length() == 0) {
			throw new JargonException("null or missing where clause");
		}

		log.info("find user group with provided where clause: {}", whereClause);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" where ");
		query.append(whereClause);

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserGroupAO#listUserGroupMembers(java.lang.
	 * String)
	 */
	@Override
	public List<User> listUserGroupMembers(final String userGroupName)
			throws JargonException {

		log.info("listUserGroupMembers()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userGroupName");
		}

		log.info("for user group name:{}", userGroupName);

		List<User> users = new ArrayList<User>();

		// create query for users in group

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		StringBuilder query = new StringBuilder();

		query.append(UserAOHelper.buildUserSelects());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(" where ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(" = '");
		query.append(userGroupName.trim());
		query.append("'");

		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 5000);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			// build user, do not retrieve the user DN (too expensive)
			users.add(UserAOHelper.buildUserFromResultSet(row,
					irodsGenQueryExecutor, false));
		}

		return users;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.IRODSUserGroupAO#findUserGroupsForUser(java
	 * .lang.String)
	 */
	@Override
	public List<UserGroup> findUserGroupsForUser(final String userName)
			throws JargonException {
		if (userName == null || userName.length() == 0) {
			throw new JargonException("null or missing userName");
		}

		log.info("find user group with user name: {}", userName);

		IRODSGenQueryExecutor irodsGenQueryExecutor = getGenQueryExecutor();

		StringBuilder query = new StringBuilder();

		query.append(buildUserGroupSelects());
		query.append(" WHERE ");
		query.append(RodsGenQueryEnum.COL_USER_NAME.getName());
		query.append(" = '");
		query.append(userName.trim());
		query.append("'");
		String queryString = query.toString();
		log.info("query string: {}", queryString);

		IRODSGenQuery irodsQuery = IRODSGenQuery.instance(queryString, 500);

		IRODSQueryResultSetInterface resultSet;
		try {
			resultSet = irodsGenQueryExecutor.executeIRODSQueryAndCloseResult(
					irodsQuery, 0);
		} catch (JargonQueryException e) {
			log.error("query exception for user query:" + queryString, e);
			throw new JargonException("error in user group query");
		}

		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		for (IRODSQueryResultRow row : resultSet.getResults()) {
			userGroups.add(buildUserGroupFromResultSet(row));
		}

		return userGroups;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserGroupAO#addUserToGroup(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addUserToGroup(final String userGroupName,
			final String userName, final String zoneName)
			throws InvalidGroupException, InvalidUserException, JargonException {

		log.info("addUserToGroup()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userName");
		}

		log.info("userName:{}", userName);
		log.info("userGroupName:{}", userGroupName);

		if (zoneName != null) {
			log.info("zoneName:{}", zoneName);
		}

		// call the iadmin iRODS service

		GeneralAdminInp adminPI = GeneralAdminInp.instanceForAddUserToGroup(
				userGroupName, userName, zoneName);

		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irods.jargon.core.pub.UserGroupAO#removeUserFromGroup(java.lang.String
	 * , java.lang.String, java.lang.String)
	 */
	@Override
	public void removeUserFromGroup(final String userGroupName,
			final String userName, final String zoneName)
			throws InvalidUserException, InvalidGroupException, JargonException {

		log.info("removeUserFromGroup()");

		if (userGroupName == null || userGroupName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userGroupName");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or emtpy userName");
		}

		log.info("userName:{}", userName);
		log.info("userGroupName:{}", userGroupName);

		if (zoneName != null) {
			log.info("zoneName:{}", zoneName);
		}

		// call the iadmin iRODS service

		GeneralAdminInp adminPI = GeneralAdminInp
				.instanceForRemoveUserFromGroup(userGroupName, userName,
						zoneName);

		log.debug("executing admin PI");

		getIRODSProtocol().irodsFunction(adminPI);

	}

	private String buildUserGroupSelects() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT ");
		query.append(RodsGenQueryEnum.COL_USER_GROUP_NAME.getName());
		query.append(COMMA);
		query.append(RodsGenQueryEnum.COL_USER_GROUP_ID.getName());
		return query.toString();
	}

	private UserGroup buildUserGroupFromResultSet(final IRODSQueryResultRow row)
			throws JargonException {
		UserGroup userGroup = new UserGroup();
		userGroup.setUserGroupId(row.getColumn(1));
		userGroup.setUserGroupName(row.getColumn(0));

		return userGroup;
	}

	private IRODSGenQueryExecutor getGenQueryExecutor() throws JargonException {
		if (irodsGenQueryExecutor == null) {
			irodsGenQueryExecutor = this.getIRODSAccessObjectFactory()
					.getIRODSGenQueryExecutor(getIRODSAccount());
		}

		return irodsGenQueryExecutor;

	}

}
