package lt.emasina.resthub.util;

import com.google.inject.persist.PersistService;
import javax.inject.Inject;

/**
 * JpaDeinitializer class
 *
 * @author valdo
 */
public class JpaDeinitializer {

    @Inject
    public JpaDeinitializer(PersistService service) {
        service.stop();
    }
    
}
