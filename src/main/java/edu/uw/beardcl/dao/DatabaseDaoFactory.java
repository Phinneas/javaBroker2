package edu.uw.beardcl.dao;

import edu.uw.ext.framework.dao.AccountDao;
import edu.uw.ext.framework.dao.DaoFactory;
import edu.uw.ext.framework.dao.DaoFactoryException;


/**
 * A DaoFactory implementation that instantiates a DatabaseAccountDao.
 *
 * @author Chester Beard
 */
public final class DatabaseDaoFactory implements DaoFactory {
    /**
     * Instantiates a new DatabaseAccountDao object.
     *
     * @return a newly instantiated DatabaseAccountDao object
     * @throws DaoFactoryException if unable to initialize the DAO
     */
    public AccountDao getAccountDao() throws DaoFactoryException {
        return new DatabaseAccountDao();
    }
}

