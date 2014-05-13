package net.resthub.server.parser.check;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * SelectListDef
 * @author valdo
 */
public class SelectListDef {

    @Getter
    private final List<SubSelectDef> selectDefs = new ArrayList<>();
    
}
