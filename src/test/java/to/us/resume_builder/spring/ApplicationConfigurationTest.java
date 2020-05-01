package to.us.resume_builder.spring;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import to.us.resume_builder.ApplicationConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests methods in the {@link ApplicationConfiguration} class
 * @author Brooklynn Stone
 */
public class ApplicationConfigurationTest {
    /**
     * {@link ApplicationConfiguration} instance to run tests on
     */
    static ApplicationConfiguration ac;

    /**
     * Initializes a Singletong {@link ApplicationConfiguration} instance
     */
    @BeforeAll
    static void init() {
        ac = ApplicationConfiguration.getInstance();
    }

    /**
     * Tests that {@link ApplicationConfiguration} is a Singleton
     */
    @Test
    void testGetInstance() {
        assertEquals(ac, ApplicationConfiguration.getInstance());
    }

    /**
     * Test that the internal structure of the map is as it should be
     */
    @Test
    void testSetDefaults() {
        assertEquals("./templates/", ac.getString("templates.directory"));
        assertEquals("./temp/", ac.getString("export.tempLocation"));
        assertEquals(60L, ac.getLong("export.timeout"));
        assertEquals(60L, ac.getLong("upload.timeout"));
        assertEquals("file.io", ac.getString("upload.url"));
    }


}
