package net.resthub.server.cache;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public abstract class CcBase<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private T value;
	
}
