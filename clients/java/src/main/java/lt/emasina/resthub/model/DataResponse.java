package lt.emasina.resthub.model;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;
import lombok.Getter;
import org.restlet.representation.Representation;

@Getter
public class DataResponse {
    
    private final String string;
    private final Map headers;
    private final int status;
    
    public DataResponse(Representation r, Map headers, int status) throws IOException{
        this.string = r.getText();
        this.headers = headers;
        this.status = status;
    }
    
    public Reader getReader() throws IOException{
        return new StringReader(string);
    }
}
