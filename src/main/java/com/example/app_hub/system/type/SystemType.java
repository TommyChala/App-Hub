package com.example.app_hub.system.type;

public enum SystemType {
    IDENTITY_DATA(1),
    ACCESS_DATA(2);

    private int numVal;

    SystemType(int numVal) {
        this.numVal = numVal;
    }

    public int getNumVal() {
        return numVal;
    }

}
