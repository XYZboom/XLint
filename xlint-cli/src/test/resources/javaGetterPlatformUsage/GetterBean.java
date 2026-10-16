package javaGetterPlatformUsage;

public class GetterBean {
    public String getText() { return null; }
    @org.jetbrains.annotations.NotNull
    public String getNonNullText() { return "text"; }
    public int getCount() { return 1; }
}
