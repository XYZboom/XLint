package platformArgumentToKotlin;

public class JavaCaller {
    public void call(String platformValue) {
        KotlinApi api = new KotlinApi();
        api.nonNull(platformValue);
        api.nullable(platformValue);
        api.nonNull("known non-null");
    }
}
