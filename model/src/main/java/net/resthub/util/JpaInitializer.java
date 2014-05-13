package net.resthub.util;

import com.google.inject.persist.PersistService;
import javax.inject.Inject;

/**
 * JpaInitializer class
 *
 * @author valdo
 */
public class JpaInitializer {

    @Inject
    public JpaInitializer(PersistService service) {
        service.start();
    }
    
}
