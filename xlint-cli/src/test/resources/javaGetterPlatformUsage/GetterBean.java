package javaGetterPlatformUsage;

public class GetterBean {
    public String getText() { return null; } // Fixture: unannotated getter deliberately returns null for positive use-site cases.
    @org.jetbrains.annotations.NotNull
    public String getNonNullText() { return "text"; } // Fixture: non-null contract for a negative case.
    public int getCount() { return 1; } // Fixture: primitive getter for a negative case.
}
