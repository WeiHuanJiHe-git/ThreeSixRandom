package com.fonuhuo.sevenrandom;

public final class HistoryRecord {
    public long id;
    public String name;
    public int n1;
    public int n2;
    public int n3;
    public String p1;
    public String p2;
    public String p3;
    public boolean saved;
    public long createdAt;

    public String[] palaces() {
        return new String[]{p1, p2, p3};
    }
}
