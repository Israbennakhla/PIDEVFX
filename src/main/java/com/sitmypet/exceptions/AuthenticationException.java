package com.sitmypet.exceptions;

public class AuthenticationException extends Exception {
    private long unlockTimeMillis;
    
    public AuthenticationException(String message) {
        super(message);
        this.unlockTimeMillis = 0;
    }
    
    public AuthenticationException(String message, long unlockTimeMillis) {
        super(message);
        this.unlockTimeMillis = unlockTimeMillis;
    }
    
    public long getUnlockTimeMillis() {
        return unlockTimeMillis;
    }
}
