package net.celestialdata.plexbot.database;

import net.celestialdata.plexbot.database.models.*;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.persistence.NoResultException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DbOperations {

    /**
     * Save or update a object to the database
     *
     * @param object Any of the models contained in the net.celestialdata.plexbot.database.models package
     * @return true if inserted or updated successfully
     * @see net.celestialdata.plexbot.database.models.Movie
     * @see net.celestialdata.plexbot.database.models.UpgradeItem
     * @see net.celestialdata.plexbot.database.models.User
     * @see net.celestialdata.plexbot.database.models.WaitlistItem
     */
    @SuppressWarnings({"TryFinallyCanBeTryWithResources", "UnusedReturnValue"})
    public static boolean saveObject(Object object) {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            session.saveOrUpdate(object);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return false;
        } finally {
            session.close();
        }
    }

    @SuppressWarnings({"TryFinallyCanBeTryWithResources", "UnusedReturnValue"})
    public static boolean deleteItem(Class<?> type, Serializable id) {
        Transaction transaction = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            transaction = session.beginTransaction();
            var object = (BaseModel) session.get(type, id);
            if (object != null) {
                session.delete(object);
                transaction.commit();
                return true;
            } else return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            return false;
        } finally {
            session.close();
        }
    }


    /**
     * Organizes the database operations relating to Movies
     */
    @SuppressWarnings("unused")
    public static class movieOps {
        /**
         * Fetch a list of all the movies in the database
         *
         * @return a list of movies in the database
         */
        public static List<Movie> getAllMovies() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("from Movie").list();
            } catch (NoResultException e) {
                return new ArrayList<>();
            }
        }

        /**
         * Count the number of Movies in the database
         *
         * @return the number of Movies in the database
         */
        public static int getCount() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return ((Long) session.createQuery("select count(*) from Movie").uniqueResult()).intValue();
            } catch (Exception e) {
                return 0;
            }
        }

        /**
         * Fetch a movie by its IMDb code
         *
         * @param imdbCode IMDb code to search for
         * @return {@link Movie} stored in database
         * @throws ObjectNotFoundException thrown when a movie is not found in the database
         */
        public static Movie getMovieById(String imdbCode) throws ObjectNotFoundException {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.load(Movie.class, imdbCode);
            }
        }

        /**
         * Check if a movie exists in the database
         *
         * @param imdbCode IMDb code to search for
         * @return true if the movie was found in the database
         */
        public static boolean exists(String imdbCode) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.get(Movie.class, imdbCode) != null;
            }
        }
    }

    /**
     * Organizes database operations related to UpgradeItems
     */
    @SuppressWarnings("unused")
    public static class upgradeItemOps {
        /**
         * Fetch a list of all the UpgradeItems in the database
         *
         * @return a list of UpgradeItems in the database
         */
        public static List<UpgradeItem> getAllItems() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM UpgradeItem").list();
            } catch (NoResultException e) {
                return new ArrayList<>();
            }
        }

        /**
         * Count the number of UpgradeItems in the database
         *
         * @return the number of UpgradeItems in the database
         */
        public static int getCount() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return ((Long) session.createQuery("select count(*) from UpgradeItem").uniqueResult()).intValue();
            } catch (Exception e) {
                return 0;
            }
        }

        /**
         * Fetch a UpgradeItem by its IMDb code
         *
         * @param imdbCode IMDb code to search for
         * @return {@link UpgradeItem} stored in database
         * @throws ObjectNotFoundException thrown when a UpgradeItem is not found in the database
         */
        public static UpgradeItem getItemById(String imdbCode) throws ObjectNotFoundException {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.load(UpgradeItem.class, imdbCode);
            }
        }

        /**
         * Check if a UpgradeItem exists in the database
         *
         * @param imdbCode IMDb code to search for
         * @return true if the UpgradeItem was found in the database
         */
        public static boolean exists(String imdbCode) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.get(UpgradeItem.class, imdbCode) != null;
            }
        }
    }

    /**
     * Organizes database operations related to Users
     */
    @SuppressWarnings("unused")
    public static class userOps {
        /**
         * Fetch a list of all the Users in the database
         *
         * @return a list of Users in the database
         */
        public static List<User> getAllUsers() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM User").list();
            } catch (NoResultException e) {
                return new ArrayList<>();
            }
        }

        /**
         * Count the number of Users in the database
         *
         * @return the number of Users in the database
         */
        public static int getCount() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return ((Long) session.createQuery("select count(*) from User").uniqueResult()).intValue();
            } catch (Exception e) {
                return 0;
            }
        }

        /**
         * Fetch a User by its id
         *
         * @param userId user ID to search for
         * @return {@link User} stored in database
         * @throws ObjectNotFoundException thrown when a User is not found in the database
         */
        public static User getUserById(Long userId) throws ObjectNotFoundException {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.load(User.class, userId);
            }
        }

        /**
         * Check if a User exists in the database
         *
         * @param userId user ID to search for
         * @return true if the User was found in the database
         */
        public static boolean exists(Long userId) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.get(User.class, userId) != null;
            }
        }
    }

    /**
     * Organizes database operations related to WaitlistItems
     */
    public static class waitlistItemOps {
        /**
         * Fetch a list of all the UpgradeItems in the database
         *
         * @return a list of UpgradeItems in the database
         */
        public static List<WaitlistItem> getAllItems() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("FROM WaitlistItem").list();
            } catch (NoResultException e) {
                return new ArrayList<>();
            }
        }

        public static List<WaitlistItem> getAllItemsByUser(Long userId) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery("from WaitlistItem i where i.requestedBy.id = " + userId).list();
            } catch (NoResultException e) {
                return new ArrayList<>();
            }
        }

        /**
         * Count the number of WaitlistItems in the database
         *
         * @return the number of WaitlistItems in the database
         */
        public static int getCount() {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return ((Long) session.createQuery("select count(*) from WaitlistItem").uniqueResult()).intValue();
            } catch (Exception e) {
                return 0;
            }
        }

        /**
         * Fetch a WaitlistItem by its IMDb code
         *
         * @param imdbCode IMDb code to search for
         * @return {@link WaitlistItem} stored in database
         * @throws ObjectNotFoundException thrown when a WaitlistItem is not found in the database
         */
        public static WaitlistItem getItemById(String imdbCode) throws ObjectNotFoundException {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.load(WaitlistItem.class, imdbCode);
            }
        }

        /**
         * Check if a WaitlistItem exists in the database
         *
         * @param imdbCode IMDb code to search for
         * @return true if the WaitlistItem was found in the database
         */
        public static boolean exists(String imdbCode) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.get(WaitlistItem.class, imdbCode) != null;
            }
        }
    }
}