package net.resthub.server.cache;

public class CcLob extends CcBase<byte[]> {

    private static final long serialVersionUID = 1L;
        
    public void setValue(Byte[] bts) {
        byte[] bytes = new byte[bts.length];
        for (int i = 0; i < bts.length; i++) {
            bytes[i] = bts[i];
        }
        setValue(bytes);
    }

    public void setValue(String str) {
        setValue(str.getBytes());
    }
    
}
