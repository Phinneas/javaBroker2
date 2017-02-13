package edu.uw.beardcl.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.uw.ext.framework.account.Account;
import edu.uw.ext.framework.account.AccountException;
import edu.uw.ext.framework.account.Address;
import edu.uw.ext.framework.account.CreditCard;
import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactoryException;

/**
 * Implementation of the AccountDao using relational database for persistence.
 *
 * @author Chester Beard
 */
public final class DatabaseAccountDao implements AccountDao {
    /** The class' logger */
    private static final Logger logger = LoggerFactory.getLogger(DatabaseAccountDao.class);

    /** The JNDI name used for the DataSource */
    private static final String DATASOURCE_JNDI_NAME = "jdbc/AccountDb";

    /** SQL for determining if an account exists */
    private static final String ACCOUNT_EXISTS_QUERY =
            "SELECT accountid"
          + "  FROM account"
          + " WHERE accountid = ?";

    /** SQL for obtaining account */
    private static final String ACCOUNT_QUERY =
            "SELECT a.password_hash, a.balance, a.fullname, a.phone, a.email,"
          + "       b.street, b.city, b.state, b.zip,"
          + "       c.card_number, c.issuer, c.cardtype, c.holder, c.expires"
          + "  FROM account a"
          + "  LEFT JOIN address b ON a.accountid = b.accountid"
          + "  LEFT JOIN creditcard c ON a.accountid = c.accountid"
          + " WHERE a.accountid = ?";

    /* Column indices for account query result set */
    private static final int QUERY_PASSWD_COL_NDX = 1;
    private static final int QUERY_BALANCE_COL_NDX = 2;
    private static final int QUERY_FULLNAME_COL_NDX = 3;
    private static final int QUERY_PHONE_COL_NDX = 4;
    private static final int QUERY_EMAIL_COL_NDX = 5;
    private static final int QUERY_STREET_COL_NDX = 6;
    private static final int QUERY_CITY_COL_NDX = 7;
    private static final int QUERY_STATE_COL_NDX = 8;
    private static final int QUERY_ZIP_COL_NDX = 9;
    private static final int QUERY_CARDNUM_COL_NDX = 10;
    private static final int QUERY_ISSUER_COL_NDX = 11;
    private static final int QUERY_CARDTYPE_COL_NDX = 12;
    private static final int QUERY_HOLDER_COL_NDX = 13;
    private static final int QUERY_EXPIRES_COL_NDX = 14;

    /** SQL for inserting an account */
    private static final String ACCOUNT_INSERT =
            "INSERT INTO account"
          + " ( password_hash, balance, fullname, phone, email, accountid )"
          + " VALUES ( ?, ?, ?, ?, ?, ? ) ";

    /** SQL for updating an account */
    private static final String ACCOUNT_UPDATE =
            "UPDATE account"
          + "   SET password_hash =?,"
          + "       balance = ?,"
          + "       fullname = ?,"
          + "       phone = ?,"
          + "       email = ?"
          + " WHERE accountid = ?";

    /* Parameter indices for account insert/update */
    private static final int ACCOUNT_PASSWD_PARAM_NDX = 1;
    private static final int ACCOUNT_BALANCE_PARAM_NDX = 2;
    private static final int ACCOUNT_FULLNAME_PARAM_NDX = 3;
    private static final int ACCOUNT_PHONE_PARAM_NDX = 4;
    private static final int ACCOUNT_EMAIL_PARAM_NDX = 5;
    private static final int ACCOUNT_ACCOUNTID_PARAM_NDX = 6;

    /** SQL for determining if an address exists */
    private static final String ADDRESS_EXISTS_QUERY =
            "SELECT accountid"
          + "  FROM address"
          + " WHERE accountid = ?";

    /** SQL for inserting an address */
    private static final String ADDRESS_INSERT =
            "INSERT INTO address"
          + " ( street, city, state, zip, accountid )"
          + " VALUES ( ?, ?, ?, ?, ? ) ";

    /** SQL for updating an account */
    private static final String ADDRESS_UPDATE =
            "UPDATE address"
          + "   SET street = ?,"
          + "       city = ?,"
          + "       state = ?,"
          + "       zip = ?"
          + " WHERE accountid = ?";
    
    /* Parameter indices for account insert/update */
    private static final int ADDRESS_STREET_PARAM_NDX = 1;
    private static final int ADDRESS_CITY_PARAM_NDX = 2;
    private static final int ADDRESS_STATE_PARAM_NDX = 3;
    private static final int ADDRESS_ZIP_PARAM_NDX = 4;
    private static final int ADDRESS_ACCOUNTID_PARAM_NDX = 5;

    /** SQL for determining if an account exists */
    private static final String CREDITCARD_EXISTS_QUERY =
            "SELECT accountid"
          + "  FROM creditcard"
          + " WHERE accountid = ?";

    /** SQL for inserting an credit card */
    private static final String CREDITCARD_INSERT =
            "INSERT INTO creditcard"
          + " ( card_number, issuer, cardtype, holder, expires, accountid )"
          + " VALUES ( ?, ?, ?, ?, ?, ? ) ";

    /** SQL for updating a credit card */
    private static final String CREDITCARD_UPDATE =
            " UPDATE creditcard"
          + " SET card_number = ?,"
          + "     issuer = ?,"
          + "     cardtype = ?,"
          + "     holder = ?,"
          + "     expires = ?"
          + " WHERE accountid = ?";
    
    /* Parameter indices for account insert/update */
    private static final int CREDITCARD_CARDNUM_PARAM_NDX = 1;
    private static final int CREDITCARD_ISSUER_PARAM_NDX = 2;
    private static final int CREDITCARD_CARDTYPE_PARAM_NDX = 3;
    private static final int CREDITCARD_HOLDER_PARAM_NDX = 4;
    private static final int CREDITCARD_EXPIRES_PARAM_NDX = 5;
    private static final int CREDITCARD_ACCOUNTID_PARAM_NDX = 6;

    /** SQL for deleting an account */
    private static final String ACCOUNT_DELETE =
            "DELETE from account"
          + " WHERE accountid = ?";
    /** SQL for deleting an address */
    private static final String ADDRESS_DELETE =
            "DELETE from address"
          + " WHERE accountid = ?";
    /** SQL for deleting a credit card */
    private static final String CREDITCARD_DELETE =
            "DELETE from creditcard"
          + " WHERE accountid = ?";

    /** SQL for resetting the accounts */
    private static final String DELETE_ALL = "DELETE from account";

    /** Parameter index for account id in existence, delete and account queries */
    private static final int ACCOUNTID_PARAM_NDX = 1;
 
    /** The connection. */
    private Connection connection;

    /** Account query prepared statement. */
    private PreparedStatement accountQuery;
    private PreparedStatement accountExistsQuery;
    private PreparedStatement addressExistsQuery;
    private PreparedStatement ccExistsQuery;

    /** Account insert prepared statement. */
    private PreparedStatement accountInsert;

    /** Account update prepared statement. */
    private PreparedStatement accountUpdate;

    /** Address insert prepared statement. */
    private PreparedStatement addressInsert;

    /** Address update prepared statement. */
    private PreparedStatement addressUpdate;

    /** Credit card insert prepared statement. */
    private PreparedStatement ccInsert;

    /** Credit card update prepared statement. */
    private PreparedStatement ccUpdate;

    /** Account delete prepared statement. */
    private PreparedStatement accountDelete;
    private PreparedStatement addressDelete;
    private PreparedStatement ccDelete;

    /**
     * Constructor.  Connects to the database and initializes the prepared
     * statements.
     *
     * @throws DaoFactoryException if unable to connect to the database or
     *         initialize the prepared statements
     */
    public DatabaseAccountDao() throws DaoFactoryException {
        // Connect to the database using JNDI
    	Context ctx = null;
    	try {
            ctx = new InitialContext();
            final DataSource ds = (DataSource) ctx.lookup(DATASOURCE_JNDI_NAME);
            connection = ds.getConnection();
        } catch (final NamingException nex) {
            throw new DaoFactoryException(
                    String.format("Unable to resolve datasource name, '%s'", DATASOURCE_JNDI_NAME),
                    nex);
        } catch (final SQLException ex) {
            throw new DaoFactoryException("Unable to connect to database.", ex);
        } finally {
            if (ctx != null) {
            	try {
					ctx.close();
				} catch (NamingException nex) {
	                logger.warn(String.format("Unable to close context.", nex));
					nex.printStackTrace();
				}
            }
        }

        // Prepare all the statements for future use
        try {
            accountExistsQuery = connection.prepareStatement(ACCOUNT_EXISTS_QUERY);
            addressExistsQuery = connection.prepareStatement(ADDRESS_EXISTS_QUERY);
            ccExistsQuery = connection.prepareStatement(CREDITCARD_EXISTS_QUERY);
            accountQuery = connection.prepareStatement(ACCOUNT_QUERY);
            accountInsert = connection.prepareStatement(ACCOUNT_INSERT);
            accountUpdate = connection.prepareStatement(ACCOUNT_UPDATE);
            addressInsert = connection.prepareStatement(ADDRESS_INSERT);
            addressUpdate = connection.prepareStatement(ADDRESS_UPDATE);
            ccInsert = connection.prepareStatement(CREDITCARD_INSERT);
            ccUpdate = connection.prepareStatement(CREDITCARD_UPDATE);
            accountDelete = connection.prepareStatement(ACCOUNT_DELETE);
            addressDelete = connection.prepareStatement(ADDRESS_DELETE);
            ccDelete = connection.prepareStatement(CREDITCARD_DELETE);
        } catch (final SQLException ex) {
            throw new DaoFactoryException("Unable to initialize prepared statemnts.", ex);
        }
    }

    /**
     * Lookup an account in the database based on username.
     *
     * @param accountName the name of the desired account
     *
     * @return the account if located otherwise null
     */
    public Account getAccount(final String accountName) {
        Account acct = null;
        ResultSet rs = null;
        try (ClassPathXmlApplicationContext appContext =
        	 new ClassPathXmlApplicationContext("context.xml")) {
            accountQuery.setString(ACCOUNTID_PARAM_NDX, accountName);
       	    rs = accountQuery.executeQuery();

            if (rs.next()) {
                acct = appContext.getBean(Account.class);
                acct.setName(accountName);
                acct.setPasswordHash(rs.getBytes(QUERY_PASSWD_COL_NDX));
                acct.setBalance(rs.getInt(QUERY_BALANCE_COL_NDX));

                acct.setFullName(rs.getString(QUERY_FULLNAME_COL_NDX));
                acct.setPhone(rs.getString(QUERY_PHONE_COL_NDX));
                acct.setEmail(rs.getString(QUERY_EMAIL_COL_NDX));

                Address addr;
                final String streetAddress = rs.getString(QUERY_STREET_COL_NDX);
                final String city = rs.getString(QUERY_CITY_COL_NDX);
                final String state = rs.getString(QUERY_STATE_COL_NDX);
                final String zipCode = rs.getString(QUERY_ZIP_COL_NDX);

                if (streetAddress != null ||
                    city != null ||
                    state != null ||
                    zipCode != null) {
                    addr = appContext.getBean(Address.class);
                    addr.setStreetAddress(streetAddress);
                    addr.setCity(city);
                    addr.setState(state);
                    addr.setZipCode(zipCode);
                    acct.setAddress(addr);
                }

                final String accountNumber = rs.getString(QUERY_CARDNUM_COL_NDX);
                final String issuer = rs.getString(QUERY_ISSUER_COL_NDX);
                final String type = rs.getString(QUERY_CARDTYPE_COL_NDX);
                final String holder = rs.getString(QUERY_HOLDER_COL_NDX);
                final String expirationDate = rs.getString(QUERY_EXPIRES_COL_NDX);

                if (accountNumber != null ||
                    issuer != null ||
                    type != null ||
                    holder != null ||
                    expirationDate != null) {
                    CreditCard cc;
                    cc = appContext.getBean(CreditCard.class);
                    cc.setAccountNumber(accountNumber);
                    cc.setIssuer(issuer);
                    cc.setType(type);
                    cc.setHolder(holder);
                    cc.setExpirationDate(expirationDate);
                    acct.setCreditCard(cc);
                }
            } else {
                logger.info(String.format("Account '%s' not in DB", accountName));
            }
        } catch (final BeansException ex) {
            logger.error("Unable to instantiate required classes, null will be returned.", ex);
        } catch (final SQLException ex) {
            logger.error("Unable to retrieve values for account.", ex);
        } catch (final AccountException ex) {
            logger.error("Unable to initialize account.", ex);
        } finally {
        	if (rs != null) {
        		try {
					rs.close();
				} catch (SQLException ex) {
		            logger.warn("Attemp to close result set failed.", ex);
				}
        	}
        }

        return acct;
    }

    /**
     * Adds or updates an account.
     *
     * @param account the account to add/update
     *
     * @exception AccountException if operation fails
     */
	public void setAccount(final Account account) throws AccountException {
        try {
            String accountId = account.getName();
            accountExistsQuery.setString(ACCOUNTID_PARAM_NDX, accountId);
       	    ResultSet rs = accountExistsQuery.executeQuery();
        	boolean acctExists = rs.next();
            @SuppressWarnings("resource")
			PreparedStatement ps = acctExists ? accountUpdate : accountInsert;
            connection.setAutoCommit(false);
        	ps.setBytes(ACCOUNT_PASSWD_PARAM_NDX, account.getPasswordHash());
        	ps.setInt(ACCOUNT_BALANCE_PARAM_NDX, account.getBalance());
        	ps.setString(ACCOUNT_FULLNAME_PARAM_NDX, account.getFullName());
        	ps.setString(ACCOUNT_PHONE_PARAM_NDX, account.getPhone());
        	ps.setString(ACCOUNT_EMAIL_PARAM_NDX, account.getEmail());
            ps.setString(ACCOUNT_ACCOUNTID_PARAM_NDX, accountId);
            ps.executeUpdate();

            insertUpdateAddress(accountId, account.getAddress());
            insertUpdateCreditCard(accountId, account.getCreditCard());

            connection.commit();
        } catch (final SQLException ex) {
            try {
				connection.rollback();
			} catch (SQLException e) {
				logger.error("Failed to rollback", e);
			}
            throw new AccountException("Insertion/update of account failed.", ex);
        } finally {
            try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error("Failed to enable auto commit", e);
			}
        }
    }
	
    /**
     * Inserts, updates or deletes a credit credit card record.
     * 
     * @param accountId the records account id
     * @param address the Address object, may be null
     * @throws SQLException if any occur
     */
	private void insertUpdateAddress(final String accountId,
	                                 final Address address)
	throws SQLException {
	    addressExistsQuery.setString(ACCOUNTID_PARAM_NDX, accountId);
	    ResultSet rs = addressExistsQuery.executeQuery();
	    boolean hasAddress = rs.next();

	    if (address != null) {
	        @SuppressWarnings("resource")
	        PreparedStatement ps = hasAddress ? addressUpdate : addressInsert;
	        ps.setString(ADDRESS_STREET_PARAM_NDX, address.getStreetAddress());
	        ps.setString(ADDRESS_CITY_PARAM_NDX, address.getCity());
	        ps.setString(ADDRESS_STATE_PARAM_NDX, address.getState());
	        ps.setString(ADDRESS_ZIP_PARAM_NDX, address.getZipCode());
	        ps.setString(ADDRESS_ACCOUNTID_PARAM_NDX, accountId);
            ps.executeUpdate();

	    } else if (hasAddress) {
	        addressDelete.setString(ACCOUNTID_PARAM_NDX, accountId);
	        addressDelete.executeUpdate();
	    }
	}
	
	/**
	 * Inserts, updates or deletes a credit credit card record.
	 * 
	 * @param accountId the records account id
	 * @param cc the CreditCard object, may be null
	 * @throws SQLException if any occur
	 */
	private void insertUpdateCreditCard(final String accountId,
	                                    final CreditCard cc)
    throws SQLException {
	    ccExistsQuery.setString(ACCOUNTID_PARAM_NDX, accountId);
	    ResultSet rs = ccExistsQuery.executeQuery();
	    boolean hasCreditCard = rs.next();

	    if (cc != null) {
	        @SuppressWarnings("resource")
	        PreparedStatement ps = hasCreditCard ? ccUpdate : ccInsert;
            ps.setString(CREDITCARD_CARDNUM_PARAM_NDX, cc.getAccountNumber());
            ps.setString(CREDITCARD_ISSUER_PARAM_NDX, cc.getIssuer());
            ps.setString(CREDITCARD_CARDTYPE_PARAM_NDX, cc.getType());
            ps.setString(CREDITCARD_HOLDER_PARAM_NDX, cc.getHolder());
            ps.setString(CREDITCARD_EXPIRES_PARAM_NDX, cc.getExpirationDate());
            ps.setString(CREDITCARD_ACCOUNTID_PARAM_NDX, accountId);
            ps.executeUpdate();

	    } else if (hasCreditCard) {
            ccDelete.setString(ACCOUNTID_PARAM_NDX, accountId);
            ccDelete.executeUpdate();
	    }
	}

    /**
     * Remove the account.
     *
     * @param accountName the name of the account to remove
     *
     * @exception AccountException if operation fails
     */
    public void deleteAccount(final String accountName) throws AccountException
    {
        try {
            accountDelete.setString(ACCOUNTID_PARAM_NDX, accountName);
            accountDelete.executeUpdate();
        } catch (final SQLException ex) {
            throw new AccountException("Account deletion failed", ex);
        }
    }

    /**
     * Remove all accounts.  This is primarily available to facilitate testing.
     *
     * @exception AccountException if operation fails
     */
    public void reset() throws AccountException {
        try (Statement stmnt = connection.createStatement();) {
            stmnt.executeUpdate(DELETE_ALL);
        } catch (final SQLException ex) {
            throw new AccountException("Database deletion failed", ex);
        }
    }

    /**
     * Close the DAO.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (final SQLException ex) {
                logger.warn("Db connection close failed.", ex);
            } finally {
                connection = null;
                logger.info("Db connection closed.");
            }
        }
    }
}

