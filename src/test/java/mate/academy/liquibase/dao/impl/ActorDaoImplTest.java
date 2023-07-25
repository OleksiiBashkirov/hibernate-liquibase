package mate.academy.liquibase.dao.impl;

import java.util.Optional;
import mate.academy.liquibase.dao.AbstractTest;
import mate.academy.liquibase.dao.ActorDao;
import mate.academy.liquibase.model.Actor;
import mate.academy.liquibase.model.Country;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ActorDaoImplTest extends AbstractTest {
    private static final Actor christianBale = new Actor("Christian Bale");
    private static final Actor bradPitt = new Actor("Brad Pitt");

    @Override
    protected Class<?>[] entities() {
        return new Class[]{Actor.class, Country.class};
    }

    @Before
    public void setUp() throws Exception {
        try (Session session = getSessionFactory().openSession()) {
            Transaction transaction = null;
            try {
                transaction = session.beginTransaction();
                session.createNativeQuery("DELETE FROM actors;", Actor.class).executeUpdate();
                session.createNativeQuery("ALTER TABLE actors ALTER COLUMN id RESTART WITH 0;", Actor.class).executeUpdate();
                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

    @Test
    public void create_Ok() {
        ActorDao actorDao = new ActorDaoImpl(getSessionFactory());
        verifyCreateActorWorks(actorDao, christianBale.clone(), 0L);
        verifyCreateActorWorks(actorDao, bradPitt.clone(), 1L);
    }

    @Test
    public void getById_Ok() {
        ActorDao actorDao = new ActorDaoImpl(getSessionFactory());
        verifyCreateActorWorks(actorDao, christianBale.clone(), 0L);
        Optional<Actor> christianBaleOptional = actorDao.findById(0L);
        Assert.assertTrue(christianBaleOptional.isPresent());
        Actor actualChristianBale = christianBaleOptional.get();
        Assert.assertNotNull(actualChristianBale);
        Assert.assertEquals(0L, actualChristianBale.getId().longValue());
        Assert.assertEquals(christianBale.getName(), actualChristianBale.getName());

        verifyCreateActorWorks(actorDao, bradPitt.clone(), 1L);
        Optional<Actor> bradPittOptional = actorDao.findById(1L);
        Assert.assertTrue(bradPittOptional.isPresent());
        Actor actualBradPitt = bradPittOptional.get();
        Assert.assertNotNull(actualBradPitt);
        Assert.assertEquals(1L, actualBradPitt.getId().longValue());
        Assert.assertEquals(bradPitt.getName(), actualBradPitt.getName());
    }

    @Test
    public void getByNotExistingId_Ok() {
        ActorDao actorDao = new ActorDaoImpl(getSessionFactory());
        Optional<Actor> actual = actorDao.findById(100L);
        Assert.assertFalse(actual.isPresent());
    }

    static void verifyCreateActorWorks(ActorDao actorDao, Actor actor, Long expectedId) {
        Actor actual = actorDao.save(actor);
        Assert.assertNotNull("Check you have implemented the `create` method " +
                "in the ActorDaoImpl class", actual);
        Assert.assertNotNull("ID for actor should be autogenerated", actual.getId());
        Assert.assertEquals(expectedId, actual.getId());
        Assert.assertEquals(actor.getName(), actual.getName());
    }
}